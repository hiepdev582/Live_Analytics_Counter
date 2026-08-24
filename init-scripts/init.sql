CREATE TABLE IF NOT EXISTS videos (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS video_stats (
    video_id VARCHAR(64) PRIMARY KEY REFERENCES videos(id) ON DELETE CASCADE,
    view_count BIGINT DEFAULT 0 NOT NULL,
    like_count BIGINT DEFAULT 0 NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Dữ liệu mẫu
INSERT INTO videos (id, title) VALUES 
('video_101', 'Hướng dẫn Spring Boot Microservices'),
('video_102', 'Kiến trúc Kafka & Redis Write-Back')
ON CONFLICT (id) DO NOTHING;

INSERT INTO video_stats (video_id, view_count, like_count) VALUES 
('video_101', 1000, 50),
('video_102', 500, 20)
ON CONFLICT (video_id) DO NOTHING;