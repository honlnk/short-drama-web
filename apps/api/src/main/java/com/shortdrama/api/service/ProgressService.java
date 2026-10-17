package com.shortdrama.api.service;

import com.shortdrama.api.dto.HistoryItem;
import com.shortdrama.api.dto.ProgressReportRequest;
import com.shortdrama.api.dto.ProgressReportResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.entity.WatchProgress;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.EpisodeRepository;
import com.shortdrama.api.repository.UserRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 观看进度（阶段4 任务1，PL-04/05 后端）。
 *
 * report 刻意不开外层事务：并发双击撞 (user_id, drama_id) 唯一索引时，
 * save 的独立事务干净回滚，异常被本类捕获后重查走更新路径——若套一层
 * @Transactional，共享事务会被标记 rollback-only，提交时变 500（与第三阶段
 * InteractionTxService 拆 bean 解决的是同一问题，此处用「无外层事务」达成）。
 * 更新路径显式 save() 而非依赖 OSIV 的 dirty checking，语义不随配置漂移。
 */
@Service
public class ProgressService {

    private final WatchProgressRepository watchProgressRepository;
    private final DramaRepository dramaRepository;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;

    public ProgressService(WatchProgressRepository watchProgressRepository, DramaRepository dramaRepository,
                           EpisodeRepository episodeRepository, UserRepository userRepository) {
        this.watchProgressRepository = watchProgressRepository;
        this.dramaRepository = dramaRepository;
        this.episodeRepository = episodeRepository;
        this.userRepository = userRepository;
    }

    /** upsert：每用户每剧一条，存在则更新集/秒/时间，不存在则插入（PRD §6「每部剧只保留最新一条进度」）。 */
    public ProgressReportResponse report(Long userId, Long dramaId, ProgressReportRequest request) {
        // ONLINE/OFFLINE 均可：用户对下架剧的历史进度仍可写，与 history 不过滤 OFFLINE 口径一致
        Drama drama = dramaRepository.findById(dramaId)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在"));
        Episode episode = episodeRepository.findById(request.episodeId())
                .filter(item -> item.getDrama().getId().equals(dramaId))
                .orElseThrow(() -> new BusinessException(400, "剧集不属于该短剧"));
        int progressSec = Math.min(Math.max(request.progressSec(), 0), episode.getDurationSec());

        WatchProgress progress = watchProgressRepository.findByUserIdAndDramaId(userId, dramaId).orElse(null);
        if (progress == null) {
            progress = new WatchProgress();
            progress.setUser(userRepository.getReferenceById(userId));
            progress.setDrama(drama);
            try {
                progress.setEpisode(episode);
                progress.setProgressSec(progressSec);
                progress.setUpdatedAt(LocalDateTime.now());
                watchProgressRepository.save(progress);
            } catch (DataIntegrityViolationException e) {
                // 并发首插撞唯一索引：对手已写入，按最终状态收敛为更新对手那条
                updateExisting(userId, dramaId, episode, progressSec);
            }
        } else {
            progress.setEpisode(episode);
            progress.setProgressSec(progressSec);
            progress.setUpdatedAt(LocalDateTime.now());
            watchProgressRepository.save(progress);
        }
        return new ProgressReportResponse(true);
    }

    /** 观看历史（PL-05）：updated_at 倒序；Drama/Episode 已被删的脏记录跳过（防管理端级联删除遗漏）。 */
    @Transactional(readOnly = true)
    public List<HistoryItem> history(Long userId) {
        return watchProgressRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ProgressService::toHistoryItem)
                .filter(item -> item != null)
                .toList();
    }

    private void updateExisting(Long userId, Long dramaId, Episode episode, int progressSec) {
        WatchProgress existing = watchProgressRepository.findByUserIdAndDramaId(userId, dramaId)
                .orElseThrow(() -> new BusinessException(409, "进度写入冲突，请重试"));
        existing.setEpisode(episode);
        existing.setProgressSec(progressSec);
        existing.setUpdatedAt(LocalDateTime.now());
        watchProgressRepository.save(existing);
    }

    /** LAZY 代理在 Drama/Episode 被删后访问会抛异常，逐条隔离保证单条脏数据不拖垮整个列表。 */
    private static HistoryItem toHistoryItem(WatchProgress progress) {
        try {
            Drama drama = progress.getDrama();
            Episode episode = progress.getEpisode();
            return new HistoryItem(
                    drama.getId(),
                    drama.getTitle(),
                    drama.getDescription(),
                    drama.getCategory(),
                    drama.getCoverUrl(),
                    drama.getLikeCount(),
                    drama.getStatus().name(),
                    drama.getEpisodes().size(),
                    episode.getId(),
                    episode.getEpisodeNo(),
                    episode.getTitle(),
                    progress.getProgressSec(),
                    progress.getUpdatedAt().toString()
            );
        } catch (RuntimeException e) {
            return null;
        }
    }
}
