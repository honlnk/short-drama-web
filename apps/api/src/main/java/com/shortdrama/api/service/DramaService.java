package com.shortdrama.api.service;

import com.shortdrama.api.dto.DramaSummary;
import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.repository.DramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DramaService {

    private final DramaRepository dramaRepository;

    public DramaService(DramaRepository dramaRepository) {
        this.dramaRepository = dramaRepository;
    }

    @Transactional(readOnly = true)
    public List<DramaSummary> listOnline() {
        return dramaRepository.findByStatusOrderByLikeCountDesc(DramaStatus.ONLINE).stream()
                .map(DramaService::toSummary)
                .toList();
    }

    private static DramaSummary toSummary(Drama drama) {
        return new DramaSummary(
                drama.getId(),
                drama.getTitle(),
                drama.getDescription(),
                drama.getCategory(),
                drama.getCoverUrl(),
                drama.getLikeCount(),
                drama.getStatus().name(),
                drama.getEpisodes().size()
        );
    }
}
