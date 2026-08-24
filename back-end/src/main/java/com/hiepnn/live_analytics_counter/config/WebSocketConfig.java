package com.hiepnn.live_analytics_counter.config;

import com.hiepnn.live_analytics_counter.websocket.VideoHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VideoHandler videoHandler;

    public WebSocketConfig(VideoHandler videoHandler) {
        this.videoHandler = videoHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(videoHandler, "/ws/video")
                .setAllowedOrigins("*"); // Just for develope
    }
}
