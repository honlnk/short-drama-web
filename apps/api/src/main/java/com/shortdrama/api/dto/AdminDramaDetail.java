package com.shortdrama.api.dto;

import com.shortdrama.api.entity.Drama;

/** 后台编辑回填用单查结果（比列表行多 description）。 */
public record AdminDramaDetail(
        Long id,
        String title,
        String description,
        String category,
        String coverUrl,
        String status
) {

    public static AdminDramaDetail from(Drama drama) {
        return new AdminDramaDetail(
                drama.getId(),
                drama.getTitle(),
                drama.getDescription(),
                drama.getCategory(),
                drama.getCoverUrl(),
                drama.getStatus().name()
        );
    }
}
