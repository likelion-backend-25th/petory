-- 더미 데이터 (schema.sql 테이블 기준, FK 의존 순서)
-- password: password123 (BCrypt)

-- 1. member
INSERT INTO member (id, email, password, nickname, species, sex, birth_date, intro, profile_image, address, status, role, created_at, info_provide_agreement) VALUES
(1, 'admin@petory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자냥', '고양이', '여', '2020-01-01', '사자그램 관리자입니다.', 'https://s3.example.com/profile/admin.jpg', '서울시 강남구', 'ACTIVE', 'ROLE_ADMIN', '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 'mungchi@petory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '멍치', '개', '남', '2021-03-15', '산책 좋아하는 골든리트리버!', 'https://s3.example.com/profile/mungchi.jpg', '서울시 마포구', 'ACTIVE', 'ROLE_VIP', '2025-02-10 11:00:00', '2025-02-10 11:00:00'),
(3, 'nabi@petory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '나비', '고양이', '여', '2022-07-20', '창가에서 낮잠 자는 게 취미예요.', 'https://s3.example.com/profile/nabi.jpg', '서울시 송파구', 'ACTIVE', 'ROLE_USER', '2025-03-05 09:30:00', NULL),
(4, 'coco@petory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '코코', '개', '여', '2020-11-08', '간식 없으면 시무룩해요.', 'https://s3.example.com/profile/coco.jpg', '경기도 성남시', 'ACTIVE', 'ROLE_USER', '2025-04-12 14:20:00', '2025-04-12 14:20:00'),
(5, 'choco@petory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '초코', '개', '남', '2019-05-30', '공놀이 최강자 🎾', 'https://s3.example.com/profile/choco.jpg', '부산시 해운대구', 'ACTIVE', 'ROLE_USER', '2025-05-01 16:00:00', NULL),
(6, NULL, NULL, '카카오토리', '고양이', '남', '2023-01-12', '카카오로 가입했어요.', 'https://s3.example.com/profile/kakao.jpg', '서울시 종로구', 'ACTIVE', 'ROLE_USER', '2025-06-15 10:00:00', '2025-06-15 10:00:00');

-- 2. linked_account
INSERT INTO linked_account (id, member_id, provider, provider_user_id, provider_email, created_at) VALUES
(1, 6, 'KAKAO', 'kakao_123456789', 'kakao_tori@kakao.com', '2025-06-15 10:00:00'),
(2, 2, 'GOOGLE', 'google_987654321', 'mungchi@gmail.com', '2025-02-10 11:05:00'),
(3, 3, 'KAKAO', 'kakao_555666777', 'nabi_cat@kakao.com', '2025-03-05 09:35:00');

-- 3. post_main
INSERT INTO post_main (id, member_id, type, content, bgm_url, is_subscriber_only, hashtags, created_at, updated_at) VALUES
(1, 2, 1, '오늘 한강 공원 산책했어요! 날씨 최고~', 'https://s3.example.com/bgm/happy.mp3', 0, '#강아지 #산책 #한강', '2025-07-01 10:00:00', '2025-07-01 10:00:00'),
(2, 3, 1, '창밖 새소리 들으며 낮잠... zzz', NULL, 0, '#고양이 #낮잠 #일상', '2025-07-02 14:30:00', '2025-07-02 14:30:00'),
(3, 2, 1, '펫클럽 구독자 전용! 오늘의 특별 간식 ASMR', 'https://s3.example.com/bgm/asmr.mp3', 1, '#펫클럽 #간식 #ASMR', '2025-07-03 18:00:00', '2025-07-03 18:00:00'),
(4, 4, 2, '강아지 피부병 연고 추천 부탁드려요!', NULL, 0, '#QandA #피부병 #강아지', '2025-07-04 09:00:00', '2025-07-04 09:00:00'),
(5, 5, 1, '공놀이 30분 성공! 초코 기특해', NULL, 0, '#공놀이 #초코 #운동', '2025-07-05 17:20:00', '2025-07-05 17:20:00'),
(6, 6, 1, '첫 게시글이에요. 잘 부탁드려요!', NULL, 0, '#인사 #고양이', '2025-07-06 11:00:00', '2025-07-06 11:00:00');

-- 4. post_image
INSERT INTO post_image (id, post_id, image_url, sort_order) VALUES
(1, 1, 'https://s3.example.com/post/1_1.jpg', 0),
(2, 1, 'https://s3.example.com/post/1_2.jpg', 1),
(3, 1, 'https://s3.example.com/post/1_3.jpg', 2),
(4, 2, 'https://s3.example.com/post/2_1.jpg', 0),
(5, 3, 'https://s3.example.com/post/3_1.jpg', 0),
(6, 3, 'https://s3.example.com/post/3_2.jpg', 1),
(7, 5, 'https://s3.example.com/post/5_1.jpg', 0),
(8, 6, 'https://s3.example.com/post/6_1.jpg', 0);

-- 5. comment
INSERT INTO comment (id, post_id, member_id, content, created_at) VALUES
(1, 1, 3, '한강 너무 좋겠다! 다음에 같이 가요~', '2025-07-01 10:30:00'),
(2, 1, 4, '멍치 표정 귀여워요 ㅎㅎ', '2025-07-01 11:00:00'),
(3, 2, 2, '나비도 낮잠 장인!', '2025-07-02 15:00:00'),
(4, 4, 2, '우리 병원에서는 OO연고 많이 써요. 수의사 상담도 꼭!', '2025-07-04 10:00:00'),
(5, 4, 5, '저희 초코도 그거 썼는데 좋아졌어요.', '2025-07-04 12:00:00'),
(6, 5, 3, '공놀이 대성공 축하해요!', '2025-07-05 18:00:00'),
(7, 6, 2, '환영해요 카카오토리!', '2025-07-06 11:30:00');

-- 6. post_interaction
INSERT INTO post_interaction (id, member_id, post_id, interaction_type, created_at) VALUES
(1, 3, 1, 'LIKE', '2025-07-01 10:25:00'),
(2, 4, 1, 'LIKE', '2025-07-01 10:40:00'),
(3, 4, 1, 'BOOKMARK', '2025-07-01 10:41:00'),
(4, 2, 2, 'LIKE', '2025-07-02 14:45:00'),
(5, 5, 2, 'BOOKMARK', '2025-07-02 16:00:00'),
(6, 3, 3, 'LIKE', '2025-07-03 18:30:00'),
(7, 4, 4, 'LIKE', '2025-07-04 09:30:00'),
(8, 2, 5, 'LIKE', '2025-07-05 17:40:00'),
(9, 3, 5, 'LIKE', '2025-07-05 17:50:00'),
(10, 2, 6, 'LIKE', '2025-07-06 11:20:00');

-- 7. payment
INSERT INTO payment (id, member_id, target_member_id, imp_uid, merchant_uid, amount, category, status, pay_type, created_at, completed_at) VALUES
(1, 3, 2, 'imp_0000000001', 'ORD_20250710_001', 5000, '간식 쏘기', 'PAID', 'card', '2025-07-10 12:00:00', '2025-07-10 12:00:30'),
(2, 4, 2, 'imp_0000000002', 'ORD_20250711_001', 9900, '펫 클럽 구독', 'PAID', 'card', '2025-07-11 09:00:00', '2025-07-11 09:00:20'),
(3, 5, 2, NULL, 'ORD_20250712_001', 3000, '간식 쏘기', 'READY', 'card', '2025-07-12 15:00:00', '2025-07-12 15:00:00'),
(4, 3, 2, 'imp_0000000004', 'ORD_20250713_001', 5000, '간식 쏘기', 'FAILED', 'card', '2025-07-13 11:00:00', '2025-07-13 11:00:10'),
(5, 4, 5, 'imp_0000000005', 'ORD_20250714_001', 3000, '간식 쏘기', 'CANCELLED', 'point', '2025-07-14 16:00:00', '2025-07-14 16:05:00');

-- 8. subscription_plan
INSERT INTO subscription_plan (id, member_id, plan_name, price, description, status) VALUES
(1, 2, '베이직', 4900, '월간 전용 피드 + 감사 메시지', 'ACTIVE'),
(2, 2, '프리미엄', 9900, '전용 피드 + 월 1회 화상 만남 + 간식 배송', 'ACTIVE'),
(3, 5, '초코 팬클럽', 3900, '초코의 공놀이 하이라이트 영상 제공', 'ACTIVE'),
(4, 2, '올드플랜', 2900, '더 이상 판매하지 않는 플랜', 'DELETED');

-- 9. subscription
INSERT INTO subscription (id, member_id, target_member_id, plan_id, customer_uid, status, started_at, ended_at, next_billing_at, agreement) VALUES
(1, 4, 2, 2, 'cust_coco_001', 'ACTIVE', '2025-07-11 09:00:20', NULL, '2025-08-11 09:00:00', 1),
(2, 3, 2, 1, 'cust_nabi_001', 'ACTIVE', '2025-07-15 10:00:00', NULL, '2025-08-15 10:00:00', 1),
(3, 6, 5, 3, 'cust_kakao_001', 'CANCELLED', '2025-06-20 12:00:00', '2025-07-20 12:00:00', NULL, 0);

-- 10. follow
INSERT INTO follow (id, follower_id, following_id, created_at) VALUES
(1, 3, 2, '2025-07-01 09:00:00'),
(2, 4, 2, '2025-07-01 09:10:00'),
(3, 5, 2, '2025-07-01 09:20:00'),
(4, 2, 3, '2025-07-02 10:00:00'),
(5, 2, 5, '2025-07-02 10:05:00'),
(6, 6, 2, '2025-07-06 12:00:00'),
(7, 4, 5, '2025-07-05 08:00:00');

-- 11. notification
INSERT INTO notification (id, member_id, sender_id, notification_type, content, is_checked, created_at) VALUES
(1, 2, 3, 'FOLLOW', '나비가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-01 09:00:01'),
(2, 2, 3, 'COMMENT', '나비가 회원님의 게시글에 댓글을 남겼습니다.', 1, '2025-07-01 10:30:01'),
(3, 2, 3, 'DONATION', '나비가 간식 5,000원을 쏘셨습니다!', 0, '2025-07-10 12:00:31'),
(4, 2, 4, 'SUBSCRIPTION', '코코가 프리미엄 플랜을 구독했습니다.', 0, '2025-07-11 09:00:21'),
(5, 3, 2, 'FOLLOW', '멍치가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-02 10:00:01'),
(6, 2, 1, 'PET BIRTHDAY', '내일은 멍치의 생일이에요! 축하 메시지를 남겨보세요.', 0, '2025-03-14 09:00:00'),
(7, 5, 4, 'FOLLOW', '코코가 회원님을 팔로우하기 시작했습니다.', 0, '2025-07-05 08:00:01');

-- 12. chat_room
INSERT INTO chat_room (id, member1_id, member2_id, member1_exited, member2_exited, created_at) VALUES
(1, 2, 3, 0, 0, '2025-07-01 20:00:00'),
(2, 2, 4, 0, 0, '2025-07-11 10:00:00'),
(3, 4, 5, 0, 1, '2025-07-05 19:00:00');

-- 13. chat_message
INSERT INTO chat_message (id, room_id, sender_id, message, created_at) VALUES
(1, 1, 3, '멍치야 한강 산책 재밌었어?', '2025-07-01 20:01:00'),
(2, 1, 2, '응! 다음에 나비도 같이 가자~', '2025-07-01 20:02:00'),
(3, 1, 3, '좋아! 주말에 어때?', '2025-07-01 20:03:00'),
(4, 2, 4, '구독했어요! 프리미엄 콘텐츠 기대할게요.', '2025-07-11 10:01:00'),
(5, 2, 2, '환영해요 코코! 곧 새 영상 올릴게요.', '2025-07-11 10:05:00'),
(6, 3, 4, '초코야 공놀이 영상 공유해줘!', '2025-07-05 19:01:00'),
(7, 3, 5, '알겠어! 내일 올려줄게.', '2025-07-05 19:02:00');

-- 14. missing_pet_post
INSERT INTO missing_pet_post (id, member_id, missing_date, missing_address, detail, image_url, status, latitude, longitude, created_at, updated_at) VALUES
(1, 4, '2025-07-08', '경기도 성남시 분당구 정자동 공원 근처', '흰색 푸들, 빨간 목걸이 착용. 이름이 코코입니다.', 'https://s3.example.com/missing/1.jpg', 'MISSING', 37.3595000, 127.1052000, '2025-07-08 20:00:00', '2025-07-08 20:00:00'),
(2, 6, '2025-06-25', '서울시 종로구 청계천 일대', '회색 코숏, 왼쪽 귀에 작은 상처 있음.', 'https://s3.example.com/missing/2.jpg', 'FOUND', 37.5700000, 126.9820000, '2025-06-25 18:30:00', '2025-06-28 12:00:00'),
(3, 5, '2025-07-01', '부산시 해운대구 달맞이길', '갈색 믹스견. 찾아주셔서 감사합니다. 신고 취소합니다.', NULL, 'CANCELLED', 35.1587000, 129.1604000, '2025-07-01 07:00:00', '2025-07-01 22:00:00');

-- 15. missing_pet_report
INSERT INTO missing_pet_report (id, missing_pet_post_id, member_id, address, detail, image_url, sight_at, latitude, longitude, created_at, updated_at) VALUES
(1, 1, 2, '성남시 분당구 수내동 카페거리', '빨간 목걸이 흰 푸들 비슷한 아이를 봤어요. 사람 가까이 오진 않았습니다.', 'https://s3.example.com/report/1.jpg', '2025-07-09 08:30:00', 37.3781000, 127.1142000, '2025-07-09 09:00:00', '2025-07-09 09:00:00'),
(2, 1, 3, '성남시 분당구 정자역 3번 출구', '목격 후 바로 사진 찍었어요. 아직 근처에 있을 수 있어요.', 'https://s3.example.com/report/2.jpg', '2025-07-09 17:00:00', 37.3660000, 127.1082000, '2025-07-09 17:20:00', '2025-07-09 17:20:00'),
(3, 2, 3, '서울시 종로구 광장시장 근처', '회색 고양이 구조해서 보호소에 연락했습니다.', NULL, '2025-06-27 14:00:00', 37.5704000, 126.9996000, '2025-06-27 15:00:00', '2025-06-27 15:00:00');
