package com.hiepnn.live_analytics_counter.repository;

import com.hiepnn.live_analytics_counter.entity.VideoStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface VideoStatsRepository extends JpaRepository<VideoStats, String> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO video_stats (video_id, view_count, like_count, updated_at)
        VALUES (:videoId, :viewDelta, :likeDelta, CURRENT_TIMESTAMP)
        ON CONFLICT (video_id)
        DO UPDATE SET 
            view_count = video_stats.view_count + EXCLUDED.view_count,
            like_count = video_stats.like_count + EXCLUDED.like_count,
            updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertStats(String videoId, long viewDelta, long likeDelta);
}
