package com.shortdrama.api.service;

import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.dto.FavoriteToggleResponse;
import com.shortdrama.api.dto.LikeToggleResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.entity.Favorite;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 互动切换编排逻辑单测（阶段3 任务1）：切换方向、唯一索引冲突兜底、OFFLINE 404、收藏列表映射。
 * 写库事务边界 InteractionTxService 单独在 InteractionTxServiceTest 覆盖。
 */
@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private DramaRepository dramaRepository;
    @Mock
    private UserLikeRepository userLikeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private InteractionTxService txService;
    @InjectMocks
    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        Drama onlineDrama = new Drama();
        onlineDrama.setId(1L);
        onlineDrama.setLikeCount(101);
        onlineDrama.setStatus(DramaStatus.ONLINE);
        lenient().when(dramaRepository.findById(1L)).thenReturn(Optional.of(onlineDrama));
    }

    @Test
    void toggleLike_未点赞时走点赞路径() {
        when(userLikeRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(false);
        when(txService.addLike(2L, 1L)).thenReturn(new LikeToggleResponse(true, 101));

        LikeToggleResponse response = interactionService.toggleLike(1L, 2L);

        assertThat(response.liked()).isTrue();
        verify(txService).addLike(2L, 1L);
        verify(txService, never()).removeLike(any(), any());
    }

    @Test
    void toggleLike_已点赞时走取消路径() {
        when(userLikeRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(true);
        when(txService.removeLike(2L, 1L)).thenReturn(new LikeToggleResponse(false, 100));

        LikeToggleResponse response = interactionService.toggleLike(1L, 2L);

        assertThat(response.liked()).isFalse();
        verify(txService).removeLike(2L, 1L);
        verify(txService, never()).addLike(any(), any());
    }

    @Test
    void toggleLike_并发双击触发唯一索引冲突时按最终状态返回不抛500() {
        when(userLikeRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(false);
        // 另一并发请求已抢先插入：本请求 addLike 撞唯一索引回滚，重读时返回最终状态
        when(txService.addLike(2L, 1L)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        LikeToggleResponse response = interactionService.toggleLike(1L, 2L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(101);
    }

    @Test
    void toggleLike_下架剧一律404() {
        Drama offline = new Drama();
        offline.setId(9L);
        offline.setStatus(DramaStatus.OFFLINE);
        offline.setLikeCount(0);
        when(dramaRepository.findById(9L)).thenReturn(Optional.of(offline));

        assertThatThrownBy(() -> interactionService.toggleLike(9L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("短剧不存在或已下架");
    }

    @Test
    void toggleFavorite_往返切换() {
        when(favoriteRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(false);
        when(txService.addFavorite(2L, 1L)).thenReturn(new FavoriteToggleResponse(true));
        assertThat(interactionService.toggleFavorite(1L, 2L).favorited()).isTrue();

        when(favoriteRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(true);
        when(txService.removeFavorite(2L, 1L)).thenReturn(new FavoriteToggleResponse(false));
        assertThat(interactionService.toggleFavorite(1L, 2L).favorited()).isFalse();
    }

    @Test
    void toggleFavorite_并发双击冲突兜底() {
        when(favoriteRepository.existsByUserIdAndDramaId(2L, 1L)).thenReturn(false);
        when(txService.addFavorite(2L, 1L)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThat(interactionService.toggleFavorite(1L, 2L).favorited()).isTrue();
    }

    @Test
    void listFavorites_映射为DramaSummary() {
        Drama drama = new Drama();
        drama.setId(1L);
        drama.setTitle("闪婚老公是豪门");
        drama.setCategory("甜宠");
        drama.setStatus(DramaStatus.ONLINE);
        drama.setLikeCount(101);
        Favorite favorite = new Favorite();
        favorite.setDrama(drama);
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(favorite));

        List<DramaSummary> result = interactionService.listFavorites(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).title()).isEqualTo("闪婚老公是豪门");
        assertThat(result.get(0).status()).isEqualTo("ONLINE");
    }
}
