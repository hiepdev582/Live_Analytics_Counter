package com.hiepnn.live_analytics_counter.service;

import com.hiepnn.live_analytics_counter.repository.VideoStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class WriteBackScheduler {

    private final StringRedisTemplate redisTemplate;
    private final VideoStatsRepository videoStatsRepository;

    public WriteBackScheduler(StringRedisTemplate redisTemplate, VideoStatsRepository videoStatsRepository) {
        this.redisTemplate = redisTemplate;
        this.videoStatsRepository = videoStatsRepository;
    }

    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    public void syncRedisToPostgres() {
        log.info("Starting scheduled write-back task...");
        try {
            // SPOP up to 100 dirty video IDs
            List<String> dirtyVideoIds = redisTemplate.opsForSet().pop("dirty_videos", 100);
            if (dirtyVideoIds == null || dirtyVideoIds.isEmpty()) {
                log.info("No dirty videos to sync.");
                return;
            }

            log.info("Found {} dirty videos to sync: {}", dirtyVideoIds.size(), dirtyVideoIds);

            for (String videoId : dirtyVideoIds) {
                // Get and reset views delta
                String viewsDeltaStr = redisTemplate.opsForValue().getAndSet("video:" + videoId + ":views:delta", "0");
                String likesDeltaStr = redisTemplate.opsForValue().getAndSet("video:" + videoId + ":likes:delta", "0");

                long viewsDelta = 0;
                long likesDelta = 0;

                if (viewsDeltaStr != null) {
                    viewsDelta = Long.parseLong(viewsDeltaStr);
                }
                if (likesDeltaStr != null) {
                    likesDelta = Long.parseLong(likesDeltaStr);
                }

                if (viewsDelta > 0 || likesDelta > 0) {
                    log.info("Syncing video {} to PostgreSQL: viewsDelta={}, likesDelta={}", videoId, viewsDelta, likesDelta);
                    videoStatsRepository.upsertStats(videoId, viewsDelta, likesDelta);
                }
            }
            log.info("Write-back task completed successfully.");
        } catch (Exception e) {
            log.error("Error running write-back scheduled task", e);
        }
    }
}
