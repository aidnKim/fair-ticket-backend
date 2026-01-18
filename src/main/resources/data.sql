-- 1. 유저 데이터
INSERT INTO users (email, password, name, role, point, created_at, updated_at)
VALUES ('test@test.com', '$2a$10$u5Rgkm09P45ULNsJImFym.4Zf0BGQ12i3.I9ncCHULD9DOZz5elc2', '테스트유저', 'USER', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 2. 공연장 데이터 (오라클은 VALUES 뒤에 여러 개 못 씀 -> 하나씩 쪼개야 함)
INSERT INTO venues (name, total_rows, seats_per_row, vip_rows) VALUES ('벡스코 제1전시장 1홀', 5, 10, 'A,B');
INSERT INTO venues (name, total_rows, seats_per_row, vip_rows) VALUES ('블루스퀘어 신한카드홀', 4, 8, 'A');
INSERT INTO venues (name, total_rows, seats_per_row, vip_rows) VALUES ('잠실 올림픽주경기장', 6, 15, 'A,B,C');
INSERT INTO venues (name, total_rows, seats_per_row, vip_rows) VALUES ('잠실종합운동장 내 빅탑', 5, 12, 'A,B');


-- 3. 공연 데이터 (하나씩 쪼갬)
INSERT INTO concerts (title, description, image_url, detail_image_url, start_date, end_date, venue_id, created_at, updated_at)
VALUES ('2026 엠씨더맥스 이수 콘서트 ‘겨울나기’ - 부산', '믿고 듣는 이수의 감동적인 라이브!', '/images/mcthemax_poster.jpg', '/images/mcthemax_detail.jpg', DATE '2026-06-17', DATE '2026-06-19', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concerts (title, description, image_url, detail_image_url, start_date, end_date, venue_id, created_at, updated_at)
VALUES ('2026 이창섭 단독 콘서트 〈AndEnd〉', '지금 이 순간, 다시 시작되는 전설', '/images/leecs_poster.jpg', '/images/leecs_detail.jpg', DATE '2026-06-20', DATE '2026-06-21', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concerts (title, description, image_url, detail_image_url, start_date, end_date, venue_id, created_at, updated_at)
VALUES ('2026 이문세 ‘The Best’ - 대구', '지금 이 순간, 다시 시작되는 전설', '/images/lms_poster.jpg', '/images/lms_detail.jpg', DATE '2026-07-15', DATE '2026-07-15', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concerts (title, description, image_url, detail_image_url, start_date, end_date, venue_id, created_at, updated_at)
VALUES ('센트럴 씨 첫 단독 내한공연', 'Central Cee - CAN’T RUSH GREATNESS WORLD TOUR - Asia 2026설', '/images/cen_poster.jpg', '/images/cen_detail.jpg', DATE '2026-07-20', DATE '2026-07-22', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 4. 스케줄 데이터 (하나씩 쪼갬)
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (1, TIMESTAMP '2026-06-17 19:00:00', 50, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (1, TIMESTAMP '2026-06-18 18:00:00', 50, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (1, TIMESTAMP '2026-06-19 17:00:00', 50, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (2, TIMESTAMP '2026-06-20 19:00:00', 32, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (2, TIMESTAMP '2026-06-21 18:00:00', 32, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (3, TIMESTAMP '2026-07-15 17:00:00', 90, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (4, TIMESTAMP '2026-07-20 19:00:00', 60, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (4, TIMESTAMP '2026-07-21 18:00:00', 60, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at) VALUES (4, TIMESTAMP '2026-07-22 17:00:00', 60, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---- 1. 유저 데이터 (로그인 테스트용)
---- password는 BCrypt로 '1234'를 암호화한 문자열
--INSERT INTO users (email, password, name, role, point, created_at, updated_at)
--VALUES (
--	'test@test.com', 
--	'$2a$10$u5Rgkm09P45ULNsJImFym.4Zf0BGQ12i3.I9ncCHULD9DOZz5elc2', 
--	'테스트유저', 'USER', 0, NOW(), NOW())
--ON DUPLICATE KEY UPDATE
--  updated_at = NOW();
--
---- 2. 공연장 데이터
--INSERT INTO venues (name, total_rows, seats_per_row, vip_rows) VALUES
--('벡스코 제1전시장 1홀', 5, 10, 'A,B'),
--('블루스퀘어 신한카드홀', 4, 8, 'A'),
--('잠실 올림픽주경기장', 6, 15, 'A,B,C'),
--('잠실종합운동장 내 빅탑', 5, 12, 'A,B');
--
---- 3. 공연 데이터 (메인 페이지용)
--INSERT INTO concerts (title, description, image_url, detail_image_url, start_date, end_date, venue_id, created_at, updated_at)
--VALUES 
--(
--	'2026 엠씨더맥스 이수 콘서트 ‘겨울나기’ - 부산', 
--	'믿고 듣는 이수의 감동적인 라이브!', 
--	'/images/mcthemax_poster.jpg', 
--	'/images/mcthemax_detail.jpg',
--	'2026-06-17', '2026-06-19',
--	1,
--    NOW(), NOW()
--),
--
--(
--	'2026 이창섭 단독 콘서트 〈AndEnd〉', 
--	'지금 이 순간, 다시 시작되는 전설', 
--	'/images/leecs_poster.jpg', 
--	'/images/leecs_detail.jpg',
--	'2026-06-20', '2026-06-21',
--	2,
--    NOW(), NOW()
--),
--
--(
--	'2026 이문세 ‘The Best’ - 대구', 
--	'지금 이 순간, 다시 시작되는 전설', 
--	'/images/lms_poster.jpg', 
--	'/images/lms_detail.jpg',
--	'2026-07-15', '2026-07-15',
--	3,
--    NOW(), NOW()
--),
--
--(
--	'센트럴 씨 첫 단독 내한공연', 
--	'Central Cee - CAN’T RUSH GREATNESS WORLD TOUR - Asia 2026설', 
--	'/images/cen_poster.jpg', 
--	'/images/cen_detail.jpg',
--	'2026-07-20', '2026-07-22',
--	4,
--    NOW(), NOW()
--);
--
---- 4. 스케줄 데이터
--INSERT INTO concert_schedules (concert_id, concert_date, total_seats, available_seats, created_at, updated_at)
--VALUES 
--(1, '2026-06-17 19:00:00', 50, 50, NOW(), NOW()),
--(1, '2026-06-18 18:00:00', 50, 50, NOW(), NOW()),
--(1, '2026-06-19 17:00:00', 50, 50, NOW(), NOW()),
--(2, '2026-06-20 19:00:00', 32, 32, NOW(), NOW()),
--(2, '2026-06-21 18:00:00', 32, 32, NOW(), NOW()),
--(3, '2026-07-15 17:00:00', 90, 90, NOW(), NOW()),
--(4, '2026-07-20 19:00:00', 60, 60, NOW(), NOW()),
--(4, '2026-07-21 18:00:00', 60, 60, NOW(), NOW()),
--(4, '2026-07-22 17:00:00', 60, 60, NOW(), NOW());