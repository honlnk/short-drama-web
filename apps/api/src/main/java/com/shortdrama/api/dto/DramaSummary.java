package com.shortdrama.api.dto;

/**
 * C 端首页推荐的剧集卡片数据（DR-01：封面、剧名、集数、点赞数）。
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
}
