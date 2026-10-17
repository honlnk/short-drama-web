package com.shortdrama.api.repository;

import com.shortdrama.api.entity.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 观看进度仓储。阶段3 仅用于删除剧/删集时的级联清理（读写进度属里程碑 6）。
 */
public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {

    long deleteByDramaId(Long dramaId);

    long deleteByEpisodeId(Long episodeId);
}
