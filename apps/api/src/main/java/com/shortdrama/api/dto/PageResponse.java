package com.shortdrama.api.dto;

import java.util.List;

/** 后台分页响应：字段收敛，避免直接序列化 Spring Data Page 的不稳定结构。 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long total
) {
}
