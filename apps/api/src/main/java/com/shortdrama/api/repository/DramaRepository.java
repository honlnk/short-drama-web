package com.shortdrama.api.repository;

import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DramaRepository extends JpaRepository<Drama, Long> {

    /** C 端推荐流：上架剧按点赞数倒序，一次性带出剧集避免 N+1。 */
    @EntityGraph(attributePaths = "episodes")
    List<Drama> findByStatusOrderByLikeCountDesc(DramaStatus status);
}
