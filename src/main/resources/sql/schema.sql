-- 1. 안전장치: 외래키 검사 잠시 끄기 (순서 상관없이 만들기 위해)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 기존 테이블이 있다면 삭제 (초기화용)
DROP TABLE IF EXISTS blocked_clients;
DROP TABLE IF EXISTS access_logs;
DROP TABLE IF EXISTS waiting_subscriptions;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS concert_schedules;
DROP TABLE IF EXISTS concerts;
DROP TABLE IF EXISTS users;

-- 3. 테이블 생성 시작
-- (1) 사용자
CREATE TABLE users (
    user_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    email         VARCHAR(100)    NOT NULL UNIQUE COMMENT '로그인 ID',
    password      VARCHAR(255)    NOT NULL COMMENT '암호화된 비밀번호',
    nickname      VARCHAR(50)     NOT NULL COMMENT '닉네임',
    role          VARCHAR(20)     NOT NULL DEFAULT 'USER' COMMENT 'ROLE_USER, ROLE_ADMIN',
    point         BIGINT          NOT NULL DEFAULT 0 COMMENT '보유 포인트',
    created_at    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

-- (2) 공연 정보
CREATE TABLE concerts (
    concert_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    title         VARCHAR(200)    NOT NULL COMMENT '공연 제목',
    description   TEXT            COMMENT '공연 설명',
    start_date    DATETIME        NOT NULL COMMENT '공연 시작일',
    end_date      DATETIME        NOT NULL COMMENT '공연 종료일',
    created_at    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (concert_id)
);

-- (3) 공연 일정
CREATE TABLE concert_schedules (
    schedule_id   BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    concert_id    BIGINT          NOT NULL COMMENT 'FK',
    concert_date  DATETIME        NOT NULL COMMENT '공연 날짜/시간',
    total_seats   INT             NOT NULL DEFAULT 0,
    available_seats INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (schedule_id),
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id)
);

-- (4) 좌석 (핵심)
CREATE TABLE seats (
    seat_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    schedule_id   BIGINT          NOT NULL COMMENT 'FK',
    seat_no       INT             NOT NULL COMMENT '좌석 번호',
    grade         VARCHAR(20)     NOT NULL,
    price         DECIMAL(10, 2)  NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    version       BIGINT          DEFAULT 0 COMMENT '낙관적 락 버전',
    PRIMARY KEY (seat_id),
    UNIQUE KEY uk_schedule_seat (schedule_id, seat_no),
    FOREIGN KEY (schedule_id) REFERENCES concert_schedules(schedule_id)
);

-- (5) 예매 내역
CREATE TABLE reservations (
    reservation_id BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'PK',
    user_id       BIGINT          NOT NULL,
    seat_id       BIGINT          NOT NULL,
    schedule_id   BIGINT          NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    reservation_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    expire_time   DATETIME        COMMENT '결제 만료 시간',
    PRIMARY KEY (reservation_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);

-- (6) 결제 내역
CREATE TABLE payments (
    payment_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    reservation_id BIGINT         NOT NULL,
    user_id       BIGINT          NOT NULL,
    amount        DECIMAL(10, 2)  NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'COMPLETED',
    payment_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- (7) 취소표 알림
CREATE TABLE waiting_subscriptions (
    subscription_id BIGINT        NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NOT NULL,
    schedule_id   BIGINT          NOT NULL,
    created_at    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    is_active     BOOLEAN         DEFAULT TRUE,
    PRIMARY KEY (subscription_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (schedule_id) REFERENCES concert_schedules(schedule_id),
    UNIQUE KEY uk_user_schedule (user_id, schedule_id)
);

-- (8) 로그 및 차단
CREATE TABLE access_logs (
    log_id        BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NULL,
    ip_address    VARCHAR(50)     NOT NULL,
    endpoint      VARCHAR(200)    NOT NULL,
    action_type   VARCHAR(50)     NOT NULL,
    request_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    is_abnormal   BOOLEAN         DEFAULT FALSE,
    PRIMARY KEY (log_id),
    INDEX idx_ip_time (ip_address, request_time)
);

CREATE TABLE blocked_clients (
    block_id      BIGINT          NOT NULL AUTO_INCREMENT,
    ip_address    VARCHAR(50)     NULL,
    user_id       BIGINT          NULL,
    reason        VARCHAR(255)    NULL,
    blocked_at    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    release_at    DATETIME        NULL,
    PRIMARY KEY (block_id)
);

-- 4. 안전장치: 외래키 검사 다시 켜기
SET FOREIGN_KEY_CHECKS = 1;