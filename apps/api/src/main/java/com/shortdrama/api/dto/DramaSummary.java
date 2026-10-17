package com.shortdrama.api.dto;

import com.shortdrama.api.entity.Drama;

/**
 * C 端首页推荐的剧集卡片数据（DR-01：封面、剧名、集数、点赞数）。
 * 收藏列表（IN-03）复用同一结构，映射唯一来源在 from 工厂。
 */
public record DramaSummary(
        Long id,
        String title,
        String description,
        String category,
        String coverUrl,
        int likeCount,
        String status,
        int episodeCount
) {

    public static DramaSummary from(Drama drama) {
        return new DramaSummary(
                drama.getId(),
                drama.getTitle(),
                drama.getDescription(),
                drama.getCategory(),
                drama.getCoverUrl(),
                drama.getLikeCount(),
                drama.getStatus().name(),
                drama.getEpisodes().size()
        );
    }
}
