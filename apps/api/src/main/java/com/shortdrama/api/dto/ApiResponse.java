package com.shortdrama.api.dto;

/**
 * 统一响应/错误包体：{code, message, data}。成功 code=0，失败 code=HTTP 状态码。
 */
public record ApiResponse<T>(int code, String message, T data) {
}
