package com.shortdrama.api.dto;

/**
 * 对外暴露的用户信息（不含密码摘要）。
 */
public record UserInfo(Long id, String username, String nickname, String role) {
}
