package com.shortdrama.api.dto;

/**
 * 收藏切换结果（IN-02 幂等切换）：收藏无计数展示需求，只返回最新状态。
 */
public record FavoriteToggleResponse(boolean favorited) {
}
