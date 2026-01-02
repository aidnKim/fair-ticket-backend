-- 1. 유저 데이터 (로그인 테스트용)
-- password는 BCrypt로 '1234'를 암호화한 문자열
INSERT INTO users (email, password, name, role, point, created_at, updated_at)
VALUES (
	'test@test.com', 
	'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
	'테스트유저', 'USER', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  updated_at = NOW();



-- 2. 공연 데이터 (메인 페이지용)
INSERT INTO concerts (title, description, image_url, start_date, end_date, venue, created_at, updated_at)
VALUES 
(
	'2025 아이유 콘서트 : H.E.R', 
	'믿고 듣는 아이유의 감동적인 라이브!', 
	'https://placehold.co/600x800?text=IU+Concert', 
	'2026-05-01', '2026-05-03',
	'서울 KSPO DOME (체조경기장)',
    NOW(), NOW()
),

(
	'뮤지컬 <지킬 앤 하이드>', 
	'지금 이 순간, 다시 시작되는 전설', 
	'https://placehold.co/600x800?text=Jekyll+%26+Hyde', 
	'2026-06-10', '2026-08-20',
	'블루스퀘어 신한카드홀',
    NOW(), NOW()
),

(
	'싸이 흠뻑쇼 SUMMER SWAG 2025', 
	'미친듯이 놀 준비 되었는가?', 
	'https://placehold.co/600x800?text=PSY+SUMMER+SWAG', 
	'2026-07-15', '2026-07-15',
	'잠실 올림픽주경기장',
    NOW(), NOW()
),

(
	'태양의서커스 <루치아>', 
	'꿈과 현실을 넘나드는 멕시코의 전설', 
	'https://placehold.co/600x800?text=Cirque+Luzia', 
	'2026-04-01', '2026-05-30',
	'잠실종합운동장 내 빅탑',
    NOW(), NOW()
);