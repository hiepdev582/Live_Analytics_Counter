package com.hiepnn.live_analytics_counter.service;

import com.hiepnn.live_analytics_counter.entity.Video;
import com.hiepnn.live_analytics_counter.entity.VideoStats;
import com.hiepnn.live_analytics_counter.repository.VideoRepository;
import com.hiepnn.live_analytics_counter.repository.VideoStatsRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoStatsService {

    private final VideoRepository videoRepository;
    private final VideoStatsRepository videoStatsRepository;
    private final StringRedisTemplate redisTemplate;

    public VideoStatsService(VideoRepository videoRepository,
                             VideoStatsRepository videoStatsRepository,
                             StringRedisTemplate redisTemplate) {
        this.videoRepository = videoRepository;
        this.videoStatsRepository = videoStatsRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<Map<String, Object>> getVideosWithStats() {
        List<Video> videos = videoRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Video video : videos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", video.getId());
            map.put("title", video.getTitle());
            map.put("createdAt", video.getCreatedAt());

            long views = getOrInitStat(video.getId(), "views");
            long likes = getOrInitStat(video.getId(), "likes");

            map.put("views", views);
            map.put("likes", likes);
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> getVideoStats(String videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return null;
        }
        long views = getOrInitStat(videoId, "views");
        long likes = getOrInitStat(videoId, "likes");

        return Map.of(
                "id", video.getId(),
                "title", video.getTitle(),
                "views", views,
                "likes", likes
        );
    }

    private long getOrInitStat(String videoId, String statType) {
        String key = "video:" + videoId + ":" + statType;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            return Long.parseLong(val);
        }

        // Fallback to database
        VideoStats stats = videoStatsRepository.findById(videoId).orElse(null);
        long dbValue = 0;
        if (stats != null) {
            if ("views".equals(statType)) {
                dbValue = stats.getViewCount();
            } else if ("likes".equals(statType)) {
                dbValue = stats.getLikeCount();
            }
        }

        // SETNX: set if absent to prevent race conditions
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(dbValue));
        
        // Return latest value
        String finalVal = redisTemplate.opsForValue().get(key);
        return finalVal != null ? Long.parseLong(finalVal) : dbValue;
    }
}
