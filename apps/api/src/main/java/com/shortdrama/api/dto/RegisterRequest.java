package com.shortdrama.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "不能为空")
        @Size(min = 2, max = 50, message = "长度须在 2~50 之间")
        String username,

        @NotBlank(message = "不能为空")
        @Size(max = 72, message = "长度不能超过 72")
        String password
) {
}
