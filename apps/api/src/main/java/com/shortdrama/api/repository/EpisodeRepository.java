package com.shortdrama.api.repository;

import com.shortdrama.api.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    /** 详情/播放页选集列表：按集号升序。 */
    List<Episode> findByDramaIdOrderByEpisodeNoAsc(Long dramaId);

    /** 后台新增/编辑集数的重复校验（另有 (drama_id, episode_no) 唯一索引兜底）。 */
    boolean existsByDramaIdAndEpisodeNo(Long dramaId, int episodeNo);
}
