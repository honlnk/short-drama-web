package com.shortdrama.api.dto;

/**
 * 观看历史条目（PL-05）：DramaSummary 全字段平铺 + 该条进度的集信息。
 * 不过滤 OFFLINE 剧（历史是用户私有数据），前端点击下架剧会撞详情 404，属正常口径。
 */
public record HistoryItem(
        Long dramaId,
        String title,
        String description,
        String category,
        String coverUrl,
        int likeCount,
        String status,
        int episodeCount,
        Long episodeId,
        int episodeNo,
        String episodeTitle,
        int progressSec,
        String updatedAt
) {
}
