package com.shortdrama.api.service;

import com.shortdrama.api.dto.LikeToggleResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.entity.User;
import com.shortdrama.api.entity.UserLike;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import com.shortdrama.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 互动写库事务边界单测（阶段3 任务1）：计数加减算术、按删除行数回减、下限 0、落库字段完整。
 */
@ExtendWith(MockitoExtension.class)
class InteractionTxServiceTest {

    @Mock
    private DramaRepository dramaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserLikeRepository userLikeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @InjectMocks
    private InteractionTxService txService;

    private final Drama drama = new Drama();

    @BeforeEach
    void setUp() {
        drama.setId(1L);
        drama.setLikeCount(100);
        drama.setStatus(DramaStatus.ONLINE);
        when(dramaRepository.findById(1L)).thenReturn(Optional.of(drama));
    }

    @Test
    void addLike_插入记录并计数加一() {
        User user = new User();
        user.setId(2L);
        user.setUsername("demo");
        when(userRepository.getReferenceById(2L)).thenReturn(user);

        LikeToggleResponse response = txService.addLike(2L, 1L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(101);
        ArgumentCaptor<UserLike> captor = ArgumentCaptor.forClass(UserLike.class);
        verify(userLikeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getDrama()).isEqualTo(drama);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void removeLike_删除命中时计数减一() {
        when(userLikeRepository.deleteByUserIdAndDramaId(2L, 1L)).thenReturn(1L);

        LikeToggleResponse response = txService.removeLike(2L, 1L);

        assertThat(response.liked()).isFalse();
        assertThat(response.likeCount()).isEqualTo(99);
        assertThat(drama.getLikeCount()).isEqualTo(99);
    }

    @Test
    void removeLike_并发重复取消未删到行时不回减() {
        when(userLikeRepository.deleteByUserIdAndDramaId(2L, 1L)).thenReturn(0L);

        LikeToggleResponse response = txService.removeLike(2L, 1L);

        assertThat(response.liked()).isFalse();
        assertThat(drama.getLikeCount()).isEqualTo(100);
    }

    @Test
    void removeLike_计数下限为0() {
        drama.setLikeCount(0);
        when(userLikeRepository.deleteByUserIdAndDramaId(2L, 1L)).thenReturn(1L);

        LikeToggleResponse response = txService.removeLike(2L, 1L);

        assertThat(response.likeCount()).isEqualTo(0);
        assertThat(drama.getLikeCount()).isEqualTo(0);
    }
}
