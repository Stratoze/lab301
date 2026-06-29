-- =================================================================================
-- PROJECT: DÒ VÉ SỐ (LOTTERY CHECKER) - FINAL DATABASE SCRIPT
-- Cấu hình: MySQL 8.x / 9.x
-- =================================================================================

CREATE DATABASE IF NOT EXISTS lottery_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lottery_db;

SET FOREIGN_KEY_CHECKS = 0;

-- XÓA BẢNG CŨ ĐỂ KHỞI TẠO MỚI HOÀN TOÀN
DROP TABLE IF EXISTS check_histories;
DROP TABLE IF EXISTS check_sessions;
DROP TABLE IF EXISTS prize_details;
DROP TABLE IF EXISTS lottery_results;
DROP TABLE IF EXISTS lottery_stations;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_auth_providers;
DROP TABLE IF EXISTS users;

-- =================================================================================
-- 1. TẠO CẤU TRÚC BẢNG (DDL)
-- =================================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(25) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) UNIQUE,
    password VARCHAR(255), -- BCrypt hash
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_auth_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL, -- 'GOOGLE', 'FACEBOOK'
    provider_id VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE lottery_stations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    station_code VARCHAR(15) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    region VARCHAR(20) NOT NULL -- 'NORTH', 'CENTRAL', 'SOUTH'
);

CREATE TABLE lottery_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_code VARCHAR(30) NOT NULL UNIQUE,
    station_id INT NOT NULL,
    draw_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'UNPUBLISH',
    total_queries INT DEFAULT 0,
    created_by BIGINT,
    published_by BIGINT,
    published_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES lottery_stations(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (published_by) REFERENCES users(id),
    UNIQUE KEY unique_idx_station_date (station_id, draw_date)
);

CREATE TABLE prize_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_id BIGINT NOT NULL,
    prize_type VARCHAR(20) NOT NULL,
    winning_number VARCHAR(10) NOT NULL,
    reward_amount BIGINT NOT NULL, -- VND
    FOREIGN KEY (result_id) REFERENCES lottery_results(id) ON DELETE CASCADE
);

CREATE TABLE check_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL, -- NULL if Guest
    total_spent BIGINT DEFAULT 0,
    total_won BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE check_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    result_id BIGINT NOT NULL,
    ticket_number VARCHAR(10) NOT NULL,
    is_won BOOLEAN DEFAULT FALSE,
    won_prize VARCHAR(20),
    won_amount BIGINT DEFAULT 0,
    check_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES check_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (result_id) REFERENCES lottery_results(id)
);

SET FOREIGN_KEY_CHECKS = 1;

-- =================================================================================
-- 2. DỮ LIỆU MẪU (DML) - TỐI THIỂU 15 BẢN GHI MỖI BẢNG
-- =================================================================================

-- Pwd: 'phucddfx03285' | BCrypt Cost: 12 vòng ($12$)
SET @pwd = '$2a$12$dffPiK9Yj5Ji6f8MN3wh.co0b9.4F7x/6MmD3Ev1Hwbj47Pa6Jz6';

-- BẢNG 1: USERS (15 records)
INSERT INTO users (user_code, email, phone, password, full_name, role, is_active) VALUES
('USR-10-2023-00000001', 'admin@veso.vn', '0900000001', @pwd, 'Phan Đặng Duy Phúc', 'ROLE_ADMIN', 1),
('USR-10-2023-00000002', 'staff@veso.vn', '0900000002', @pwd, 'Nguyễn Nhân Viên', 'ROLE_ADMIN', 1),
('USR-10-2023-00000003', 'khach1@gmail.com', '0910000001', @pwd, 'Lê Văn Tám', 'ROLE_USER', 1),
('USR-10-2023-00000004', 'khach2@gmail.com', '0910000002', @pwd, 'Trần Thị Chín', 'ROLE_USER', 1),
('USR-10-2023-00000005', 'google_u@gmail.com', NULL, NULL, 'User Google', 'ROLE_USER', 1),
('USR-10-2023-00000006', 'fb_u@gmail.com', NULL, NULL, 'User Facebook', 'ROLE_USER', 1),
('USR-10-2023-00000007', 'test3@gmail.com', '0910000007', @pwd, 'Lý Tiểu Long', 'ROLE_USER', 1),
('USR-10-2023-00000008', 'test4@gmail.com', '0910000008', @pwd, 'Châu Tinh Trì', 'ROLE_USER', 1),
('USR-11-2023-00000001', 'locked@gmail.com', '0910000009', @pwd, 'Người Bị Khóa', 'ROLE_USER', 0),
('USR-11-2023-00000002', 'test5@gmail.com', '0910000010', @pwd, 'Ngô Thanh Vân', 'ROLE_USER', 1),
('USR-11-2023-00000003', 'test6@gmail.com', '0910000011', @pwd, 'Trương Ngọc Ánh', 'ROLE_USER', 1),
('USR-11-2023-00000004', 'test7@gmail.com', '0910000012', @pwd, 'Sơn Tùng MTP', 'ROLE_USER', 1),
('USR-11-2023-00000005', 'test8@gmail.com', '0910000013', @pwd, 'Hồ Ngọc Hà', 'ROLE_USER', 1),
('USR-11-2023-00000006', 'test9@gmail.com', '0910000014', @pwd, 'Đen Vâu', 'ROLE_USER', 1),
('USR-11-2023-00000007', 'test10@gmail.com', '0910000015', @pwd, 'Mỹ Tâm', 'ROLE_USER', 1);

