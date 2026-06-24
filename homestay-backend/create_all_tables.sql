-- 民宿预订平台数据库建表SQL
-- 数据库：homestay_db
-- 字符集：UTF-8

-- 创建数据库
CREATE DATABASE IF NOT EXISTS homestay_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE homestay_db;

-- ============================================
-- 1. 用户表 (User)
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户, HOST-房东, ADMIN-管理员',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常, INACTIVE-禁用',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (`username`),
    INDEX idx_role (`role`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 房源表 (House)
-- ============================================
CREATE TABLE IF NOT EXISTS `house` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房源ID',
    `host_id` BIGINT NOT NULL COMMENT '房东ID（外键关联user表）',
    `title` VARCHAR(200) NOT NULL COMMENT '房源标题',
    `description` TEXT COMMENT '房源描述',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `address` VARCHAR(255) COMMENT '详细地址',
    `max_guests` INT DEFAULT 1 COMMENT '最大入住人数',
    `area` INT COMMENT '面积（平方米）',
    `bed_count` INT DEFAULT 1 COMMENT '床位数',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '每晚价格',
    `bathroom_desc` VARCHAR(100) COMMENT '卫生间描述',
    `supply_desc` VARCHAR(500) COMMENT '设施描述',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核, ACTIVE-上架, INACTIVE-下架',
    `latitude` DECIMAL(10, 7) COMMENT '纬度',
    `longitude` DECIMAL(10, 7) COMMENT '经度',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_host_id (`host_id`),
    INDEX idx_city (`city`),
    INDEX idx_status (`status`),
    INDEX idx_price (`price`),
    FOREIGN KEY (`host_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源表';

-- ============================================
-- 3. 订单表 (Order)
-- ============================================
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号（唯一）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    `house_id` BIGINT NOT NULL COMMENT '房源ID（外键关联house表）',
    `check_in_date` DATE NOT NULL COMMENT '入住日期',
    `check_out_date` DATE NOT NULL COMMENT '退房日期',
    `guest_count` INT DEFAULT 1 COMMENT '入住人数',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '订单总价',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待确认, CONFIRMED-已确认, CANCELLED-已取消, COMPLETED-已完成',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_house_id (`house_id`),
    INDEX idx_status (`status`),
    INDEX idx_check_in_date (`check_in_date`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 4. 评价表 (Review)
-- ============================================
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID（外键关联order表）',
    `house_id` BIGINT NOT NULL COMMENT '房源ID（外键关联house表）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    `rating` DOUBLE NOT NULL COMMENT '评分（1-5分，支持0.5分）',
    `comment` TEXT COMMENT '评论内容',
    `reply` TEXT COMMENT '房东回复',
    `is_anonymous` BOOLEAN DEFAULT FALSE COMMENT '是否匿名',
    `images` TEXT COMMENT '评价图片URL（多张用逗号分隔）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (`order_id`),
    INDEX idx_house_id (`house_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_rating (`rating`),
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================
-- 5. 心愿单表 (Wishlist)
-- ============================================
CREATE TABLE IF NOT EXISTS `wishlist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '心愿单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    `house_id` BIGINT NOT NULL COMMENT '房源ID（外键关联house表）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_house (`user_id`, `house_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_house_id (`house_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿单表';

-- ============================================
-- 6. 浏览历史表 (BrowseHistory)
-- ============================================
CREATE TABLE IF NOT EXISTS `browse_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '浏览历史ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    `house_id` BIGINT NOT NULL COMMENT '房源ID（外键关联house表）',
    `browse_time` DATETIME NOT NULL COMMENT '浏览时间',
    `browse_duration` INT DEFAULT 0 COMMENT '浏览时长（秒）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_house_id (`house_id`),
    INDEX idx_browse_time (`browse_time`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

-- ============================================
-- 7. 房源图片表 (HouseImage)
-- ============================================
CREATE TABLE IF NOT EXISTS `house_image` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    `house_id` BIGINT NOT NULL COMMENT '房源ID（外键关联house表）',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_house_id (`house_id`),
    INDEX idx_sort_order (`sort_order`),
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源图片表';

-- ============================================
-- 8. 用户资料表 (UserProfile)
-- ============================================
CREATE TABLE IF NOT EXISTS `user_profile` (
    `user_id` BIGINT PRIMARY KEY COMMENT '用户ID（主键，外键关联user表）',
    `bio` VARCHAR(500) COMMENT '个人简介',
    `profession` VARCHAR(100) COMMENT '职业',
    `target_location` VARCHAR(100) COMMENT '目标旅行地',
    `fun_fact` VARCHAR(200) COMMENT '趣事',
    `pets` VARCHAR(100) COMMENT '宠物',
    `born_year` VARCHAR(10) COMMENT '出生年份',
    `school` VARCHAR(100) COMMENT '学校',
    `useless_skill` VARCHAR(200) COMMENT '无用技能',
    `waste_time` VARCHAR(200) COMMENT '消磨时间方式',
    `fav_song` VARCHAR(200) COMMENT '最爱歌曲',
    `languages` VARCHAR(200) COMMENT '语言',
    `selected_places` TEXT COMMENT '去过的地方（JSON格式）',
    `show_places` BOOLEAN DEFAULT TRUE COMMENT '是否展示去过的地方',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- ============================================
-- 9. 会话表 (Conversation)
-- ============================================
CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    `host_id` BIGINT NOT NULL COMMENT '房东ID（外键关联user表）',
    `house_id` BIGINT COMMENT '房源ID（外键关联house表）',
    `order_id` BIGINT COMMENT '订单ID（外键关联order表）',
    `last_message` TEXT COMMENT '最后一条消息',
    `last_message_time` DATETIME COMMENT '最后消息时间',
    `unread_count_user` INT DEFAULT 0 COMMENT '用户未读消息数',
    `unread_count_host` INT DEFAULT 0 COMMENT '房东未读消息数',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_host_id (`host_id`),
    INDEX idx_house_id (`house_id`),
    INDEX idx_order_id (`order_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`host_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`house_id`) REFERENCES `house`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ============================================
-- 10. 消息表 (Message)
-- ============================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID（外键关联conversation表）',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID（外键关联user表）',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID（外键关联user表）',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (`conversation_id`),
    INDEX idx_sender_id (`sender_id`),
    INDEX idx_receiver_id (`receiver_id`),
    INDEX idx_is_read (`is_read`),
    FOREIGN KEY (`conversation_id`) REFERENCES `conversation`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ============================================
-- 插入测试数据（可选）
-- ============================================

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `role`, `nickname`, `phone`, `status`) VALUES
('host1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ROLE_HOST', '房东张三', '13800000001', 'ACTIVE'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ROLE_USER', '用户李四', '13800000002', 'ACTIVE');
-- 管理员账号由 DataInitializer 自动创建（admin/123）

-- 插入测试房源
INSERT INTO `house` (`host_id`, `title`, `description`, `province`, `city`, `address`, `max_guests`, `area`, `bed_count`, `price`, `status`) VALUES
(2, '西湖边温馨民宿', '位于西湖景区，步行5分钟到湖边，环境优美，设施齐全', '浙江省', '杭州', '西湖区灵隐路88号', 4, 80, 2, 388.00, 'ACTIVE'),
(2, '成都宽窄巷子特色民宿', '紧邻宽窄巷子，体验地道成都生活', '四川省', '成都', '青羊区宽窄巷子12号', 3, 60, 1, 268.00, 'ACTIVE');

-- 完成
SELECT '数据库表创建完成！' AS message;
