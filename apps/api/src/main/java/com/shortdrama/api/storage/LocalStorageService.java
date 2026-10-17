package com.shortdrama.api.storage;

import com.shortdrama.api.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘实现：落盘 {app.upload-dir}/files/（与种子媒体同目录同映射，WebMvcConfig 已映射 /uploads/**）。
 * uuid 文件名规避碰撞与中文名问题；安全三重校验：扩展名白名单 + 大小上限 + 文件头 magic bytes
 * （防改后缀伪装，现场讲解可提安全意识）。
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> COVER_EXTS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4");
    private static final long COVER_MAX_BYTES = 5L * 1024 * 1024;
    private static final long VIDEO_MAX_BYTES = 50L * 1024 * 1024;

    private final Path filesDir;

    public LocalStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.filesDir = Paths.get(uploadDir, "files");
    }

    @PostConstruct
    void ensureDir() throws IOException {
        Files.createDirectories(filesDir);
    }

    @Override
    public String store(MultipartFile file, String usage) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        String ext = extensionOf(file.getOriginalFilename());
        boolean isCover = "cover".equals(usage);
        Set<String> allowedExts = isCover ? COVER_EXTS : VIDEO_EXTS;
        long maxBytes = isCover ? COVER_MAX_BYTES : VIDEO_MAX_BYTES;

        if (!allowedExts.contains(ext)) {
            throw new BusinessException(400,
                    isCover ? "封面仅支持 jpg/jpeg/png/webp 格式" : "视频仅支持 mp4 格式");
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessException(400,
                    isCover ? "封面大小不能超过 5MB" : "视频大小不能超过 50MB");
        }
        if (!magicBytesMatch(file, ext)) {
            throw new BusinessException(400, "文件内容与扩展名不符，请勿伪装文件类型");
        }

        String filename = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filesDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败，请重试");
        }
        return getUrl(filename);
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/files/")) {
            return;
        }
        String filename = url.substring("/uploads/files/".length());
        // 只删本目录内的文件名，防路径穿越
        Path target = filesDir.resolve(filename).normalize();
        if (!target.startsWith(filesDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 删除失败不阻断业务（本阶段删除链路不调用此方法）
        }
    }

    @Override
    public String getUrl(String filename) {
        return "/uploads/files/" + filename;
    }

    private static String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 文件头嗅探：mp4（ftyp）、jpg（FFD8FF）、png（89504E47）、webp（RIFF....WEBP）。 */
    private static boolean magicBytesMatch(MultipartFile file, String ext) {
        byte[] head = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            return false;
        }
        if (read < 4) {
            return false;
        }
        return switch (ext) {
            case "mp4" -> read >= 8
                    && head[4] == 'f' && head[5] == 't' && head[6] == 'y' && head[7] == 'p';
            case "jpg", "jpeg" -> (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
            case "png" -> (head[0] & 0xFF) == 0x89 && head[1] == 0x50 && head[2] == 0x4E && head[3] == 0x47;
            case "webp" -> read >= 12
                    && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            default -> false;
        };
    }
}
