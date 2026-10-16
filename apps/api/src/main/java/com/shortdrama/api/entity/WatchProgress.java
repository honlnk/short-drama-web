package com.shortdrama.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 观看进度。每用户每部剧只保留最新一条：(user_id, drama_id) 唯一索引。
 */
@Entity
@Table(name = "watch_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "drama_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WatchProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drama_id")
    private Drama drama;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @Column(name = "progress_sec", nullable = false)
    private int progressSec;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
