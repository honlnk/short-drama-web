package com.shortdrama.api.service;

import com.shortdrama.api.dto.AdminEpisodeRequest;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.EpisodeRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * 后台剧集管理单测（阶段3 任务3 门禁）：episodeNo 冲突（预查 + 唯一索引兜底）、
 * 视频 URL 白名单、删集先清观看进度。
 */
@ExtendWith(MockitoExtension.class)
class AdminEpisodeServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;
    @Mock
    private DramaRepository dramaRepository;
    @Mock
    private WatchProgressRepository watchProgressRepository;
    @InjectMocks
    private AdminEpisodeService service;

    private final Drama drama = new Drama();

    @BeforeEach
    void setUp() {
        drama.setId(1L);
        org.mockito.Mockito.lenient().when(dramaRepository.findById(1L)).thenReturn(Optional.of(drama));
        org.mockito.Mockito.lenient().when(dramaRepository.getReferenceById(1L)).thenReturn(drama);
    }

    private static AdminEpisodeRequest request(int episodeNo) {
        return new AdminEpisodeRequest(episodeNo, "第" + episodeNo + "集", "/uploads/files/v.mp4", 20);
    }

    @Test
    void create_集数已存在_400() {
        when(episodeRepository.existsByDramaIdAndEpisodeNo(1L, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, request(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("集数已存在");
    }

    @Test
    void create_并发窗口撞唯一索引_兜底400() {
        when(episodeRepository.existsByDramaIdAndEpisodeNo(1L, 2)).thenReturn(false);
        when(episodeRepository.saveAndFlush(any(Episode.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> service.create(1L, request(2)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("集数已存在");
    }

    @Test
    void create_视频非本站上传产物_400() {
        AdminEpisodeRequest bad = new AdminEpisodeRequest(1, "第1集", "https://cdn.example.com/v.mp4", 20);

        assertThatThrownBy(() -> service.create(1L, bad))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("/uploads/");
    }

    @Test
    void create_合法请求_返回集数摘要() {
        when(episodeRepository.existsByDramaIdAndEpisodeNo(1L, 3)).thenReturn(false);
        when(episodeRepository.saveAndFlush(any(Episode.class))).thenAnswer(inv -> inv.getArgument(0));

        var summary = service.create(1L, request(3));

        assertThat(summary.episodeNo()).isEqualTo(3);
        assertThat(summary.videoUrl()).isEqualTo("/uploads/files/v.mp4");
    }

    @Test
    void update_集数与自身相同_不判重复() {
        Episode existing = new Episode();
        existing.setId(5L);
        existing.setDrama(drama);
        existing.setEpisodeNo(2);
        existing.setTitle("第2集");
        existing.setVideoUrl("/uploads/files/old.mp4");
        existing.setDurationSec(15);
        when(episodeRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(episodeRepository.findByDramaIdOrderByEpisodeNoAsc(1L)).thenReturn(List.of(existing));
        when(episodeRepository.saveAndFlush(any(Episode.class))).thenAnswer(inv -> inv.getArgument(0));

        var summary = service.update(5L, request(2));

        assertThat(summary.title()).isEqualTo("第2集");
    }

    @Test
    void delete_先清观看进度再删集() {
        Episode existing = new Episode();
        existing.setId(5L);
        existing.setDrama(drama);
        when(episodeRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.delete(5L);

        InOrder order = inOrder(watchProgressRepository, episodeRepository);
        order.verify(watchProgressRepository).deleteByEpisodeId(5L);
        order.verify(episodeRepository).delete(existing);
    }
}
