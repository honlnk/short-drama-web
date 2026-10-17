package com.shortdrama.api.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象（PRD §7.3：与种子媒体同目录同映射单轨制）。
 * 本期唯一实现 LocalStorageService；storage.type=oss 为讲解占位，不实现。
 */
public interface StorageService {

    /**
     * 校验并落盘上传文件，返回可访问 URL（/uploads/files/&lt;uuid&gt;.&lt;ext&gt;）。
     * 校验：类型白名单（封面 jpg/jpeg/png/webp ≤5MB；视频 mp4 ≤50MB）+ 文件头 magic bytes。
     *
     * @param file  上传文件
     * @param usage cover | video，决定白名单与大小上限
     */
    String store(MultipartFile file, String usage);

    /** 按 URL 删除落盘文件（本阶段删除内容不调用——演示期保留磁盘文件，接口完整性预留）。 */
    void delete(String url);

    /** 物理路径 → 访问 URL。 */
    String getUrl(String filename);
}
