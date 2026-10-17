package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.HistoryItem;
import com.shortdrama.api.dto.ProgressReportRequest;
import com.shortdrama.api.dto.ProgressReportResponse;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.security.AuthUser;
import com.shortdrama.api.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 观看进度接口（阶段4 任务1）。与第三阶段 MeController 同前缀 /api/me 不同文件、
 * 各管各的端点，互不包含。鉴权由 SecurityConfig 的 /api/me/** 规则兜住。
 */
@RestController
@RequestMapping("/api/me")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /** 进度上报（PL-04）：每用户每剧 upsert 一条，前端 5s 节流调用。dramaId 走查询参数（计划 §五定案）。 */
    @PutMapping("/progress")
    public ApiResponse<ProgressReportResponse> report(@RequestParam Long dramaId,
                                                      @Valid @RequestBody ProgressReportRequest request,
                                                      @AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return ApiResponse.ok(progressService.report(authUser.getId(), dramaId, request));
    }

    /** 观看历史（PL-05）：与 PL-04 共用 watch_progress 数据，最近观看在前。 */
    @GetMapping("/history")
    public ApiResponse<List<HistoryItem>> history(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return ApiResponse.ok(progressService.history(authUser.getId()));
    }
}
