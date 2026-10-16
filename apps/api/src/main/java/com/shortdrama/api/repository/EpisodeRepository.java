package com.shortdrama.api.repository;

import com.shortdrama.api.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    /** 详情/播放页选集列表：按集号升序。 */
    List<Episode> findByDramaIdOrderByEpisodeNoAsc(Long dramaId);
}
