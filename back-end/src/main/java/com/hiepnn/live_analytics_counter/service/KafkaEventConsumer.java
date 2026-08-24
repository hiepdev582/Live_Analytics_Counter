package com.hiepnn.live_analytics_counter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiepnn.live_analytics_counter.config.KafkaConfig;
import com.hiepnn.live_analytics_counter.config.RedisConfig;
import com.hiepnn.live_analytics_counter.model.VideoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaEventConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaEventConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = KafkaConfig.VIDEO_EVENTS_TOPIC, groupId = "analytics-group")
    public void consume(String message) {
        log.info("Consuming event from Kafka: {}", message);
        try {
            VideoEvent event = objectMapper.readValue(message, VideoEvent.class);
            String videoId = event.getVideoId();
            String eventType = event.getEventType();

            String mainKey;
            String deltaKey;

            if ("VIEW".equalsIgnoreCase(eventType)) {
                mainKey = "video:" + videoId + ":views";
                deltaKey = "video:" + videoId + ":views:delta";
            } else if ("LIKE".equalsIgnoreCase(eventType)) {
                mainKey = "video:" + videoId + ":likes";
                deltaKey = "video:" + videoId + ":likes:delta";
            } else {
                log.warn("Unknown event type: {}", eventType);
                return;
            }

            // 1. Atomically increment stats in Redis
            redisTemplate.opsForValue().increment(mainKey);
            redisTemplate.opsForValue().increment(deltaKey);

            // 2. Add videoId to set of dirty videos for sync
            redisTemplate.opsForSet().add("dirty_videos", videoId);

            // 3. Read current cumulative views and likes to broadcast
            String currentViewsStr = redisTemplate.opsForValue().get("video:" + videoId + ":views");
            String currentLikesStr = redisTemplate.opsForValue().get("video:" + videoId + ":likes");

            long currentViews = currentViewsStr != null ? Long.parseLong(currentViewsStr) : 0;
            long currentLikes = currentLikesStr != null ? Long.parseLong(currentLikesStr) : 0;

            // 4. Publish to Redis Pub/Sub channel
            String pubSubPayload = videoId + ":" + currentViews + ":" + currentLikes;
            redisTemplate.convertAndSend(RedisConfig.VIDEO_CHANNEL, pubSubPayload);
            log.info("Published to Redis Pub/Sub: {}", pubSubPayload);

        } catch (Exception e) {
            log.error("Error processing consumed event", e);
        }
    }
}
