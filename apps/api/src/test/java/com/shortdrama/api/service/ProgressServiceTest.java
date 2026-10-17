package com.shortdrama.api.service;

import com.shortdrama.api.dto.HistoryItem;
import com.shortdrama.api.dto.ProgressReportRequest;
import com.shortdrama.api.dto.ProgressReportResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.entity.WatchProgress;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.EpisodeRepository;
import com.shortdrama.api.repository.UserRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 阶段4 任务1：进度 upsert / 截断 / 归属校验 / 历史组装与脏记录跳过（纯 Mockito 单测，不起 Spring 上下文）。
 * 实体是 Lombok POJO，直接 new + set 造数；save 用 thenAnswer 捕获入参供断言。
 */
class ProgressServiceTest {

    private WatchProgressRepository watchProgressRepository;
    private EpisodeRepository episodeRepository;
    private ProgressService progressService;

    private final Long userId = 7L;
    private final Long dramaId = 1L;
    private Drama drama;
    private Episode ep2;
    private Episode ep3;

    /** 捕获最近一次 save 入参，供断言最终落库字段。 */
    private WatchProgress savedEntity;

    @BeforeEach
    void setUp() {
        watchProgressRepository = mock(WatchProgressRepository.class);
        episodeRepository = mock(EpisodeRepository.class);
        DramaRepository dramaRepository = mock(DramaRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        progressService = new ProgressService(watchProgressRepository, dramaRepository, episodeRepository, userRepository);

        drama = new Drama();
        drama.setId(dramaId);
        drama.setTitle("闪婚老公是豪门");
        drama.setStatus(DramaStatus.ONLINE);

        ep2 = new Episode();
        ep2.setId(21L);
        ep2.setDrama(drama);
        ep2.setEpisodeNo(2);
        ep2.setTitle("第2集");
        ep2.setDurationSec(60);

        ep3 = new Episode();
        ep3.setId(31L);
        ep3.setDrama(drama);
        ep3.setEpisodeNo(3);
        ep3.setTitle("第3集");
        ep3.setDurationSec(90);

        when(dramaRepository.findById(dramaId)).thenReturn(Optional.of(drama));
        when(episodeRepository.findById(21L)).thenReturn(Optional.of(ep2));
        when(episodeRepository.findById(31L)).thenReturn(Optional.of(ep3));
        when(watchProgressRepository.findByUserIdAndDramaId(userId, dramaId)).thenReturn(Optional.empty());
        stubSavePassThrough();
    }

    private void stubSavePassThrough() {
        when(watchProgressRepository.save(ArgumentMatchers.any(WatchProgress.class)))
                .thenAnswer(invocation -> {
                    savedEntity = invocation.getArgument(0);
                    return savedEntity;
                });
    }

    @Test
    void reportInsertsWhenAbsent() {
        ProgressReportResponse response = progressService.report(userId, dramaId, new ProgressReportRequest(21L, 35));

        assertThat(response.updated()).isTrue();
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getEpisode()).isEqualTo(ep2);
        assertThat(savedEntity.getProgressSec()).isEqualTo(35);
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    void reportUpdatesExistingRecordInPlace() {
        WatchProgress existing = new WatchProgress();
        existing.setEpisode(ep2);
        existing.setProgressSec(35);
        when(watchProgressRepository.findByUserIdAndDramaId(userId, dramaId)).thenReturn(Optional.of(existing));

        progressService.report(userId, dramaId, new ProgressReportRequest(31L, 8));

        // 同剧换集仍是同一条记录：episode 换成第 3 集、秒数覆盖
        assertThat(existing.getEpisode()).isEqualTo(ep3);
        assertThat(existing.getProgressSec()).isEqualTo(8);
        assertThat(savedEntity).isSameAs(existing);
    }

    @Test
    void reportClampsProgressToUpperBound() {
        progressService.report(userId, dramaId, new ProgressReportRequest(21L, 9999));

        assertThat(savedEntity.getProgressSec()).isEqualTo(60);
    }

    @Test
    void reportClampsProgressToLowerBound() {
        // 服务层下限兜底（负数在 DTO 校验层已拦，这里防的是绕过校验的调用方）
        progressService.report(userId, dramaId, new ProgressReportRequest(21L, -5));

        assertThat(savedEntity.getProgressSec()).isZero();
    }

    @Test
    void reportRejectsEpisodeFromOtherDrama() {
        Episode foreign = new Episode();
        foreign.setId(99L);
        Drama other = new Drama();
        other.setId(2L);
        foreign.setDrama(other);
        when(episodeRepository.findById(99L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> progressService.report(userId, dramaId, new ProgressReportRequest(99L, 10)))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getStatus()).isEqualTo(400));
    }

    @Test
    void reportRejectsMissingDrama() {
        assertThatThrownBy(() -> progressService.report(userId, 404L, new ProgressReportRequest(21L, 10)))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getStatus()).isEqualTo(404));
    }

    @Test
    void reportRecoversFromUniqueIndexConflict() {
        WatchProgress winner = new WatchProgress();
        winner.setEpisode(ep2);
        winner.setProgressSec(1);
        // 首次 save 撞唯一索引，重查后的收敛更新走正常保存
        when(watchProgressRepository.save(ArgumentMatchers.any(WatchProgress.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(watchProgressRepository.findByUserIdAndDramaId(userId, dramaId))
                .thenReturn(Optional.empty())    // 首查：无记录
                .thenReturn(Optional.of(winner)); // 冲突后重查：对手已写入

        ProgressReportResponse response = progressService.report(userId, dramaId, new ProgressReportRequest(31L, 8));

        // 并发首插撞唯一索引 → 不抛 500，收敛为更新对手那条
        assertThat(response.updated()).isTrue();
        assertThat(winner.getEpisode()).isEqualTo(ep3);
        assertThat(winner.getProgressSec()).isEqualTo(8);
    }

    @Test
    void historyKeepsOrderAndSkipsOrphanRecords() {
        WatchProgress healthy = new WatchProgress();
        healthy.setDrama(drama);
        healthy.setEpisode(ep2);
        healthy.setProgressSec(30);
        healthy.setUpdatedAt(LocalDateTime.now());

        // 孤儿记录：drama 行已被级联删除，LAZY 代理访问即抛，真实对象无法注入故用 mock
        WatchProgress orphan = mock(WatchProgress.class);
        when(orphan.getDrama()).thenThrow(EntityNotFoundException.class);

        when(watchProgressRepository.findByUserIdOrderByUpdatedAtDesc(userId))
                .thenReturn(List.of(healthy, orphan));

        List<HistoryItem> items = progressService.history(userId);

        assertThat(items).hasSize(1);
        HistoryItem item = items.get(0);
        assertThat(item.dramaId()).isEqualTo(dramaId);
        assertThat(item.title()).isEqualTo("闪婚老公是豪门");
        assertThat(item.episodeNo()).isEqualTo(2);
        assertThat(item.episodeTitle()).isEqualTo("第2集");
        assertThat(item.progressSec()).isEqualTo(30);
        assertThat(item.updatedAt()).isNotBlank();
    }
}
