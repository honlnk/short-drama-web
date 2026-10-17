package com.shortdrama.api.controller;

import com.shortdrama.api.dto.AdminEpisodeRequest;
import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.EpisodeSummary;
import com.shortdrama.api.service.AdminEpisodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 后台剧集管理：列表挂在剧下（/api/admin/dramas/{id}/episodes），编辑/删除按剧集 id。 */
@RestController
public class AdminEpisodeController {

    private final AdminEpisodeService adminEpisodeService;

    public AdminEpisodeController(AdminEpisodeService adminEpisodeService) {
        this.adminEpisodeService = adminEpisodeService;
    }

    @GetMapping("/api/admin/dramas/{id}/episodes")
    public ApiResponse<List<EpisodeSummary>> list(@PathVariable Long id) {
        return ApiResponse.ok(adminEpisodeService.list(id));
    }

    @PostMapping("/api/admin/dramas/{id}/episodes")
    public ApiResponse<EpisodeSummary> create(@PathVariable Long id, @Valid @RequestBody AdminEpisodeRequest request) {
        return ApiResponse.ok(adminEpisodeService.create(id, request));
    }

    @PutMapping("/api/admin/episodes/{id}")
    public ApiResponse<EpisodeSummary> update(@PathVariable Long id, @Valid @RequestBody AdminEpisodeRequest request) {
        return ApiResponse.ok(adminEpisodeService.update(id, request));
    }

    @DeleteMapping("/api/admin/episodes/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminEpisodeService.delete(id);
        return ApiResponse.ok(null);
    }
}
