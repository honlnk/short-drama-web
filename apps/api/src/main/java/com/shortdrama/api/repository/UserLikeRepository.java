package com.shortdrama.api.repository;

import com.shortdrama.api.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    boolean existsByUserIdAndDramaId(Long userId, Long dramaId);

    /** 取消点赞：返回实际删除行数，调用方据此判断是否回减计数（并发重复取消不重复减）。 */
    long deleteByUserIdAndDramaId(Long userId, Long dramaId);

    /** 后台删除剧时级联清理（仅数据，不动磁盘文件）。 */
    long deleteByDramaId(Long dramaId);
}
