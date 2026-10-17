package com.shortdrama.api.controller;

import com.shortdrama.api.dto.ApiResponse;
import com.shortdrama.api.dto.UploadResponse;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.storage.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 后台文件上传（阶段3 任务3）：type 决定白名单与大小上限（cover|video）。 */
@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    private final StorageService storageService;

    public AdminFileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("type") String type) {
        if (!"cover".equals(type) && !"video".equals(type)) {
            throw new BusinessException(400, "上传类型只支持 cover 或 video");
        }
        return ApiResponse.ok(new UploadResponse(storageService.store(file, type)));
    }
}
