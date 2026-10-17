package com.shortdrama.api.dto;

/**
 * 点赞切换结果（IN-01 幂等切换）：liked 为切换后的最新状态，likeCount 同步返回供前端即时刷新。
 */
public record LikeToggleResponse(boolean liked, int likeCount) {
}
