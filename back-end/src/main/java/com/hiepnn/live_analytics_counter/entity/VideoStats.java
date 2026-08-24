package com.hiepnn.live_analytics_counter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "video_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStats {
    @Id
    @Column(name = "video_id", length = 64)
    private String videoId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
