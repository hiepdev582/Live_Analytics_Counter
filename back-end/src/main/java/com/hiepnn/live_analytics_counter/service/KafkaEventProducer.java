package com.hiepnn.live_analytics_counter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiepnn.live_analytics_counter.config.KafkaConfig;
import com.hiepnn.live_analytics_counter.model.VideoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String videoId, String eventType) {
        VideoEvent event = VideoEvent.builder()
                .videoId(videoId)
                .eventType(eventType)
                .timestamp(System.currentTimeMillis())
                .build();
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Producing event to Kafka: {}", message);
            kafkaTemplate.send(KafkaConfig.VIDEO_EVENTS_TOPIC, videoId, message);
        } catch (Exception e) {
            log.error("Failed to serialize or send event to Kafka", e);
        }
    }
}
