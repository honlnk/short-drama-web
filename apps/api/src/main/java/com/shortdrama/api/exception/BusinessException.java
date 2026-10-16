package com.shortdrama.api.exception;

/**
 * 业务异常：携带 HTTP 状态码与用户可读信息，由 GlobalExceptionHandler 统一转 JSON。
 */
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
