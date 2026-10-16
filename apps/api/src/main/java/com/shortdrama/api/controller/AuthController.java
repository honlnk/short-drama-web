package com.shortdrama.api.controller;

import com.shortdrama.api.dto.AuthResponse;
import com.shortdrama.api.dto.LoginRequest;
import com.shortdrama.api.dto.RegisterRequest;
import com.shortdrama.api.dto.UserInfo;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.security.AuthUser;
import com.shortdrama.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 登录态校验：SecurityConfig 已拦截匿名访问，principal 为 null 仅作兜底。 */
    @GetMapping("/me")
    public UserInfo me(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return authService.getMe(authUser.getId());
    }
}
