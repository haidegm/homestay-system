-- 为平台增长趋势图生成测试用户和房东数据
-- 这个脚本会为最近12个月生成用户和房东注册数据

-- 注意：密码使用BCrypt加密，这里使用明文"123456"的加密结果
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6

-- 2025年5月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202505001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '五月用户1', 'ROLE_USER', 'ACTIVE', '2025-05-10 10:00:00', '2025-05-10 10:00:00'),
('user202505002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '五月用户2', 'ROLE_USER', 'ACTIVE', '2025-05-15 14:30:00', '2025-05-15 14:30:00'),
('host202505001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '五月房东1', 'ROLE_HOST', 'ACTIVE', '2025-05-20 09:00:00', '2025-05-20 09:00:00');

-- 2025年6月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202506001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '六月用户1', 'ROLE_USER', 'ACTIVE', '2025-06-05 11:00:00', '2025-06-05 11:00:00'),
('user202506002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '六月用户2', 'ROLE_USER', 'ACTIVE', '2025-06-12 15:30:00', '2025-06-12 15:30:00'),
('user202506003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '六月用户3', 'ROLE_USER', 'ACTIVE', '2025-06-20 10:00:00', '2025-06-20 10:00:00'),
('host202506001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '六月房东1', 'ROLE_HOST', 'ACTIVE', '2025-06-25 14:00:00', '2025-06-25 14:00:00');

-- 2025年7月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202507001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '七月用户1', 'ROLE_USER', 'ACTIVE', '2025-07-03 09:30:00', '2025-07-03 09:30:00'),
('user202507002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '七月用户2', 'ROLE_USER', 'ACTIVE', '2025-07-10 13:00:00', '2025-07-10 13:00:00'),
('user202507003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '七月用户3', 'ROLE_USER', 'ACTIVE', '2025-07-18 16:30:00', '2025-07-18 16:30:00'),
('user202507004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '七月用户4', 'ROLE_USER', 'ACTIVE', '2025-07-25 11:00:00', '2025-07-25 11:00:00'),
('host202507001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '七月房东1', 'ROLE_HOST', 'ACTIVE', '2025-07-28 15:00:00', '2025-07-28 15:00:00');

-- 2025年8月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202508001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月用户1', 'ROLE_USER', 'ACTIVE', '2025-08-02 10:00:00', '2025-08-02 10:00:00'),
('user202508002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月用户2', 'ROLE_USER', 'ACTIVE', '2025-08-08 12:30:00', '2025-08-08 12:30:00'),
('user202508003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月用户3', 'ROLE_USER', 'ACTIVE', '2025-08-15 14:00:00', '2025-08-15 14:00:00'),
('user202508004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月用户4', 'ROLE_USER', 'ACTIVE', '2025-08-22 16:30:00', '2025-08-22 16:30:00'),
('user202508005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月用户5', 'ROLE_USER', 'ACTIVE', '2025-08-28 11:00:00', '2025-08-28 11:00:00'),
('host202508001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '八月房东1', 'ROLE_HOST', 'ACTIVE', '2025-08-30 13:00:00', '2025-08-30 13:00:00');

-- 2025年9月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202509001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月用户1', 'ROLE_USER', 'ACTIVE', '2025-09-03 09:00:00', '2025-09-03 09:00:00'),
('user202509002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月用户2', 'ROLE_USER', 'ACTIVE', '2025-09-10 11:30:00', '2025-09-10 11:30:00'),
('user202509003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月用户3', 'ROLE_USER', 'ACTIVE', '2025-09-17 14:00:00', '2025-09-17 14:00:00'),
('user202509004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月用户4', 'ROLE_USER', 'ACTIVE', '2025-09-24 16:00:00', '2025-09-24 16:00:00'),
('host202509001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月房东1', 'ROLE_HOST', 'ACTIVE', '2025-09-28 12:00:00', '2025-09-28 12:00:00'),
('host202509002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '九月房东2', 'ROLE_HOST', 'ACTIVE', '2025-09-30 15:00:00', '2025-09-30 15:00:00');

-- 2025年10月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202510001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月用户1', 'ROLE_USER', 'ACTIVE', '2025-10-05 10:00:00', '2025-10-05 10:00:00'),
('user202510002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月用户2', 'ROLE_USER', 'ACTIVE', '2025-10-12 12:00:00', '2025-10-12 12:00:00'),
('user202510003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月用户3', 'ROLE_USER', 'ACTIVE', '2025-10-18 14:30:00', '2025-10-18 14:30:00'),
('user202510004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月用户4', 'ROLE_USER', 'ACTIVE', '2025-10-25 16:00:00', '2025-10-25 16:00:00'),
('user202510005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月用户5', 'ROLE_USER', 'ACTIVE', '2025-10-30 11:30:00', '2025-10-30 11:30:00'),
('host202510001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十月房东1', 'ROLE_HOST', 'ACTIVE', '2025-10-28 13:00:00', '2025-10-28 13:00:00');

-- 2025年11月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202511001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月用户1', 'ROLE_USER', 'ACTIVE', '2025-11-03 09:30:00', '2025-11-03 09:30:00'),
('user202511002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月用户2', 'ROLE_USER', 'ACTIVE', '2025-11-10 11:00:00', '2025-11-10 11:00:00'),
('user202511003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月用户3', 'ROLE_USER', 'ACTIVE', '2025-11-17 13:30:00', '2025-11-17 13:30:00'),
('user202511004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月用户4', 'ROLE_USER', 'ACTIVE', '2025-11-24 15:00:00', '2025-11-24 15:00:00'),
('user202511005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月用户5', 'ROLE_USER', 'ACTIVE', '2025-11-28 12:00:00', '2025-11-28 12:00:00'),
('host202511001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十一月房东1', 'ROLE_HOST', 'ACTIVE', '2025-11-30 14:00:00', '2025-11-30 14:00:00');

-- 2025年12月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202512001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户1', 'ROLE_USER', 'ACTIVE', '2025-12-02 10:00:00', '2025-12-02 10:00:00'),
('user202512002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户2', 'ROLE_USER', 'ACTIVE', '2025-12-08 12:30:00', '2025-12-08 12:30:00'),
('user202512003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户3', 'ROLE_USER', 'ACTIVE', '2025-12-15 14:00:00', '2025-12-15 14:00:00'),
('user202512004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户4', 'ROLE_USER', 'ACTIVE', '2025-12-22 16:30:00', '2025-12-22 16:30:00'),
('user202512005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户5', 'ROLE_USER', 'ACTIVE', '2025-12-28 11:00:00', '2025-12-28 11:00:00'),
('user202512006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月用户6', 'ROLE_USER', 'ACTIVE', '2025-12-30 13:30:00', '2025-12-30 13:30:00'),
('host202512001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '十二月房东1', 'ROLE_HOST', 'ACTIVE', '2025-12-26 15:00:00', '2025-12-26 15:00:00');

-- 2026年1月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202601001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月用户1', 'ROLE_USER', 'ACTIVE', '2026-01-05 10:00:00', '2026-01-05 10:00:00'),
('user202601002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月用户2', 'ROLE_USER', 'ACTIVE', '2026-01-12 12:00:00', '2026-01-12 12:00:00'),
('user202601003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月用户3', 'ROLE_USER', 'ACTIVE', '2026-01-18 14:30:00', '2026-01-18 14:30:00'),
('user202601004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月用户4', 'ROLE_USER', 'ACTIVE', '2026-01-25 16:00:00', '2026-01-25 16:00:00'),
('user202601005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月用户5', 'ROLE_USER', 'ACTIVE', '2026-01-30 11:30:00', '2026-01-30 11:30:00'),
('host202601001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月房东1', 'ROLE_HOST', 'ACTIVE', '2026-01-28 13:00:00', '2026-01-28 13:00:00'),
('host202601002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '一月房东2', 'ROLE_HOST', 'ACTIVE', '2026-01-30 15:00:00', '2026-01-30 15:00:00');

-- 2026年2月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202602001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月用户1', 'ROLE_USER', 'ACTIVE', '2026-02-03 09:30:00', '2026-02-03 09:30:00'),
('user202602002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月用户2', 'ROLE_USER', 'ACTIVE', '2026-02-10 11:00:00', '2026-02-10 11:00:00'),
('user202602003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月用户3', 'ROLE_USER', 'ACTIVE', '2026-02-17 13:30:00', '2026-02-17 13:30:00'),
('user202602004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月用户4', 'ROLE_USER', 'ACTIVE', '2026-02-24 15:00:00', '2026-02-24 15:00:00'),
('user202602005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月用户5', 'ROLE_USER', 'ACTIVE', '2026-02-28 12:00:00', '2026-02-28 12:00:00'),
('host202602001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '二月房东1', 'ROLE_HOST', 'ACTIVE', '2026-02-26 14:00:00', '2026-02-26 14:00:00');

-- 2026年3月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202603001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户1', 'ROLE_USER', 'ACTIVE', '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
('user202603002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户2', 'ROLE_USER', 'ACTIVE', '2026-03-08 12:30:00', '2026-03-08 12:30:00'),
('user202603003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户3', 'ROLE_USER', 'ACTIVE', '2026-03-15 14:00:00', '2026-03-15 14:00:00'),
('user202603004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户4', 'ROLE_USER', 'ACTIVE', '2026-03-22 16:30:00', '2026-03-22 16:30:00'),
('user202603005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户5', 'ROLE_USER', 'ACTIVE', '2026-03-28 11:00:00', '2026-03-28 11:00:00'),
('user202603006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月用户6', 'ROLE_USER', 'ACTIVE', '2026-03-30 13:30:00', '2026-03-30 13:30:00'),
('host202603001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '三月房东1', 'ROLE_HOST', 'ACTIVE', '2026-03-26 15:00:00', '2026-03-26 15:00:00');

-- 2026年4月注册用户
INSERT INTO user (username, password, nickname, role, status, created_time, updated_time)
VALUES 
('user202604001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '四月用户1', 'ROLE_USER', 'ACTIVE', '2026-04-05 10:00:00', '2026-04-05 10:00:00'),
('user202604002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '四月用户2', 'ROLE_USER', 'ACTIVE', '2026-04-12 12:00:00', '2026-04-12 12:00:00'),
('user202604003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '四月用户3', 'ROLE_USER', 'ACTIVE', '2026-04-18 14:30:00', '2026-04-18 14:30:00'),
('host202604001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmxNsZGvxq8S5Ej6', '四月房东1', 'ROLE_HOST', 'ACTIVE', '2026-04-20 16:00:00', '2026-04-20 16:00:00');

SELECT '测试用户和房东数据插入完成！' as message;
SELECT '所有测试用户的密码都是: 123456' as note;
