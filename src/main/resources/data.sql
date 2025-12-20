-- 1. 유저 데이터 (로그인 테스트용)
-- 비밀번호는 암호화된 걸 넣어야 하는데, 일단은 테스트니까 대충 넣거나 
-- BCrypt로 '1234'를 암호화한 문자열을 넣는 게 좋습니다. (아래는 예시)
INSERT INTO users (email, password, nickname, role, created_at, updated_at)
VALUES (
	'test@test.com', 
	'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
	'테스트유저', 'USER', NOW(), NOW());


-- 2. 공연 데이터 (메인 페이지용)
-- 이미지 URL은 실제 인터파크나 구글 이미지 주소를 복사해서 넣으세요.
INSERT INTO concert (title, description, image_url, start_date, end_date, created_at, updated_at)
VALUES 
('2025 아이유 콘서트 : H.E.R', '믿고 듣는 아이유의 감동적인 라이브!', 'https://ticketimage.interpark.com/Play/image/large/24/24001860_p.gif', '2025-05-01', '2025-05-03', NOW(), NOW()),

('뮤지컬 <지킬 앤 하이드>', '지금 이 순간, 다시 시작되는 전설', 'https://ticketimage.interpark.com/Play/image/large/23/23015482_p.gif', '2025-06-10', '2025-08-20', NOW(), NOW()),

('싸이 흠뻑쇼 SUMMER SWAG 2025', '미친듯이 놀 준비 되었는가?', 'https://ticketimage.interpark.com/Play/image/large/24/24005678_p.gif', '2025-07-15', '2025-07-15', NOW(), NOW()),

('태양의서커스 <루치아>', '꿈과 현실을 넘나드는 멕시코의 전설', 'https://ticketimage.interpark.com/Play/image/large/23/23013211_p.gif', '2025-04-01', '2025-05-30', NOW(), NOW());