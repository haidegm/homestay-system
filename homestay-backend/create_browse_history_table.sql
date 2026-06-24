-- 创建浏览记录表
CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    house_id BIGINT NOT NULL COMMENT '房源ID',
    browse_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    browse_duration INT DEFAULT 0 COMMENT '浏览时长（秒）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_house_id (house_id),
    INDEX idx_browse_time (browse_time),
    UNIQUE KEY uk_user_house_time (user_id, house_id, browse_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览记录表';