-- BẢNG 2: USER AUTH PROVIDERS (15 records)
INSERT INTO user_auth_providers (user_id, provider, provider_id) VALUES
(5, 'GOOGLE', 'GOO_ID_001'), (6, 'FACEBOOK', 'FB_ID_001'), (1, 'GOOGLE', 'GOO_ID_ADMIN'),
(3, 'GOOGLE', 'GOO_ID_003'), (4, 'GOOGLE', 'GOO_ID_004'), (7, 'FACEBOOK', 'FB_ID_007'),
(8, 'GOOGLE', 'GOO_ID_008'), (10, 'FACEBOOK', 'FB_ID_010'), (11, 'GOOGLE', 'GOO_ID_011'),
(12, 'FACEBOOK', 'FB_ID_012'), (13, 'GOOGLE', 'GOO_ID_013'), (14, 'GOOGLE', 'GOO_ID_014'),
(15, 'FACEBOOK', 'FB_ID_015'), (5, 'FACEBOOK', 'FB_ID_005_LINKED'), (2, 'GOOGLE', 'GOO_ID_STAFF');

-- BẢNG 3: PASSWORD RESET TOKENS (15 records)
INSERT INTO password_reset_tokens (user_id, token, expires_at, is_used) VALUES
(3, 'TOKEN-001', '2026-12-31 23:59:59', 0), (4, 'TOKEN-002', '2026-12-31 23:59:59', 0),
(7, 'TOKEN-003', '2023-01-01 00:00:00', 0), (8, 'TOKEN-004', '2026-12-31 23:59:59', 1),
(10, 'TOKEN-005', '2026-12-31 23:59:59', 0), (11, 'TOKEN-006', '2026-12-31 23:59:59', 0),
(12, 'TOKEN-007', '2026-12-31 23:59:59', 0), (13, 'TOKEN-008', '2026-12-31 23:59:59', 0),
(14, 'TOKEN-009', '2026-12-31 23:59:59', 0), (15, 'TOKEN-010', '2026-12-31 23:59:59', 0),
(3, 'TOKEN-011', '2026-12-31 23:59:59', 1), (4, 'TOKEN-012', '2026-12-31 23:59:59', 0),
(7, 'TOKEN-013', '2026-12-31 23:59:59', 0), (8, 'TOKEN-014', '2026-12-31 23:59:59', 0),
(10, 'TOKEN-015', '2026-12-31 23:59:59', 0);

-- BẢNG 4: LOTTERY STATIONS (15 records)
INSERT INTO lottery_stations (station_code, name, region) VALUES
('SOU-HCM', 'TP. Hồ Chí Minh', 'SOUTH'), ('SOU-DT', 'Đồng Tháp', 'SOUTH'),
('SOU-CM', 'Cà Mau', 'SOUTH'), ('SOU-VT', 'Vũng Tàu', 'SOUTH'),
('SOU-BT', 'Bến Tre', 'SOUTH'), ('SOU-BL', 'Bạc Liêu', 'SOUTH'),
('CEN-DN', 'Đà Nẵng', 'CENTRAL'), ('CEN-KH', 'Khánh Hòa', 'CENTRAL'),
('CEN-PY', 'Phú Yên', 'CENTRAL'), ('NOR-HN', 'Hà Nội', 'NORTH'),
('NOR-QN', 'Quảng Ninh', 'NORTH'), ('NOR-HP', 'Hải Phòng', 'NORTH'),
('SOU-DN', 'Đồng Nai', 'SOUTH'), ('SOU-CT', 'Cần Thơ', 'SOUTH'),
('SOU-ST', 'Sóc Trăng', 'SOUTH');

