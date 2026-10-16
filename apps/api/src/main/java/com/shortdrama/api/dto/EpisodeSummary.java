package com.shortdrama.api.dto;

/**
 * 剧集卡片数据：供详情页选集与播放页选集列表。
 */
public record EpisodeSummary(Long id, int episodeNo, String title, String videoUrl, int durationSec) {
}
