package com.shortdrama.api.service;

import com.shortdrama.api.dto.FavoriteToggleResponse;
import com.shortdrama.api.dto.LikeToggleResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.Favorite;
import com.shortdrama.api.entity.UserLike;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import com.shortdrama.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 互动写库的事务边界（阶段3 任务1）。
 *
 * 独立成 bean 的原因：点赞插入的唯一索引冲突兜底要求「冲突事务干净回滚后，
 * 调用方还能以新事务重读最终状态」——若与编排逻辑同处一个 @Transactional，
 * Spring 会把异常经过的共享事务标记 rollback-only，提交时抛 UnexpectedRollbackException 变 500。
 * 因此 InteractionService 不开事务，每个写操作在这里各自成事务。
 */
@Service
public class InteractionTxService {

    private final DramaRepository dramaRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final FavoriteRepository favoriteRepository;

    public InteractionTxService(DramaRepository dramaRepository, UserRepository userRepository,
                                UserLikeRepository userLikeRepository, FavoriteRepository favoriteRepository) {
        this.dramaRepository = dramaRepository;
        this.userRepository = userRepository;
        this.userLikeRepository = userLikeRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /** 点赞：插记录 + 计数 +1。saveAndFlush 让唯一索引冲突在本事务内即时暴露并整体回滚。 */
    @Transactional
    public LikeToggleResponse addLike(Long userId, Long dramaId) {
        Drama drama = requireDrama(dramaId);
        UserLike like = new UserLike();
        like.setUser(userRepository.getReferenceById(userId));
        like.setDrama(drama);
        like.setCreatedAt(LocalDateTime.now());
        userLikeRepository.saveAndFlush(like);
        drama.setLikeCount(drama.getLikeCount() + 1);
        return new LikeToggleResponse(true, drama.getLikeCount());
    }

    /** 取消点赞：删除记录，按实际删除行数回减计数（并发重复取消不会减成负数，下限 0 双保险）。 */
    @Transactional
    public LikeToggleResponse removeLike(Long userId, Long dramaId) {
        Drama drama = requireDrama(dramaId);
        long removed = userLikeRepository.deleteByUserIdAndDramaId(userId, dramaId);
        if (removed > 0) {
            drama.setLikeCount(Math.max(0, drama.getLikeCount() - 1));
        }
        return new LikeToggleResponse(false, drama.getLikeCount());
    }

    @Transactional
    public FavoriteToggleResponse addFavorite(Long userId, Long dramaId) {
        Favorite favorite = new Favorite();
        favorite.setUser(userRepository.getReferenceById(userId));
        favorite.setDrama(requireDrama(dramaId));
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteRepository.saveAndFlush(favorite);
        return new FavoriteToggleResponse(true);
    }

    @Transactional
    public FavoriteToggleResponse removeFavorite(Long userId, Long dramaId) {
        favoriteRepository.deleteByUserIdAndDramaId(userId, dramaId);
        return new FavoriteToggleResponse(false);
    }

    private Drama requireDrama(Long id) {
        return dramaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在或已下架"));
    }
}
