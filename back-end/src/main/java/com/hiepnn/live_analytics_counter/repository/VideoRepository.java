package com.hiepnn.live_analytics_counter.repository;

import com.hiepnn.live_analytics_counter.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, String> {
}
