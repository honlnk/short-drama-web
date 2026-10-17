package com.shortdrama.api.repository;

import com.shortdrama.api.entity.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 观看进度仓储。读写属阶段4（PL-04/05：续播定位、历史倒序）；
 * deleteByDramaId/deleteByEpisodeId 供阶段3 管理端删剧/删集时级联清理。
 */
public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {

    /** 续播/上报定位：每用户每部剧最多一条（唯一索引保证）。 */
    Optional<WatchProgress> findByUserIdAndDramaId(Long userId, Long dramaId);

    /** 观看历史：最近观看在前。 */
    List<WatchProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long deleteByDramaId(Long dramaId);

    long deleteByEpisodeId(Long episodeId);
}
