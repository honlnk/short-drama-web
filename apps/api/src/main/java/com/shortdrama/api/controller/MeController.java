package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.security.AuthUser;
import com.shortdrama.api.service.InteractionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 「我的」相关接口（阶段3 任务1）。/api/me/** 已由 SecurityConfig 配为需登录。
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final InteractionService interactionService;

    public MeController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    /** 我的收藏列表：按收藏时间倒序。 */
    @GetMapping("/favorites")
    public ApiResponse<List<DramaSummary>> favorites(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return ApiResponse.ok(interactionService.listFavorites(authUser.getId()));
    }
}
