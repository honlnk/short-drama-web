package com.shortdrama.api.service;

import com.shortdrama.api.dto.AdminEpisodeRequest;
import com.shortdrama.api.dto.EpisodeSummary;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.EpisodeRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台剧集管理（阶段3 任务3）。删集不自动重排集数（决策：由管理员手工维护 episodeNo，
 * (drama_id, episode_no) 唯一索引兜底防重复）。
 */
@Service
public class AdminEpisodeService {

    private final EpisodeRepository episodeRepository;
    private final DramaRepository dramaRepository;
    private final WatchProgressRepository watchProgressRepository;

    public AdminEpisodeService(EpisodeRepository episodeRepository, DramaRepository dramaRepository,
                               WatchProgressRepository watchProgressRepository) {
        this.episodeRepository = episodeRepository;
        this.dramaRepository = dramaRepository;
        this.watchProgressRepository = watchProgressRepository;
    }

    @Transactional(readOnly = true)
    public List<EpisodeSummary> list(Long dramaId) {
        requireDrama(dramaId);
        return episodeRepository.findByDramaIdOrderByEpisodeNoAsc(dramaId).stream()
                .map(episode -> new EpisodeSummary(
                        episode.getId(), episode.getEpisodeNo(), episode.getTitle(),
                        episode.getVideoUrl(), episode.getDurationSec()))
                .toList();
    }

    @Transactional
    public EpisodeSummary create(Long dramaId, AdminEpisodeRequest request) {
        requireDrama(dramaId);
        checkEpisodeNoFree(dramaId, request.episodeNo(), null);
        Episode episode = new Episode();
        episode.setDrama(dramaRepository.getReferenceById(dramaId));
        applyRequest(episode, request);
        try {
            episodeRepository.saveAndFlush(episode);
        } catch (DataIntegrityViolationException e) {
            // 并发窗口：唯一索引兜底
            throw new BusinessException(400, "集数已存在");
        }
        return toSummary(episode);
    }

    @Transactional
    public EpisodeSummary update(Long episodeId, AdminEpisodeRequest request) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(404, "剧集不存在"));
        checkEpisodeNoFree(episode.getDrama().getId(), request.episodeNo(), episodeId);
        applyRequest(episode, request);
        try {
            episodeRepository.saveAndFlush(episode);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "集数已存在");
        }
        return toSummary(episode);
    }

    /** 删集：先清引用该集的观看进度（FK 约束），不级联磁盘文件。 */
    @Transactional
    public void delete(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(404, "剧集不存在"));
        watchProgressRepository.deleteByEpisodeId(episodeId);
        episodeRepository.delete(episode);
    }

    private void checkEpisodeNoFree(Long dramaId, int episodeNo, Long excludeEpisodeId) {
        boolean taken = excludeEpisodeId == null
                ? episodeRepository.existsByDramaIdAndEpisodeNo(dramaId, episodeNo)
                : episodeRepository.findByDramaIdOrderByEpisodeNoAsc(dramaId).stream()
                        .anyMatch(e -> e.getEpisodeNo() == episodeNo && !e.getId().equals(excludeEpisodeId));
        if (taken) {
            throw new BusinessException(400, "集数已存在");
        }
    }

    private void applyRequest(Episode episode, AdminEpisodeRequest request) {
        if (request.videoUrl() == null || !request.videoUrl().startsWith("/uploads/")) {
            throw new BusinessException(400, "视频地址必须为本站上传产物（/uploads/ 开头）");
        }
        episode.setEpisodeNo(request.episodeNo());
        episode.setTitle(request.title().trim());
        episode.setVideoUrl(request.videoUrl().trim());
        episode.setDurationSec(request.durationSec());
    }

    private Drama requireDrama(Long id) {
        return dramaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在"));
    }

    private static EpisodeSummary toSummary(Episode episode) {
        return new EpisodeSummary(episode.getId(), episode.getEpisodeNo(), episode.getTitle(),
                episode.getVideoUrl(), episode.getDurationSec());
    }
}
