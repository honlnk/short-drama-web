package com.shortdrama.api.config;

import com.shortdrama.api.security.JwtAuthFilter;
import com.shortdrama.api.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * JWT 无状态鉴权（阶段2）：
 * - 公开：认证接口、ping、静态资源、C 端浏览类 GET（/api/dramas 列表/详情/剧集）。
 * - 登录：点赞/收藏、/api/auth/me、/api/me/**（接口里程碑 4 实现，规则先行）。
 * - 管理员：/api/admin/**（接口里程碑 5 实现，规则先行）。
 * 未带 token 访问受保护接口 → 401 JSON（AuthenticationEntryPoint）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 顺序即优先级：/api/auth/me 需登录，先于 /api/auth/** 公开规则
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**", "/api/ping", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dramas", "/api/dramas/{id}", "/api/dramas/{id}/episodes").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/dramas/*/like", "/api/dramas/*/favorite").authenticated()
                        .requestMatchers("/api/me/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        // 匿名/坏 token → 401；已登录但角色不足（如 USER 调 /api/admin/**）→ 403
                        .authenticationEntryPoint(this::unauthorized)
                        .accessDeniedHandler(this::forbidden))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void unauthorized(HttpServletRequest request, HttpServletResponse response,
                              org.springframework.security.core.AuthenticationException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
    }

    private void forbidden(HttpServletRequest request, HttpServletResponse response,
                           org.springframework.security.access.AccessDeniedException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"无权限访问该功能\",\"data\":null}");
    }
}
