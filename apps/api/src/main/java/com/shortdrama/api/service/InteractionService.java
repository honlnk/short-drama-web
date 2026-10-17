package com.shortdrama.api.service;

import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.dto.FavoriteToggleResponse;
import com.shortdrama.api.dto.LikeToggleResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 点赞/收藏编排（阶段3 任务1）：幂等切换 + 我的收藏列表。
 * 注意本类方法不开事务（写库事务在 InteractionTxService，见其类注释）。
 */
@Service
public class InteractionService {

    private final DramaRepository dramaRepository;
    private final UserLikeRepository userLikeRepository;
    private final FavoriteRepository favoriteRepository;
    private final InteractionTxService txService;

    public InteractionService(DramaRepository dramaRepository, UserLikeRepository userLikeRepository,
                              FavoriteRepository favoriteRepository, InteractionTxService txService) {
        this.dramaRepository = dramaRepository;
        this.userLikeRepository = userLikeRepository;
        this.favoriteRepository = favoriteRepository;
        this.txService = txService;
    }

    /** 点赞幂等切换：已赞再调 = 取消。并发双击由 (user_id, drama_id) 唯一索引兜底，冲突按最终状态返回。 */
    public LikeToggleResponse toggleLike(Long dramaId, Long userId) {
        requireOnlineDrama(dramaId);
        if (userLikeRepository.existsByUserIdAndDramaId(userId, dramaId)) {
            return txService.removeLike(userId, dramaId);
        }
        try {
            return txService.addLike(userId, dramaId);
        } catch (DataIntegrityViolationException e) {
            Drama drama = requireOnlineDrama(dramaId);
            return new LikeToggleResponse(true, drama.getLikeCount());
        }
    }

    /** 收藏幂等切换：已藏再调 = 取消。 */
    public FavoriteToggleResponse toggleFavorite(Long dramaId, Long userId) {
        requireOnlineDrama(dramaId);
        if (favoriteRepository.existsByUserIdAndDramaId(userId, dramaId)) {
            return txService.removeFavorite(userId, dramaId);
        }
        try {
            return txService.addFavorite(userId, dramaId);
        } catch (DataIntegrityViolationException e) {
            return new FavoriteToggleResponse(true);
        }
    }

    /** 我的收藏（IN-03）：按收藏时间倒序，复用首页卡片结构。 */
    @Transactional(readOnly = true)
    public List<DramaSummary> listFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> DramaSummary.from(favorite.getDrama()))
                .toList();
    }

    /** 与 C 端详情口径一致：不存在或 OFFLINE 一律 404。 */
    private Drama requireOnlineDrama(Long id) {
        return dramaRepository.findById(id)
                .filter(drama -> drama.getStatus() == DramaStatus.ONLINE)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在或已下架"));
    }
}
