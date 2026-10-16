package com.shortdrama.api.security;

import com.shortdrama.api.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * SecurityContext 中的当前登录用户，供 @AuthenticationPrincipal 注入。
 * 两条构造路径：JWT 过滤器（无密码摘要）/ UserDetailsService（带 BCrypt 摘要供登录校验）。
 */
public class AuthUser implements UserDetails {

    private final Long id;
    private final String username;
    /** BCrypt 摘要；来自 token 构造时为空串。 */
    private final String password;
    private final UserRole role;

    public AuthUser(Long id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
