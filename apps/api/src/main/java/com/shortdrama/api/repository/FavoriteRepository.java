package com.shortdrama.api.repository;

import com.shortdrama.api.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndDramaId(Long userId, Long dramaId);
}
