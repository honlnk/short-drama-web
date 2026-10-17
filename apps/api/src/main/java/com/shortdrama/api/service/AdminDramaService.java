package com.shortdrama.api.service;

import com.shortdrama.api.dto.AdminDramaDetail;
import com.shortdrama.api.dto.AdminDramaRequest;
import com.shortdrama.api.dto.AdminDramaRow;
import com.shortdrama.api.dto.PageResponse;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.exception.BusinessException;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.FavoriteRepository;
import com.shortdrama.api.repository.UserLikeRepository;
import com.shortdrama.api.repository.WatchProgressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 后台短剧管理（AD-01~04，阶段3 任务3）。删除走数据级联清理、磁盘文件保留（决策口径）。
 */
@Service
public class AdminDramaService {

    private final DramaRepository dramaRepository;
    private final UserLikeRepository userLikeRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchProgressRepository watchProgressRepository;

    public AdminDramaService(DramaRepository dramaRepository, UserLikeRepository userLikeRepository,
                             FavoriteRepository favoriteRepository, WatchProgressRepository watchProgressRepository) {
        this.dramaRepository = dramaRepository;
        this.userLikeRepository = userLikeRepository;
        this.favoriteRepository = favoriteRepository;
        this.watchProgressRepository = watchProgressRepository;
    }

    /** 分页列表（含 OFFLINE），剧名模糊检索，id 倒序（新剧在前）。 */
    @Transactional(readOnly = true)
    public PageResponse<AdminDramaRow> list(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<Drama> result = (keyword == null || keyword.isBlank())
                ? dramaRepository.findAll(pageable)
                : dramaRepository.findByTitleContaining(keyword.trim(), pageable);
        return new PageResponse<>(
                result.getContent().stream().map(AdminDramaRow::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    /** 编辑回填用单查（含 OFFLINE）。 */
    @Transactional(readOnly = true)
    public AdminDramaDetail getDetail(Long id) {
        return AdminDramaDetail.from(requireDrama(id));
    }

    @Transactional
    public AdminDramaRow create(AdminDramaRequest request) {
        Drama drama = new Drama();
        applyRequest(drama, request);
        drama.setLikeCount(0);
        drama.setCreatedAt(LocalDateTime.now());
        return AdminDramaRow.from(dramaRepository.save(drama));
    }

    @Transactional
    public AdminDramaRow update(Long id, AdminDramaRequest request) {
        Drama drama = requireDrama(id);
        applyRequest(drama, request);
        return AdminDramaRow.from(drama);
    }

    /**
     * 级联删除（AD-04）：同事务清 watch_progress → favorite → user_like → drama（cascade 级联 episodes）。
     * 被引用的媒体文件不删磁盘（种子素材被多集共享，仅解引用最安全）。
     */
    @Transactional
    public void delete(Long id) {
        Drama drama = requireDrama(id);
        watchProgressRepository.deleteByDramaId(id);
        favoriteRepository.deleteByDramaId(id);
        userLikeRepository.deleteByDramaId(id);
        dramaRepository.delete(drama);
    }

    private void applyRequest(Drama drama, AdminDramaRequest request) {
        if (request.coverUrl() == null || !request.coverUrl().startsWith("/uploads/")) {
            throw new BusinessException(400, "封面地址必须为本站上传产物（/uploads/ 开头）");
        }
        drama.setTitle(request.title().trim());
        drama.setDescription(request.description() == null ? "" : request.description().trim());
        drama.setCategory(request.category().trim());
        drama.setCoverUrl(request.coverUrl().trim());
        drama.setStatus(DramaStatus.valueOf(request.status()));
    }

    private Drama requireDrama(Long id) {
        return dramaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "短剧不存在"));
    }
}
