package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.FavoriteToggleResponse;
import com.shortdrama.api.dto.LikeToggleResponse;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.security.AuthUser;
import com.shortdrama.api.service.InteractionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞/收藏切换（阶段3 任务1）。路径与 SecurityConfig 阶段2 预配的
 * POST /api/dramas/[id]/like、[id]/favorite 鉴权规则逐字对应。
 */
@RestController
@RequestMapping("/api")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/dramas/{id}/like")
    public ApiResponse<LikeToggleResponse> like(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        requireLogin(authUser);
        return ApiResponse.ok(interactionService.toggleLike(id, authUser.getId()));
    }

    @PostMapping("/dramas/{id}/favorite")
    public ApiResponse<FavoriteToggleResponse> favorite(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        requireLogin(authUser);
        return ApiResponse.ok(interactionService.toggleFavorite(id, authUser.getId()));
    }

    /** SecurityConfig 已拦截匿名访问，principal 为 null 仅作兜底（与 AuthController.me 同口径）。 */
    private void requireLogin(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
    }
}
