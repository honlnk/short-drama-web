package com.shortdrama.api.storage;

import com.shortdrama.api.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 本地存储校验单测（阶段3 任务3 门禁）：扩展名白名单、大小上限、magic bytes 防伪装、落盘与 URL。
 */
class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new LocalStorageService(tempDir.toString());
        service.ensureDir();
    }

    /** 构造带合法 mp4 文件头（偏移 4-7 为 ftyp）的假视频。 */
    private static byte[] mp4Bytes(int size) {
        byte[] data = new byte[size];
        data[4] = 'f';
        data[5] = 't';
        data[6] = 'y';
        data[7] = 'p';
        return data;
    }

    @Test
    void store_合法mp4落盘并返回URL() {
        MockMultipartFile file = new MockMultipartFile("file", "ep-01.mp4", "video/mp4", mp4Bytes(64));

        String url = service.store(file, "video");

        assertThat(url).startsWith("/uploads/files/").endsWith(".mp4");
        String filename = url.substring("/uploads/files/".length());
        assertThat(Files.exists(tempDir.resolve("files").resolve(filename))).isTrue();
    }

    @Test
    void store_合法jpg封面() {
        byte[] jpg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", jpg);

        assertThat(service.store(file, "cover")).endsWith(".jpg");
    }

    @Test
    void store_扩展名不在白名单_400() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", mp4Bytes(16));

        assertThatThrownBy(() -> service.store(file, "video"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mp4");
        assertThatThrownBy(() -> service.store(file, "cover"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("封面");
    }

    @Test
    void store_txt内容伪装mp4后缀_400() {
        // 内容是纯文本（无 ftyp 头），后缀伪装成 .mp4 —— magic bytes 拦截
        byte[] fake = "hello world this is a text file".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.mp4", "video/mp4", fake);

        assertThatThrownBy(() -> service.store(file, "video"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("伪装");
    }

    @Test
    void store_封面超5MB_400() {
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg",
                new byte[5 * 1024 * 1024 + 1]);

        assertThatThrownBy(() -> service.store(file, "cover"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    void store_空文件_400() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.mp4", "video/mp4", new byte[0]);

        assertThatThrownBy(() -> service.store(file, "video"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选择");
    }
}
