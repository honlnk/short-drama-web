package com.shortdrama.api.exception;

import com.shortdrama.api.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 统一异常出口：业务异常与参数校验异常转 {code, message, data} 结构（与 /api/ping 响应风格一致）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(e.getStatus(), e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> invalidArgument(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数不合法");
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, message, null));
    }

    /** multipart 超出 application.yml 上限（50MB）时由容器抛出，转成可读 400。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> uploadTooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "文件大小超出上限（封面 ≤5MB，视频 ≤50MB）", null));
    }

    /** 数据完整性冲突兜底（如并发窗口撞 (drama_id, episode_no) 唯一索引）。 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> dataConflict(DataIntegrityViolationException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "数据冲突，请检查后重试", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(500, "服务器内部错误", null));
    }
}
