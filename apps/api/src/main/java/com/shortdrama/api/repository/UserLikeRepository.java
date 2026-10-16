package com.shortdrama.api.repository;

import com.shortdrama.api.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    boolean existsByUserIdAndDramaId(Long userId, Long dramaId);
}
