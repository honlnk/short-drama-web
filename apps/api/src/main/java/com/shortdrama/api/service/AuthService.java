package com.shortdrama.api.service;

import com.shortdrama.api.dto.AuthResponse;
import com.shortdrama.api.dto.LoginRequest;
import com.shortdrama.api.dto.RegisterRequest;
import com.shortdrama.api.dto.UserInfo;
import com.shortdrama.api.entity.User;
import com.shortdrama.api.entity.UserRole;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.UserRepository;
import com.shortdrama.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** 注册即登录：成功后直接签发 token（PRD US-01）。 */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(409, "用户名已被注册");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.username());
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return respond(user);
    }

    /** 登录失败不区分「用户不存在」与「密码错误」，统一提示。 */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        return respond(user);
    }

    /** 供前端启动时校验 token 并回填登录态（nickname 不在 JWT 载荷里，需查库）。 */
    @Transactional(readOnly = true)
    public UserInfo getMe(Long userId) {
        return userRepository.findById(userId)
                .map(AuthService::toUserInfo)
                .orElseThrow(() -> new BusinessException(401, "未登录或登录已过期"));
    }

    private AuthResponse respond(User user) {
        return new AuthResponse(jwtService.generateToken(user), toUserInfo(user));
    }

    private static UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getUsername(), user.getNickname(), user.getRole().name());
    }
}
