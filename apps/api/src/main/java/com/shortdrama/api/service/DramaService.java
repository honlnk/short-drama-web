package com.shortdrama.api.service;

import com.shortdrama.api.dto.DramaDetail;
import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.dto.EpisodeSummary;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.EpisodeRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DramaService {

    private final DramaRepository dramaRepository;
    private final EpisodeRepository episodeRepository;
    private final UserLikeRepository userLikeRepository;
    private final FavoriteRepository favoriteRepository;

    public DramaService(DramaRepository dramaRepository, EpisodeRepository episodeRepository,
                        UserLikeRepository userLikeRepository, FavoriteRepository favoriteRepository) {
        this.dramaRepository = dramaRepository;
        this.episodeRepository = episodeRepository;
        this.userLikeRepository = userLikeRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional(readOnly = true)
    public List<DramaSummary> listOnline() {
        return dramaRepository.findByStatusOrderByLikeCountDesc(DramaStatus.ONLINE).stream()
                .map(DramaSummary::from)
                .toList();
    }

    /** 详情（可选登录）：currentUserId 为 null 时 liked/favorited 恒 false。C 端不可见 OFFLINE 剧。 */
    @Transactional(readOnly = true)
    public DramaDetail getDetail(Long id, Long currentUserId) {
        Drama drama = requireOnlineDrama(id);
        List<EpisodeSummary> episodes = listEpisodes(id);
        boolean liked = currentUserId != null && userLikeRepository.existsByUserIdAndDramaId(currentUserId, id);
        boolean favorited = currentUserId != null && favoriteRepository.existsByUserIdAndDramaId(currentUserId, id);
        return new DramaDetail(
                drama.getId(),
                drama.getTitle(),
                drama.getDescription(),
                drama.getCategory(),
                drama.getCoverUrl(),
                drama.getLikeCount(),
                drama.getStatus().name(),
                episodes.size(),
                episodes,
                liked,
                favorited
        );
    }

    @Transactional(readOnly = true)
    public List<EpisodeSummary> listEpisodes(Long id) {
        requireOnlineDrama(id);
        return episodeRepository.findByDramaIdOrderByEpisodeNoAsc(id).stream()
                .map(DramaService::toEpisodeSummary)
                .toList();
    }

    /** 剧不存在或已下架对 C 端一律 404，不暴露下架状态。 */
    private Drama requireOnlineDrama(Long id) {
        return dramaRepository.findById(id)
                .filter(drama -> drama.getStatus() == DramaStatus.ONLINE)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在或已下架"));
    }

    private static EpisodeSummary toEpisodeSummary(Episode episode) {
        return new EpisodeSummary(
                episode.getId(),
                episode.getEpisodeNo(),
                episode.getTitle(),
                episode.getVideoUrl(),
                episode.getDurationSec()
        );
    }
}
