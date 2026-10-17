package com.shortdrama.api.service;

import com.shortdrama.api.dto.AdminDramaRequest;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * 后台短剧管理单测（阶段3 任务3 门禁）：级联删除顺序、封面 URL 白名单、新增字段。
 */
@ExtendWith(MockitoExtension.class)
class AdminDramaServiceTest {

    @Mock
    private DramaRepository dramaRepository;
    @Mock
    private UserLikeRepository userLikeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private WatchProgressRepository watchProgressRepository;
    @InjectMocks
    private AdminDramaService service;

    @Test
    void delete_级联清理顺序_先进度收藏点赞后删剧() {
        Drama drama = new Drama();
        drama.setId(9L);
        when(dramaRepository.findById(9L)).thenReturn(Optional.of(drama));

        service.delete(9L);

        InOrder order = inOrder(watchProgressRepository, favoriteRepository, userLikeRepository, dramaRepository);
        order.verify(watchProgressRepository).deleteByDramaId(9L);
        order.verify(favoriteRepository).deleteByDramaId(9L);
        order.verify(userLikeRepository).deleteByDramaId(9L);
        order.verify(dramaRepository).delete(drama);
    }

    @Test
    void create_封面非本站上传产物_400() {
        AdminDramaRequest request = new AdminDramaRequest(
                "测试剧", "简介", "甜宠", "https://cdn.example.com/cover.jpg", "ONLINE");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("/uploads/");
    }

    @Test
    void create_合法请求_落库字段完整() {
        AdminDramaRequest request = new AdminDramaRequest(
                " 测试剧 ", "简介", "甜宠", "/uploads/files/abc.jpg", "ONLINE");
        when(dramaRepository.save(org.mockito.ArgumentMatchers.any(Drama.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var row = service.create(request);

        ArgumentCaptor<Drama> captor = ArgumentCaptor.forClass(Drama.class);
        org.mockito.Mockito.verify(dramaRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("测试剧");
        assertThat(captor.getValue().getLikeCount()).isZero();
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(row.status()).isEqualTo("ONLINE");
    }
}
