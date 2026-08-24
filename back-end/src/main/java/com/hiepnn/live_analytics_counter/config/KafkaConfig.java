package com.hiepnn.live_analytics_counter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String VIDEO_EVENTS_TOPIC = "video-events";

    @Bean
    public NewTopic videoEventsTopic() {
        return TopicBuilder.name(VIDEO_EVENTS_TOPIC)
                .partitions(3) // Khả năng mở rộng quy mô xử lý
                .replicas(1) // Khả năng chịu lỗi - Just for develope
                .build();
    }
}
