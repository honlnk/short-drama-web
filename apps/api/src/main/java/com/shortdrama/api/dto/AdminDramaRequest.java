package com.shortdrama.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 新增/编辑短剧请求（AD-01/03）。coverUrl 只接受本服务 /uploads/ 产物（防外链，保断网可演示）。 */
public record AdminDramaRequest(
        @NotBlank(message = "剧名不能为空")
        @Size(max = 100, message = "剧名不能超过 100 字")
        String title,

        @Size(max = 2000, message = "简介过长")
        String description,

        @NotBlank(message = "分类不能为空")
        @Size(max = 20, message = "分类不能超过 20 字")
        String category,

        @NotBlank(message = "封面不能为空")
        String coverUrl,

        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "ONLINE|OFFLINE", message = "状态只允许 ONLINE/OFFLINE")
        String status
) {
}
