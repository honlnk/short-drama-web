package com.shortdrama.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 进度上报（PL-04）：dramaId 走查询参数（剧是资源定位），body 只带本集信息。
 * progressSec 允许 0（刚起播），负数在校验层拦下；上限由服务层按该集时长截断。
 */
public record ProgressReportRequest(
        @NotNull(message = "episodeId 不能为空") @Positive(message = "episodeId 必须为正整数") Long episodeId,
        @NotNull(message = "progressSec 不能为空") @PositiveOrZero(message = "progressSec 不能为负数") Integer progressSec
) {
}
