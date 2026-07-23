-- =================================================================================
-- LOTTERY CHECKER - SCHEMA ONLY
-- Run automatically by MySQL Docker on first boot
-- Database 'lottery_db' and user 'lottery' are already created by the container
-- =================================================================================

USE lottery_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS check_histories;
DROP TABLE IF EXISTS check_sessions;
DROP TABLE IF EXISTS prize_details;
DROP TABLE IF EXISTS lottery_results;
DROP TABLE IF EXISTS lottery_stations;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_auth_providers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =================================================================================
-- TABLES
-- =================================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(25) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) UNIQUE,
    password VARCHAR(255),
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
    provider VARCHAR(20) NOT NULL,
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
    region VARCHAR(20) NOT NULL
);

CREATE TABLE lottery_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_code VARCHAR(30) NOT NULL UNIQUE,
    station_id INT NOT NULL,
    draw_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'UNPUBLISH',
    total_queries BIGINT DEFAULT 0,
    created_by BIGINT,
    published_by BIGINT,
    published_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES lottery_stations(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (published_by) REFERENCES users(id),
    UNIQUE KEY unique_idx_station_date (station_id, draw_date),
    INDEX idx_draw_date (draw_date),
    INDEX idx_status (status)
);

CREATE TABLE prize_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_id BIGINT NOT NULL,
    prize_type VARCHAR(20) NOT NULL,
    winning_number VARCHAR(10) NOT NULL,
    reward_amount BIGINT NOT NULL,
    FOREIGN KEY (result_id) REFERENCES lottery_results(id) ON DELETE CASCADE,
    UNIQUE KEY unique_idx_result_prize_number (result_id, prize_type, winning_number)
);

CREATE TABLE check_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
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