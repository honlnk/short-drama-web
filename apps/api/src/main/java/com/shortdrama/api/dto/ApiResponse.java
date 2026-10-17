package com.shortdrama.api.dto;

/**
 * 统一响应/错误包体：{code, message, data}。成功 code=0，失败 code=HTTP 状态码。
 */
public record ApiResponse<T>(int code, String message, T data) {

    /** 成功包体工厂（PRD 接口规范：全部 /api 接口统一包体，成功 code=0）。 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }
}
