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
 * 点赞（整部剧，维护 drama.like_count 计数）。(user_id, drama_id) 唯一索引保证幂等。
 * 表名避开保留字 like，用 user_like。
 */
@Entity
@Table(name = "user_like", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "drama_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drama_id")
    private Drama drama;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
