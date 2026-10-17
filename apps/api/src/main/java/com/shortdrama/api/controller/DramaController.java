package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.DramaDetail;
import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.dto.EpisodeSummary;
import com.shortdrama.api.security.AuthUser;
import com.shortdrama.api.service.DramaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    /** C 端首页推荐流：上架短剧，点赞数倒序。 */
    @GetMapping("/dramas")
    public ApiResponse<List<DramaSummary>> listOnline() {
        return ApiResponse.ok(dramaService.listOnline());
    }

    /** 详情（可选登录）：带 token 返回 liked/favorited 真实状态，匿名恒 false。 */
    @GetMapping("/dramas/{id}")
    public ApiResponse<DramaDetail> detail(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok(dramaService.getDetail(id, authUser != null ? authUser.getId() : null));
    }

    /** 剧集列表（公开）：播放页选集数据源。 */
    @GetMapping("/dramas/{id}/episodes")
    public ApiResponse<List<EpisodeSummary>> episodes(@PathVariable Long id) {
        return ApiResponse.ok(dramaService.listEpisodes(id));
    }
}
