package com.shortdrama.api.security;

import com.shortdrama.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 桥接 Spring Security 的 UserDetailsService 体系：按用户名从 user 表装配 AuthUser。
 * 阶段2 登录走 AuthService 直查仓储，此类供 Security 基础设施（全局 AuthenticationManager）装配使用。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new AuthUser(user.getId(), user.getUsername(), user.getPassword(), user.getRole()))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
