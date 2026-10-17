package com.shortdrama.api.controller;

import com.shortdrama.api.dto.AdminDramaDetail;
import com.shortdrama.api.dto.AdminDramaRequest;
import com.shortdrama.api.dto.AdminDramaRow;
import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.PageResponse;
import com.shortdrama.api.service.AdminDramaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台短剧管理（AD-01~04）。/api/admin/** 已由 SecurityConfig 配为需 ADMIN，
 * 进入本控制器的请求均已通过角色校验。
 */
@RestController
@RequestMapping("/api/admin/dramas")
public class AdminDramaController {

    private final AdminDramaService adminDramaService;

    public AdminDramaController(AdminDramaService adminDramaService) {
        this.adminDramaService = adminDramaService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminDramaRow>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(adminDramaService.list(page, size, keyword));
    }

    /** 编辑表单回填用单查（含 description；OFFLINE 剧对 C 端详情 404，后台必须自有一查）。 */
    @GetMapping("/{id}")
    public ApiResponse<AdminDramaDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminDramaService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<AdminDramaRow> create(@Valid @RequestBody AdminDramaRequest request) {
        return ApiResponse.ok(adminDramaService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminDramaRow> update(@PathVariable Long id, @Valid @RequestBody AdminDramaRequest request) {
        return ApiResponse.ok(adminDramaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminDramaService.delete(id);
        return ApiResponse.ok(null);
    }
}
