package com.hiepnn.live_analytics_counter.controller;

import com.hiepnn.live_analytics_counter.service.KafkaEventProducer;
import com.hiepnn.live_analytics_counter.service.VideoStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*") // Just for develop
public class VideoController {

    private final VideoStatsService videoStatsService;
    private final KafkaEventProducer kafkaEventProducer;

    public VideoController(VideoStatsService videoStatsService, KafkaEventProducer kafkaEventProducer) {
        this.videoStatsService = videoStatsService;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllVideos() {
        return ResponseEntity.ok(videoStatsService.getVideosWithStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVideoById(@PathVariable String id) {
        Map<String, Object> stats = videoStatsService.getVideoStats(id);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, String>> triggerView(@PathVariable String id) {
        kafkaEventProducer.sendEvent(id, "VIEW");
        return ResponseEntity.ok(Map.of("message", "View event queued"));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, String>> triggerLike(@PathVariable String id) {
        kafkaEventProducer.sendEvent(id, "LIKE");
        return ResponseEntity.ok(Map.of("message", "Like event queued"));
    }
}
