package com.shortdrama.api.dto;

/**
 * 注册/登录成功响应：注册即登录，直接返回 JWT 与用户信息。
 */
public record AuthResponse(String token, UserInfo user) {
}
