package com.shortdrama.api.controller;

import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.service.DramaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    /** C 端首页推荐流：上架短剧，点赞数倒序。 */
    @GetMapping("/dramas")
    public List<DramaSummary> listOnline() {
        return dramaService.listOnline();
    }
}
