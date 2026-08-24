package com.hiepnn.live_analytics_counter.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEvent {
    private String videoId;
    private String eventType; // "VIEW" or "LIKE"
    private long timestamp;
}
