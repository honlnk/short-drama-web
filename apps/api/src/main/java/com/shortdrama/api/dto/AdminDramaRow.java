package com.shortdrama.api.dto;

import com.shortdrama.api.entity.Drama;

import java.time.LocalDateTime;

/** 后台短剧列表行（含 OFFLINE，比 C 端多 status/createdAt 维度）。 */
public record AdminDramaRow(
        Long id,
        String title,
        String category,
        int episodeCount,
        String status,
        int likeCount,
        LocalDateTime createdAt,
        String coverUrl
) {

    public static AdminDramaRow from(Drama drama) {
        return new AdminDramaRow(
                drama.getId(),
                drama.getTitle(),
                drama.getCategory(),
                drama.getEpisodes().size(),
                drama.getStatus().name(),
                drama.getLikeCount(),
                drama.getCreatedAt(),
                drama.getCoverUrl()
        );
    }
}