-- BẢNG 5: LOTTERY RESULTS (15 records)
INSERT INTO lottery_results (result_code, station_id, draw_date, status, total_queries, created_by, published_by, published_at) VALUES
('RES-HCM-23102023', 1, '2023-10-23', 'PUBLISH', 150, 1, 1, '2023-10-23 16:30:00'),
('RES-DT-23102023', 2, '2023-10-23', 'PUBLISH', 80, 1, 2, '2023-10-23 16:30:00'),
('RES-CM-23102023', 3, '2023-10-23', 'PUBLISH', 45, 2, 2, '2023-10-23 16:30:00'),
('RES-VT-24102023', 4, '2024-10-24', 'PUBLISH', 120, 1, 1, '2024-10-24 16:30:00'),
('RES-BT-24102023', 5, '2024-10-24', 'PUBLISH', 90, 2, 1, '2024-10-24 16:30:00'),
('RES-BL-24102023', 6, '2024-10-24', 'UNPUBLISH', 0, 1, NULL, NULL),
('RES-DN-21102023', 7, '2023-10-21', 'PUBLISH', 200, 1, 1, '2023-10-21 17:30:00'),
('RES-KH-22102023', 8, '2023-10-22', 'PUBLISH', 310, 2, 1, '2023-10-22 17:30:00'),
('RES-PY-23102023', 9, '2023-10-23', 'PUBLISH', 55, 1, 1, '2023-10-23 17:30:00'),
('RES-HN-25102023', 10, '2023-10-25', 'PUBLISH', 500, 1, 2, '2023-10-25 18:30:00'),
('RES-QN-26102023', 11, '2023-10-26', 'PUBLISH', 420, 2, 2, '2023-10-26 18:30:00'),
('RES-HP-27102023', 12, '2023-10-27', 'PUBLISH', 110, 1, 1, '2023-10-27 18:30:00'),
('RES-HCM-30102023', 1, '2023-10-30', 'PUBLISH', 15, 1, 1, '2023-10-30 16:30:00'),
('RES-CT-01112023', 14, '2023-11-01', 'PUBLISH', 99, 1, 1, '2023-11-01 16:30:00'),
('RES-ST-01112023', 15, '2023-11-01', 'UNPUBLISH', 0, 2, NULL, NULL);

-- BẢNG 6: PRIZE DETAILS (18 records - Ví dụ cho đài HCM ngày 23/10)
INSERT INTO prize_details (result_id, prize_type, winning_number, reward_amount) VALUES
(1, 'G8', '85', 100000), (1, 'G7', '763', 200000), (1, 'G6', '1234', 400000),
(1, 'G6', '5678', 400000), (1, 'G6', '9012', 400000), (1, 'G5', '5555', 1000000),
(1, 'G4', '11111', 3000000), (1, 'G4', '22222', 3000000), (1, 'G4', '33333', 3000000),
(1, 'G3', '44444', 10000000), (1, 'G3', '55555', 10000000), (1, 'G2', '66666', 15000000),
(1, 'G1', '77777', 30000000), (1, 'G_DB', '999999', 2000000000),
(10, 'G_DB', '12345', 500000000), (10, 'G1', '54321', 10000000), -- Hà Nội
(4, 'G8', '12', 100000), (4, 'G_DB', '123456', 2000000000); -- Vũng Tàu

-- BẢNG 7: CHECK SESSIONS (15 records)
INSERT INTO check_sessions (user_id, total_spent, total_won) VALUES
(3, 20000, 100000), (4, 10000, 0), (NULL, 50000, 0), (5, 10000, 2000000000),
(6, 20000, 400000), (7, 30000, 0), (8, 10000, 0), (NULL, 10000, 0),
(10, 50000, 0), (11, 10000, 0), (12, 10000, 0), (13, 20000, 0),
(14, 10000, 0), (15, 10000, 0), (3, 10000, 0);

-- BẢNG 8: CHECK HISTORIES (15 records)
INSERT INTO check_histories (session_id, result_id, ticket_number, is_won, won_prize, won_amount) VALUES
(1, 1, '123485', 1, 'G8', 100000), (1, 1, '000000', 0, NULL, 0), -- Session 1
(2, 1, '111111', 0, NULL, 0), (4, 1, '999999', 1, 'G_DB', 2000000000), -- Thắng ĐB
(5, 1, '123456', 0, NULL, 0), (5, 1, '005678', 1, 'G6', 400000),
(6, 4, '000000', 0, NULL, 0), (6, 4, '111111', 0, NULL, 0), (6, 4, '222222', 0, NULL, 0),
(8, 10, '12345', 1, 'G_DB', 500000000), -- Khách vãng lai thắng miền bắc
(9, 1, '111111', 0, NULL, 0), (10, 1, '222222', 0, NULL, 0),
(11, 1, '333333', 0, NULL, 0), (12, 1, '444444', 0, NULL, 0), (13, 1, '555555', 0, NULL, 0);