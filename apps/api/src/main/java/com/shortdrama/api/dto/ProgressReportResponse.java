package com.shortdrama.api.dto;

/**
 * 进度上报结果：upsert 语义下恒为 true，保留结构便于前端统一处理与后续扩展。
 */
public record ProgressReportResponse(boolean updated) {
}
