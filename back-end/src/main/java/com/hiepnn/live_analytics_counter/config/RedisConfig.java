package com.hiepnn.live_analytics_counter.config;

import com.hiepnn.live_analytics_counter.websocket.VideoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Configuration
public class RedisConfig {

    public static final String VIDEO_CHANNEL = "video_channel";

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(VIDEO_CHANNEL));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Component
    public static class RedisMessageReceiver {
        private final VideoHandler videoHandler;

        public RedisMessageReceiver(VideoHandler videoHandler) {
            this.videoHandler = videoHandler;
        }

        public void receiveMessage(String message) {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            // Expected format: videoId:views:likes
            String[] parts = message.split(":");
            if (parts.length >= 3) {
                String videoId = parts[0];
                try {
                    long views = Long.parseLong(parts[1]);
                    long likes = Long.parseLong(parts[2]);
                    videoHandler.broadcastToRoom(videoId, views, likes);
                } catch (NumberFormatException e) {
                    // Ignore malformed payloads
                }
            }
        }
    }
}
