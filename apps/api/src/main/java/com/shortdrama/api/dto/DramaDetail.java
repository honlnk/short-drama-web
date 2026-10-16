package com.shortdrama.api.dto;

import java.util.List;

/**
 * 短剧详情：DramaSummary 全部字段 + 剧集列表 + 当前用户点赞/收藏状态回显（可选登录，未登录恒 false）。
 */
public record DramaDetail(
        Long id,
        String title,
        String description,
        String category,
        String coverUrl,
        int likeCount,
        String status,
        int episodeCount,
        List<EpisodeSummary> episodes,
        boolean liked,
        boolean favorited
) {
}
