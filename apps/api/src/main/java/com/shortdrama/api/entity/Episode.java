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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "episode")
@Getter
@Setter
@NoArgsConstructor
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drama_id")
    private Drama drama;

    @Column(name = "episode_no", nullable = false)
    private int episodeNo;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "video_url", nullable = false, length = 255)
    private String videoUrl;

    @Column(name = "duration_sec", nullable = false)
    private int durationSec;
}
