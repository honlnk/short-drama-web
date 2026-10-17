package com.shortdrama.api.repository;

import com.shortdrama.api.entity.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndDramaId(Long userId, Long dramaId);

    /** 我的收藏：按收藏时间倒序，带出剧与剧集避免 N+1。 */
    @EntityGraph(attributePaths = {"drama", "drama.episodes"})
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    long deleteByUserIdAndDramaId(Long userId, Long dramaId);

    /** 后台删除剧时级联清理（仅数据，不动磁盘文件）。 */
    long deleteByDramaId(Long dramaId);
}
