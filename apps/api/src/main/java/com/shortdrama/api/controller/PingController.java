package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        return new ApiResponse<>(0, "pong", Map.of("time", Instant.now().toString()));
    }
}
