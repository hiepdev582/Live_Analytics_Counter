package com.hiepnn.live_analytics_counter.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class VideoHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Maps videoId -> Set of Web Session
    // Khi có sự kiện tăng view của video_101,
    // hệ thống sẽ gọi videoRooms.get("video_101") 
    // để lấy nhanh danh sách [session_A, session_B] 
    // và bắn tin nhắn cập nhật cho các user
    private final Map<String, Set<WebSocketSession>> videoRooms = new ConcurrentHashMap<>();
    
    // Maps WebSocketSession ID -> videoId they are currently subscribed to
    // Lưu trữ thông tin user này đang xem video nào 
    // (để khi user ngắt kết nối, hệ thống có thể xóa họ ra khỏi room)
    private final Map<String, String> sessionSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received WebSocket message: {}", payload);
        
        try {
            Map<?, ?> data = objectMapper.readValue(payload, Map.class);
            String action = (String) data.get("action");
            String videoId = (String) data.get("video_id");

            if ("subscribe".equalsIgnoreCase(action) && videoId != null) {
                // If session was subscribed to another video, unsubscribe first
                unsubscribeSession(session);

                // Subscribe to new room
                videoRooms.computeIfAbsent(videoId, k -> new CopyOnWriteArraySet<>()).add(session);
                sessionSubscriptions.put(session.getId(), videoId);
                log.info("Session {} subscribed to video room: {}", session.getId(), videoId);

                // Send immediate confirmation
                Map<String, Object> response = Map.of(
                    "type", "subscription_success",
                    "video_id", videoId
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            session.sendMessage(new TextMessage("{\"error\":\"Invalid message format\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {}", session.getId());
        unsubscribeSession(session);
    }

    private void unsubscribeSession(WebSocketSession session) {
        String videoId = sessionSubscriptions.remove(session.getId());
        if (videoId != null) {
            Set<WebSocketSession> room = videoRooms.get(videoId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    videoRooms.remove(videoId);
                }
            }
            log.info("Session {} unsubscribed from video room: {}", session.getId(), videoId);
        }
    }

    public void broadcastToRoom(String videoId, long views, long likes) {
        Set<WebSocketSession> sessions = videoRooms.get(videoId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
            "type", "stats_update",
            "video_id", videoId,
            "views", views,
            "likes", likes
        );

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(payload);
        } catch (IOException e) {
            log.error("Error serializing broadcast payload", e);
            return;
        }

        TextMessage textMessage = new TextMessage(messageJson);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Failed to send message to session " + session.getId(), e);
                }
            }
        }
    }
}
