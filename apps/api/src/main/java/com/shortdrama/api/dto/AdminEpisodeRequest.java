package com.shortdrama.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 新增/编辑剧集请求。videoUrl 只接受本服务 /uploads/ 产物（同封面口径）。 */
public record AdminEpisodeRequest(
        @NotNull(message = "集数不能为空")
        @Min(value = 1, message = "集数从 1 开始")
        Integer episodeNo,

        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过 100 字")
        String title,

        @NotBlank(message = "视频不能为空")
        String videoUrl,

        @NotNull(message = "时长不能为空")
        @Min(value = 1, message = "时长（秒）需大于 0")
        Integer durationSec
) {
}
