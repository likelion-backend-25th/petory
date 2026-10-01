-- 더미 데이터 (schema.sql 테이블 기준, FK 의존 순서)
-- password: password123 (BCrypt)

-- 1. member
INSERT INTO member (id, email, password, nickname, species, sex, birth_date, intro, profile_image, address, status, role, created_at, info_provide_agreement) VALUES
(1, 'admin@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '관리자냥', '고양이', '여', '2020-01-01', '사자그램 관리자입니다.', 'https://s3.example.com/profile/admin.jpg', '서울시 강남구', 'ACTIVE', 'ROLE_ADMIN', '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 'mungchi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '멍치', '개', '남', '2021-03-15', '산책 좋아하는 골든리트리버!', 'https://s3.example.com/profile/mungchi.jpg', '서울시 마포구', 'ACTIVE', 'ROLE_VIP', '2025-02-10 11:00:00', '2025-02-10 11:00:00'),
(3, 'nabi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '나비', '고양이', '여', '2022-07-20', '창가에서 낮잠 자는 게 취미예요.', 'https://s3.example.com/profile/nabi.jpg', '서울시 송파구', 'ACTIVE', 'ROLE_USER', '2025-03-05 09:30:00', NULL),
(4, 'coco@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '코코', '개', '여', '2020-11-08', '간식 없으면 시무룩해요.', 'https://s3.example.com/profile/coco.jpg', '경기도 성남시', 'ACTIVE', 'ROLE_USER', '2025-04-12 14:20:00', '2025-04-12 14:20:00'),
(5, 'choco@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '초코', '개', '남', '2019-05-30', '공놀이 최강자 🎾', 'https://s3.example.com/profile/choco.jpg', '부산시 해운대구', 'ACTIVE', 'ROLE_USER', '2025-05-01 16:00:00', NULL),
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
(1, 1, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(2, 1, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 1),
(3, 1, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 2),
(4, 2, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(5, 3, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0357.JPEG', 0),
(6, 3, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0352.JPEG', 1),
(7, 5, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 0),
(8, 6, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0);

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
INSERT INTO payment (id, member_id, target_member_id, payment_id, order_name, currency, total_amount, paid_amount, merchandise, status, transaction_id, pg_tx_id, receipt_url, fail_code, fail_message, cancel_amount, cancel_reason, created_at, paid_at, cancelled_at) VALUES
(1, 3, 2, 'pay_0000000001', 'ORD_20250710_001', 'KRW', 5000, 5000, '간식 쏘기', 'PAID', 'tx_0000000001', 'pg_0000000001', 'https://receipt.example.com/pay/1', NULL, NULL, NULL, NULL, '2025-07-10 12:00:00', '2025-07-10 12:00:30', NULL),
(2, 4, 2, 'pay_0000000002', 'ORD_20250711_001', 'KRW', 9900, 9900, '펫 클럽 구독', 'PAID', 'tx_0000000002', 'pg_0000000002', 'https://receipt.example.com/pay/2', NULL, NULL, NULL, NULL, '2025-07-11 09:00:00', '2025-07-11 09:00:20', NULL),
(3, 5, 2, 'pay_0000000003', 'ORD_20250712_001', 'KRW', 3000, NULL, '간식 쏘기', 'READY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-07-12 15:00:00', NULL, NULL),
(4, 3, 2, 'pay_0000000004', 'ORD_20250713_001', 'KRW', 5000, NULL, '간식 쏘기', 'FAILED', NULL, NULL, NULL, 'PAY_PROCESS_FAILED', '잔액 부족', NULL, NULL, '2025-07-13 11:00:00', NULL, NULL),
(5, 4, 5, 'pay_0000000005', 'ORD_20250714_001', 'KRW', 3000, 3000, '간식 쏘기', 'CANCELLED', 'tx_0000000005', 'pg_0000000005', 'https://receipt.example.com/pay/5', NULL, NULL, 3000, '단순 변심', '2025-07-14 16:00:00', '2025-07-14 16:00:30', '2025-07-14 16:05:00');

-- 8. cancel_payment
INSERT INTO cancel_payment (id, payment_id, cancellation_id, pg_cancellation_id, status, cancel_amount, reason, receipt_url, fail_code, fail_message, requested_at, cancelled_at) VALUES
(1, 5, 'cancel_0000000005', 'pg_cancel_0005', 'SUCCEEDED', 3000, '단순 변심', 'https://receipt.example.com/cancel/5', NULL, NULL, '2025-07-14 16:04:00', '2025-07-14 16:05:00');

-- 9. subscription_plan
INSERT INTO subscription_plan (id, member_id, plan_name, price, description, status) VALUES
(1, 2, '베이직', 4900, '월간 전용 피드 + 감사 메시지', 'ACTIVE'),
(2, 2, '프리미엄', 9900, '전용 피드 + 월 1회 화상 만남 + 간식 배송', 'ACTIVE'),
(3, 5, '초코 팬클럽', 3900, '초코의 공놀이 하이라이트 영상 제공', 'ACTIVE'),
(4, 2, '올드플랜', 2900, '더 이상 판매하지 않는 플랜', 'DELETED');

-- 10. subscription
INSERT INTO subscription (id, member_id, target_member_id, plan_id, billing_key, started_at, ended_at, next_billing_at, status, agreement) VALUES
(1, 4, 2, 2, 'billing_coco_001', '2025-07-11', NULL, '2025-08-11', 'ACTIVE', 1),
(2, 3, 2, 1, 'billing_nabi_001', '2025-07-15', NULL, '2025-08-15', 'ACTIVE', 1),
(3, 6, 5, 3, 'billing_kakao_001', '2025-06-20', '2025-07-20', NULL, 'CANCELLED', 0);

-- 11. follow
INSERT INTO follow (id, follower_id, following_id, created_at) VALUES
(1, 3, 2, '2025-07-01 09:00:00'),
(2, 4, 2, '2025-07-01 09:10:00'),
(3, 5, 2, '2025-07-01 09:20:00'),
(4, 2, 3, '2025-07-02 10:00:00'),
(5, 2, 5, '2025-07-02 10:05:00'),
(6, 6, 2, '2025-07-06 12:00:00'),
(7, 4, 5, '2025-07-05 08:00:00');

-- 12. notification
INSERT INTO notification (id, member_id, sender_id, notification_type, content, is_checked, created_at) VALUES
(1, 2, 3, 'FOLLOW', '나비가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-01 09:00:01'),
(2, 2, 3, 'COMMENT', '나비가 회원님의 게시글에 댓글을 남겼습니다.', 1, '2025-07-01 10:30:01'),
(3, 2, 3, 'DONATION', '나비가 간식 5,000원을 쏘셨습니다!', 0, '2025-07-10 12:00:31'),
(4, 2, 4, 'SUBSCRIPTION', '코코가 프리미엄 플랜을 구독했습니다.', 0, '2025-07-11 09:00:21'),
(5, 3, 2, 'FOLLOW', '멍치가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-02 10:00:01'),
(6, 2, 1, 'PET BIRTHDAY', '내일은 멍치의 생일이에요! 축하 메시지를 남겨보세요.', 0, '2025-03-14 09:00:00'),
(7, 5, 4, 'FOLLOW', '코코가 회원님을 팔로우하기 시작했습니다.', 0, '2025-07-05 08:00:01');

-- 13. chat_room
INSERT INTO chat_room (id, member1_id, member2_id, member1_exited, member2_exited, created_at) VALUES
(1, 2, 3, 0, 0, '2025-07-01 20:00:00'),
(2, 2, 4, 0, 0, '2025-07-11 10:00:00'),
(3, 4, 5, 0, 1, '2025-07-05 19:00:00');

-- 14. chat_message
INSERT INTO chat_message (id, room_id, sender_id, message, created_at) VALUES
(1, 1, 3, '멍치야 한강 산책 재밌었어?', '2025-07-01 20:01:00'),
(2, 1, 2, '응! 다음에 나비도 같이 가자~', '2025-07-01 20:02:00'),
(3, 1, 3, '좋아! 주말에 어때?', '2025-07-01 20:03:00'),
(4, 2, 4, '구독했어요! 프리미엄 콘텐츠 기대할게요.', '2025-07-11 10:01:00'),
(5, 2, 2, '환영해요 코코! 곧 새 영상 올릴게요.', '2025-07-11 10:05:00'),
(6, 3, 4, '초코야 공놀이 영상 공유해줘!', '2025-07-05 19:01:00'),
(7, 3, 5, '알겠어! 내일 올려줄게.', '2025-07-05 19:02:00');

-- 15. missing_pet_post
INSERT INTO missing_pet_post (id, member_id, missing_date, missing_address, detail, image_url, status, latitude, longitude, created_at, updated_at) VALUES
(1, 4, '2025-07-08', '경기도 성남시 분당구 정자동 공원 근처', '흰색 푸들, 빨간 목걸이 착용. 이름이 코코입니다.', 'https://s3.example.com/missing/1.jpg', 'MISSING', 37.3595000, 127.1052000, '2025-07-08 20:00:00', '2025-07-08 20:00:00'),
(2, 6, '2025-06-25', '서울시 종로구 청계천 일대', '회색 코숏, 왼쪽 귀에 작은 상처 있음.', 'https://s3.example.com/missing/2.jpg', 'FOUND', 37.5700000, 126.9820000, '2025-06-25 18:30:00', '2025-06-28 12:00:00'),
(3, 5, '2025-07-01', '부산시 해운대구 달맞이길', '갈색 믹스견. 찾아주셔서 감사합니다. 신고 취소합니다.', NULL, 'CANCELLED', 35.1587000, 129.1604000, '2025-07-01 07:00:00', '2025-07-01 22:00:00');

-- 16. missing_pet_report
INSERT INTO missing_pet_report (id, missing_pet_post_id, member_id, address, detail, image_url, sight_at, latitude, longitude, created_at, updated_at) VALUES
(1, 1, 2, '성남시 분당구 수내동 카페거리', '빨간 목걸이 흰 푸들 비슷한 아이를 봤어요. 사람 가까이 오진 않았습니다.', 'https://s3.example.com/report/1.jpg', '2025-07-09 08:30:00', 37.3781000, 127.1142000, '2025-07-09 09:00:00', '2025-07-09 09:00:00'),
(2, 2, 3, '성남시 분당구 정자역 3번 출구', '목격 후 바로 사진 찍었어요. 아직 근처에 있을 수 있어요.', 'https://s3.example.com/report/2.jpg', '2025-07-09 17:00:00', 37.3660000, 127.1082000, '2025-07-09 17:20:00', '2025-07-09 17:20:00'),
(3, 3, 3, '서울시 종로구 광장시장 근처', '회색 고양이 구조해서 보호소에 연락했습니다.', NULL, '2025-06-27 14:00:00', 37.5704000, 126.9996000, '2025-06-27 15:00:00', '2025-06-27 15:00:00');

-- ============================================================
-- 추가 더미 데이터 (무한 스크롤 / 랭킹 페이지 / 검색 테스트용)
-- ============================================================

-- 1. member (7 ~ 26)
INSERT INTO member (id, email, password, nickname, species, sex, birth_date, intro, profile_image, address, status, role, created_at, info_provide_agreement) VALUES
(7, 'dubu@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '두부', '고양이', '남', '2021-09-03', '두부처럼 하얀 페르시안이에요.', 'https://s3.example.com/profile/dubu.jpg', '서울시 성동구', 'ACTIVE', 'ROLE_USER', '2025-06-20 10:00:00', '2025-06-20 10:00:00'),
(8, 'bori@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '보리', '개', '여', '2022-02-14', '고집 센 시바견 보리입니다.', 'https://s3.example.com/profile/bori.jpg', '서울시 은평구', 'ACTIVE', 'ROLE_USER', '2025-06-22 11:30:00', NULL),
(9, 'haru@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '하루', '고양이', '여', '2023-04-01', '하루 종일 자는 러시안블루', 'https://s3.example.com/profile/haru.jpg', '인천시 연수구', 'ACTIVE', 'ROLE_USER', '2025-06-23 09:00:00', '2025-06-23 09:00:00'),
(10, 'tan@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '탄이', '개', '남', '2020-08-19', '짧은 다리 웰시코기 탄이', 'https://s3.example.com/profile/tan.jpg', '대전시 유성구', 'ACTIVE', 'ROLE_VIP', '2025-06-24 14:00:00', '2025-06-24 14:00:00'),
(11, 'mimi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '미미', '토끼', '여', '2023-06-10', '당근 먹방 전문 토끼', 'https://s3.example.com/profile/mimi.jpg', '서울시 관악구', 'ACTIVE', 'ROLE_USER', '2025-06-25 16:20:00', NULL),
(12, 'kongi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '콩이', '개', '여', '2021-12-25', '크리스마스에 태어난 말티즈', 'https://s3.example.com/profile/kongi.jpg', '경기도 수원시', 'ACTIVE', 'ROLE_USER', '2025-06-26 10:10:00', '2025-06-26 10:10:00'),
(13, 'leo@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '레오', '고양이', '남', '2020-03-03', '사냥놀이 좋아하는 뱅갈고양이', 'https://s3.example.com/profile/leo.jpg', '서울시 용산구', 'ACTIVE', 'ROLE_VIP', '2025-06-27 12:00:00', '2025-06-27 12:00:00'),
(14, 'nuri@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '누리', '개', '남', '2018-10-10', '듬직한 진돗개 형 누리', 'https://s3.example.com/profile/nuri.jpg', '광주시 북구', 'ACTIVE', 'ROLE_USER', '2025-06-28 08:30:00', NULL),
(15, 'pudding@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '푸딩', '햄스터', '여', '2024-02-02', '쳇바퀴 마라토너 햄스터', 'https://s3.example.com/profile/pudding.jpg', '서울시 동작구', 'ACTIVE', 'ROLE_USER', '2025-06-29 19:00:00', '2025-06-29 19:00:00'),
(16, 'sori@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '소리', '앵무새', '남', '2022-05-05', '말하는 앵무새 소리', 'https://s3.example.com/profile/sori.jpg', '대구시 수성구', 'ACTIVE', 'ROLE_USER', '2025-06-30 13:40:00', NULL),
(17, 'mango@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '망고', '고양이', '여', '2022-08-08', '햇살 좋아하는 치즈냥 망고', 'https://s3.example.com/profile/mango.jpg', '경기도 고양시', 'ACTIVE', 'ROLE_USER', '2025-07-01 09:20:00', '2025-07-01 09:20:00'),
(18, NULL, NULL, '또또', '개', '남', '2023-03-20', '카카오로 가입한 비숑 또또', 'https://s3.example.com/profile/ttoddo.jpg', '서울시 강서구', 'ACTIVE', 'ROLE_USER', '2025-07-02 10:00:00', '2025-07-02 10:00:00'),
(19, NULL, NULL, '루루', '고양이', '여', '2021-11-11', '구글로 가입한 스코티시폴드', 'https://s3.example.com/profile/rulu.jpg', '경기도 용인시', 'ACTIVE', 'ROLE_USER', '2025-07-03 11:00:00', NULL),
(20, 'bongsik@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '봉식', '개', '남', '2019-07-07', '먹는 게 제일 좋은 비글', 'https://s3.example.com/profile/bongsik.jpg', '울산시 남구', 'ACTIVE', 'ROLE_USER', '2025-07-04 15:00:00', '2025-07-04 15:00:00'),
(21, 'yuki@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '유키', '고양이', '여', '2022-12-01', '눈처럼 하얀 터키시앙고라', 'https://s3.example.com/profile/yuki.jpg', '강원도 춘천시', 'ACTIVE', 'ROLE_USER', '2025-07-05 10:30:00', NULL),
(22, 'dalgi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '딸기', '개', '여', '2023-05-15', '산책 좋아하는 포메라니안', 'https://s3.example.com/profile/dalgi.jpg', '서울시 노원구', 'ACTIVE', 'ROLE_USER', '2025-07-05 18:00:00', '2025-07-05 18:00:00'),
(23, 'gom@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '곰이', '개', '남', '2020-01-20', '곰처럼 큰 사모예드', 'https://s3.example.com/profile/gom.jpg', '경기도 파주시', 'ACTIVE', 'ROLE_USER', '2025-07-06 09:00:00', NULL),
(24, 'kiwi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '키위', '앵무새', '여', '2023-09-09', '어깨 위가 제일 좋은 모란앵무', 'https://s3.example.com/profile/kiwi.jpg', '제주시 연동', 'ACTIVE', 'ROLE_USER', '2025-07-06 14:00:00', '2025-07-06 14:00:00'),
(25, NULL, NULL, '밤이', '고양이', '남', '2024-01-05', '밤에만 활발한 턱시도', 'https://s3.example.com/profile/bami.jpg', '서울시 서대문구', 'ACTIVE', 'ROLE_USER', '2025-07-07 22:00:00', NULL),
(26, 'hodu@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '호두', '고슴도치', '남', '2023-10-31', '가시는 따갑지만 마음은 따뜻해요', 'https://s3.example.com/profile/hodu.jpg', '세종시 도담동', 'ACTIVE', 'ROLE_USER', '2025-07-08 10:00:00', '2025-07-08 10:00:00');

-- 2. linked_account
INSERT INTO linked_account (id, member_id, provider, provider_user_id, provider_email, created_at) VALUES
(4, 18, 'KAKAO', 'kakao_100000018', 'ttoddo@kakao.com', '2025-07-02 10:00:00'),
(5, 19, 'GOOGLE', 'google_100000019', 'rulu@gmail.com', '2025-07-03 11:00:00'),
(6, 25, 'KAKAO', 'kakao_100000025', 'bami@kakao.com', '2025-07-07 22:00:00'),
(7, 13, 'GOOGLE', 'google_100000013', 'leo@gmail.com', '2025-06-27 12:05:00');

-- 3. post_main (일반 피드 type=1: 7 ~ 36 / QnA type=2: 37 ~ 46)
INSERT INTO post_main (id, member_id, type, content, bgm_url, is_subscriber_only, hashtags, created_at, updated_at) VALUES
(7, 7, 1, '두부 목욕한 날. 표정이 세상 억울해요 ㅋㅋ', NULL, 0, '#고양이 #목욕 #페르시안', '2025-07-07 09:10:00', '2025-07-07 09:10:00'),
(8, 8, 1, '보리랑 아침 산책! 오늘도 집에 안 가겠다고 버텼어요', NULL, 0, '#강아지 #산책 #시바견', '2025-07-07 19:40:00', '2025-07-07 19:40:00'),
(9, 9, 1, '하루는 오늘도 18시간 수면 중', NULL, 0, '#고양이 #낮잠 #러시안블루', '2025-07-08 13:00:00', '2025-07-08 13:00:00'),
(10, 10, 1, '탄이의 짧은 다리로 계단 오르기 챌린지', 'https://s3.example.com/bgm/happy.mp3', 0, '#강아지 #웰시코기 #챌린지', '2025-07-08 18:20:00', '2025-07-08 18:20:00'),
(11, 11, 1, '당근 한 개 순삭하는 미미 먹방', NULL, 0, '#토끼 #먹방 #당근', '2025-07-09 12:00:00', '2025-07-09 12:00:00'),
(12, 12, 1, '콩이 미용하고 왔어요! 곰돌이컷 어때요?', NULL, 0, '#강아지 #말티즈 #미용', '2025-07-09 17:30:00', '2025-07-09 17:30:00'),
(13, 13, 1, '레오의 사냥놀이 하이라이트. 낚싯대 장난감 3개째 부숨', NULL, 0, '#고양이 #뱅갈 #사냥놀이', '2025-07-10 21:00:00', '2025-07-10 21:00:00'),
(14, 14, 1, '누리와 등산 다녀왔습니다. 정상에서 한 컷', NULL, 0, '#강아지 #진돗개 #등산', '2025-07-11 15:00:00', '2025-07-11 15:00:00'),
(15, 15, 1, '푸딩이 쳇바퀴 기록 갱신! 밤새 달렸어요', NULL, 0, '#햄스터 #쳇바퀴 #일상', '2025-07-12 08:00:00', '2025-07-12 08:00:00'),
(16, 16, 1, '소리가 드디어 "안녕"을 말했어요!!', 'https://s3.example.com/bgm/happy.mp3', 0, '#앵무새 #말하는새 #일상', '2025-07-12 20:10:00', '2025-07-12 20:10:00'),
(17, 10, 1, '펫클럽 전용) 탄이 엉덩이 흔들기 풀버전', 'https://s3.example.com/bgm/asmr.mp3', 1, '#펫클럽 #웰시코기 #강아지', '2025-07-13 19:00:00', '2025-07-13 19:00:00'),
(18, 17, 1, '망고 햇살 맛집에서 식빵 굽는 중', NULL, 0, '#고양이 #치즈냥 #식빵', '2025-07-14 10:30:00', '2025-07-14 10:30:00'),
(19, 18, 1, '또또 첫 수제간식 만들어줬어요', NULL, 0, '#강아지 #비숑 #간식', '2025-07-15 16:00:00', '2025-07-15 16:00:00'),
(20, 19, 1, '루루 귀 접힌 거 너무 귀엽지 않나요', NULL, 0, '#고양이 #스코티시폴드 #일상', '2025-07-16 11:20:00', '2025-07-16 11:20:00'),
(21, 2, 1, '멍치 수영 데뷔! 물 만난 골든', 'https://s3.example.com/bgm/happy.mp3', 0, '#강아지 #골든리트리버 #수영', '2025-07-17 14:00:00', '2025-07-17 14:00:00'),
(22, 20, 1, '봉식이 간식 앞에서 기다려 성공... 3초', NULL, 0, '#강아지 #비글 #간식 #훈련', '2025-07-18 18:45:00', '2025-07-18 18:45:00'),
(23, 21, 1, '첫눈 같은 유키 털 빗질 후기', NULL, 0, '#고양이 #터키시앙고라 #빗질', '2025-07-19 09:00:00', '2025-07-19 09:00:00'),
(24, 5, 1, '펫클럽 전용) 초코 공놀이 슬로우모션', NULL, 1, '#펫클럽 #공놀이 #초코', '2025-07-20 17:00:00', '2025-07-20 17:00:00'),
(25, 22, 1, '딸기 산책 가방 새로 샀어요', NULL, 0, '#강아지 #포메라니안 #산책', '2025-07-21 12:10:00', '2025-07-21 12:10:00'),
(26, 23, 1, '곰이 털갈이 시즌... 청소기가 쉬지를 않아요', NULL, 0, '#강아지 #사모예드 #털갈이', '2025-07-22 20:00:00', '2025-07-22 20:00:00'),
(27, 24, 1, '키위가 어깨에서 내려오질 않아요', NULL, 0, '#앵무새 #모란앵무 #일상', '2025-07-23 08:40:00', '2025-07-23 08:40:00'),
(28, 3, 1, '나비 생일 파티 했어요! 참치 케이크', NULL, 0, '#고양이 #생일 #간식', '2025-07-24 19:30:00', '2025-07-24 19:30:00'),
(29, 25, 1, '밤이 새벽 우다다 실황', NULL, 0, '#고양이 #턱시도 #우다다', '2025-07-25 02:10:00', '2025-07-25 02:10:00'),
(30, 26, 1, '호두 목욕 후 수건 속에 쏙', NULL, 0, '#고슴도치 #목욕 #일상', '2025-07-26 15:30:00', '2025-07-26 15:30:00'),
(31, 4, 1, '코코 한강 피크닉 다녀왔어요', NULL, 0, '#강아지 #산책 #한강', '2025-07-27 17:00:00', '2025-07-27 17:00:00'),
(32, 7, 1, '두부랑 창밖 구경하는 오후', NULL, 0, '#고양이 #일상 #창가', '2025-07-28 14:00:00', '2025-07-28 14:00:00'),
(33, 8, 1, '보리 생애 첫 수영장! 처음엔 무서워했는데 금방 적응', NULL, 0, '#강아지 #시바견 #수영', '2025-07-29 11:00:00', '2025-07-29 11:00:00'),
(34, 13, 1, '레오 캣타워 조립 완료. 근데 박스를 더 좋아함', NULL, 0, '#고양이 #캣타워 #뱅갈', '2025-07-30 21:30:00', '2025-07-30 21:30:00'),
(35, 2, 1, '펫클럽 전용) 멍치 하루 브이로그', 'https://s3.example.com/bgm/happy.mp3', 1, '#펫클럽 #강아지 #브이로그', '2025-07-31 20:00:00', '2025-07-31 20:00:00'),
(36, 12, 1, '콩이랑 반려견 카페 다녀왔어요', NULL, 0, '#강아지 #말티즈 #카페', '2025-08-01 13:00:00', '2025-08-01 13:00:00'),
(37, 9, 2, '고양이가 갑자기 밥을 안 먹어요. 병원 가야 할까요?', NULL, 0, '#QandA #고양이 #식욕부진', '2025-07-10 09:00:00', '2025-07-10 09:00:00'),
(38, 11, 2, '토끼 건초 어떤 거 먹이세요?', NULL, 0, '#QandA #토끼 #건초', '2025-07-12 10:00:00', '2025-07-12 10:00:00'),
(39, 14, 2, '진돗개 분리불안 훈련 팁 있을까요?', NULL, 0, '#QandA #강아지 #분리불안', '2025-07-14 21:00:00', '2025-07-14 21:00:00'),
(40, 15, 2, '햄스터 케이지 바닥재 추천해주세요', NULL, 0, '#QandA #햄스터 #바닥재', '2025-07-16 08:30:00', '2025-07-16 08:30:00'),
(41, 17, 2, '고양이 스크래쳐 어떤 재질이 좋나요?', NULL, 0, '#QandA #고양이 #스크래쳐', '2025-07-18 13:20:00', '2025-07-18 13:20:00'),
(42, 20, 2, '비글 산책 하루 몇 번 시키세요?', NULL, 0, '#QandA #강아지 #산책', '2025-07-20 07:50:00', '2025-07-20 07:50:00'),
(43, 16, 2, '앵무새가 깃털을 뽑는데 왜 그럴까요?', NULL, 0, '#QandA #앵무새 #건강', '2025-07-22 19:00:00', '2025-07-22 19:00:00'),
(44, 22, 2, '강아지 슬개골 탈구 수술 해보신 분 계신가요?', NULL, 0, '#QandA #강아지 #슬개골', '2025-07-24 22:00:00', '2025-07-24 22:00:00'),
(45, 26, 2, '고슴도치 겨울철 온도 관리 어떻게 하세요?', NULL, 0, '#QandA #고슴도치 #온도관리', '2025-07-27 10:00:00', '2025-07-27 10:00:00'),
(46, 19, 2, '고양이 중성화 후 관리 팁 부탁드려요', NULL, 0, '#QandA #고양이 #중성화', '2025-07-30 15:00:00', '2025-07-30 15:00:00');

-- 4. post_image
INSERT INTO post_image (id, post_id, image_url, sort_order) VALUES
(9, 7, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(10, 8, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(11, 8, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 1),
(12, 9, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0),
(13, 10, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 0),
(14, 11, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0357.JPEG', 0),
(15, 12, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 0),
(16, 12, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0352.JPEG', 1),
(17, 13, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(18, 14, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(19, 15, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0357.JPEG', 0),
(20, 16, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0352.JPEG', 0),
(21, 17, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 0),
(22, 17, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 1),
(23, 18, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0),
(24, 19, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0357.JPEG', 0),
(25, 20, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(26, 21, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(27, 21, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 1),
(28, 21, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 2),
(29, 22, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 0),
(30, 23, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0),
(31, 24, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 0),
(32, 25, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 0),
(33, 26, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 0),
(34, 27, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0352.JPEG', 0),
(35, 28, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(36, 28, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 1),
(37, 29, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0),
(38, 30, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0357.JPEG', 0),
(39, 31, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(40, 31, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 1),
(41, 32, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(42, 33, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_7245.JPEG', 0),
(43, 34, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_9062.JPEG', 0),
(44, 35, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_6502.JPEG', 0),
(45, 35, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0612.JPEG', 1),
(46, 36, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0352.JPEG', 0),
(47, 37, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_8933.JPEG', 0),
(48, 44, 'https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/posts/IMG_0278.JPEG', 0);

-- 5. comment
INSERT INTO comment (id, post_id, member_id, content, created_at) VALUES
(8, 7, 9, '두부 표정 너무 웃겨요 ㅋㅋㅋ', '2025-07-07 09:40:00'),
(9, 7, 17, '목욕 후 억울한 고양이 국룰', '2025-07-07 10:00:00'),
(10, 8, 2, '보리 버티기 장인이네요', '2025-07-07 20:00:00'),
(11, 9, 13, '레오도 하루만큼만 자줬으면...', '2025-07-08 13:30:00'),
(12, 10, 2, '탄이 계단 챌린지 응원합니다!', '2025-07-08 18:50:00'),
(13, 10, 14, '짧은 다리 최고 귀여움', '2025-07-08 19:10:00'),
(14, 11, 15, '미미 먹는 소리 들리는 것 같아요', '2025-07-09 12:30:00'),
(15, 12, 22, '곰돌이컷 찰떡이에요!', '2025-07-09 18:00:00'),
(16, 12, 4, '어디 미용실인지 궁금해요', '2025-07-09 18:20:00'),
(17, 13, 7, '낚싯대 3개 ㄷㄷ', '2025-07-10 21:30:00'),
(18, 14, 8, '누리 듬직하다!', '2025-07-11 15:40:00'),
(19, 16, 24, '키위도 언젠가 말해주겠죠?', '2025-07-12 20:40:00'),
(20, 16, 11, '와 진짜 신기해요', '2025-07-12 21:00:00'),
(21, 17, 4, '구독한 보람이 있네요 ㅎㅎ', '2025-07-18 09:40:00'),
(22, 18, 21, '식빵 굽는 자세 완벽', '2025-07-14 11:00:00'),
(23, 19, 20, '레시피 공유해주세요!', '2025-07-15 16:30:00'),
(24, 21, 3, '멍치 수영 폼 미쳤다', '2025-07-17 14:20:00'),
(25, 21, 10, '탄이도 데려가고 싶어요', '2025-07-17 15:00:00'),
(26, 21, 23, '골든은 역시 물을 좋아하네요', '2025-07-17 15:30:00'),
(27, 22, 12, '3초면 충분히 대단한 거예요', '2025-07-18 19:00:00'),
(28, 23, 9, '빗질하면 털이 한 마리 더 나오죠', '2025-07-19 09:30:00'),
(29, 26, 22, '저희 딸기도 털갈이 중이에요 ㅠㅠ', '2025-07-22 20:30:00'),
(30, 28, 2, '나비 생일 축하해!!', '2025-07-24 19:45:00'),
(31, 28, 7, '참치 케이크 부럽다', '2025-07-24 20:00:00'),
(32, 28, 17, '생일 축하해요 나비!', '2025-07-24 20:15:00'),
(33, 29, 13, '새벽 우다다는 고양이의 의무', '2025-07-25 08:00:00'),
(34, 30, 15, '수건 속 호두 귀여움 폭발', '2025-07-26 16:00:00'),
(35, 31, 3, '다음엔 같이 가요 코코!', '2025-07-27 17:30:00'),
(36, 33, 2, '보리 수영 데뷔 축하해요!', '2025-07-29 11:30:00'),
(37, 34, 9, '고양이는 박스가 진리죠', '2025-07-30 22:00:00'),
(38, 36, 18, '어느 카페인지 알려주세요~', '2025-08-01 13:30:00'),
(39, 37, 3, '하루 이상 안 먹으면 병원 꼭 가보세요.', '2025-07-10 09:30:00'),
(40, 37, 13, '사료 바꾸셨으면 그것 때문일 수도 있어요.', '2025-07-10 10:00:00'),
(41, 38, 26, '티모시 1번초 먹이고 있어요!', '2025-07-12 11:00:00'),
(42, 39, 8, '짧게 외출하는 연습부터 해보세요.', '2025-07-14 21:30:00'),
(43, 39, 2, '노즈워크 장난감 추천드려요.', '2025-07-14 22:00:00'),
(44, 40, 11, '종이 베딩이 먼지가 적어서 좋아요.', '2025-07-16 09:00:00'),
(45, 41, 7, '골판지 스크래쳐가 제일 반응 좋았어요.', '2025-07-18 14:00:00'),
(46, 42, 14, '저는 아침저녁 두 번 30분씩 해요.', '2025-07-20 08:30:00'),
(47, 43, 24, '스트레스일 수 있으니 장난감을 늘려보세요.', '2025-07-22 19:30:00'),
(48, 44, 4, '저희 코코 작년에 했어요. 회복 잘 됐어요!', '2025-07-24 22:30:00'),
(49, 45, 15, '온열매트 꼭 깔아주세요.', '2025-07-27 10:30:00'),
(50, 46, 21, '넥카라 꼭 씌우시고 핥지 않게 봐주세요.', '2025-07-30 15:30:00');

-- 6. post_interaction (LIKE / BOOKMARK / VIEW)
INSERT INTO post_interaction (id, member_id, post_id, interaction_type, created_at) VALUES
(11, 9, 7, 'LIKE', '2025-07-07 09:35:00'),
(12, 17, 7, 'LIKE', '2025-07-07 09:55:00'),
(13, 21, 7, 'LIKE', '2025-07-07 10:20:00'),
(14, 9, 7, 'VIEW', '2025-07-07 09:30:00'),
(15, 17, 7, 'VIEW', '2025-07-07 09:50:00'),
(16, 21, 7, 'VIEW', '2025-07-07 10:15:00'),
(17, 2, 7, 'VIEW', '2025-07-07 11:00:00'),
(18, 2, 8, 'LIKE', '2025-07-07 19:58:00'),
(19, 14, 8, 'LIKE', '2025-07-07 20:30:00'),
(20, 2, 8, 'VIEW', '2025-07-07 19:55:00'),
(21, 14, 8, 'VIEW', '2025-07-07 20:25:00'),
(22, 3, 8, 'VIEW', '2025-07-07 21:00:00'),
(23, 2, 10, 'LIKE', '2025-07-08 18:45:00'),
(24, 14, 10, 'LIKE', '2025-07-08 19:05:00'),
(25, 22, 10, 'LIKE', '2025-07-08 19:30:00'),
(26, 23, 10, 'LIKE', '2025-07-08 20:00:00'),
(27, 22, 10, 'BOOKMARK', '2025-07-08 19:31:00'),
(28, 2, 10, 'VIEW', '2025-07-08 18:40:00'),
(29, 14, 10, 'VIEW', '2025-07-08 19:00:00'),
(30, 22, 10, 'VIEW', '2025-07-08 19:25:00'),
(31, 23, 10, 'VIEW', '2025-07-08 19:55:00'),
(32, 12, 10, 'VIEW', '2025-07-08 21:00:00'),
(33, 4, 12, 'LIKE', '2025-07-09 18:15:00'),
(34, 22, 12, 'LIKE', '2025-07-09 17:55:00'),
(35, 4, 12, 'BOOKMARK', '2025-07-09 18:16:00'),
(36, 7, 13, 'LIKE', '2025-07-10 21:25:00'),
(37, 9, 13, 'LIKE', '2025-07-10 22:00:00'),
(38, 25, 13, 'LIKE', '2025-07-11 01:00:00'),
(39, 11, 16, 'LIKE', '2025-07-12 20:55:00'),
(40, 24, 16, 'LIKE', '2025-07-12 20:35:00'),
(41, 3, 16, 'LIKE', '2025-07-12 22:00:00'),
(42, 24, 16, 'BOOKMARK', '2025-07-12 20:36:00'),
(43, 4, 17, 'LIKE', '2025-07-18 09:35:00'),
(44, 14, 17, 'LIKE', '2025-07-18 10:10:00'),
(45, 21, 18, 'LIKE', '2025-07-14 10:55:00'),
(46, 7, 18, 'LIKE', '2025-07-14 11:30:00'),
(47, 3, 21, 'LIKE', '2025-07-17 14:15:00'),
(48, 4, 21, 'LIKE', '2025-07-17 14:30:00'),
(49, 5, 21, 'LIKE', '2025-07-17 14:40:00'),
(50, 10, 21, 'LIKE', '2025-07-17 14:55:00'),
(51, 23, 21, 'LIKE', '2025-07-17 15:25:00'),
(52, 8, 21, 'LIKE', '2025-07-17 16:00:00'),
(53, 3, 21, 'BOOKMARK', '2025-07-17 14:16:00'),
(54, 10, 21, 'BOOKMARK', '2025-07-17 14:56:00'),
(55, 3, 21, 'VIEW', '2025-07-17 14:10:00'),
(56, 4, 21, 'VIEW', '2025-07-17 14:25:00'),
(57, 5, 21, 'VIEW', '2025-07-17 14:35:00'),
(58, 10, 21, 'VIEW', '2025-07-17 14:50:00'),
(59, 23, 21, 'VIEW', '2025-07-17 15:20:00'),
(60, 8, 21, 'VIEW', '2025-07-17 15:55:00'),
(61, 9, 21, 'VIEW', '2025-07-17 17:00:00'),
(62, 12, 22, 'LIKE', '2025-07-18 18:55:00'),
(63, 18, 22, 'LIKE', '2025-07-18 19:30:00'),
(64, 22, 26, 'LIKE', '2025-07-22 20:25:00'),
(65, 2, 28, 'LIKE', '2025-07-24 19:40:00'),
(66, 7, 28, 'LIKE', '2025-07-24 19:58:00'),
(67, 17, 28, 'LIKE', '2025-07-24 20:10:00'),
(68, 9, 28, 'LIKE', '2025-07-24 21:00:00'),
(69, 17, 28, 'BOOKMARK', '2025-07-24 20:11:00'),
(70, 13, 29, 'LIKE', '2025-07-25 07:55:00'),
(71, 3, 31, 'LIKE', '2025-07-27 17:25:00'),
(72, 2, 31, 'LIKE', '2025-07-27 18:00:00'),
(73, 2, 33, 'LIKE', '2025-07-29 11:25:00'),
(74, 14, 33, 'LIKE', '2025-07-29 12:00:00'),
(75, 4, 35, 'LIKE', '2025-07-31 20:30:00'),
(76, 3, 35, 'LIKE', '2025-07-31 21:00:00'),
(77, 18, 36, 'LIKE', '2025-08-01 13:25:00'),
(78, 22, 36, 'LIKE', '2025-08-01 14:00:00'),
(79, 3, 37, 'LIKE', '2025-07-10 09:25:00'),
(80, 13, 37, 'LIKE', '2025-07-10 09:55:00'),
(81, 17, 37, 'BOOKMARK', '2025-07-10 11:00:00'),
(82, 3, 37, 'VIEW', '2025-07-10 09:20:00'),
(83, 13, 37, 'VIEW', '2025-07-10 09:50:00'),
(84, 17, 37, 'VIEW', '2025-07-10 10:55:00'),
(85, 8, 39, 'LIKE', '2025-07-14 21:25:00'),
(86, 20, 39, 'BOOKMARK', '2025-07-15 08:00:00'),
(87, 4, 44, 'LIKE', '2025-07-24 22:25:00'),
(88, 12, 44, 'LIKE', '2025-07-25 09:00:00'),
(89, 12, 44, 'BOOKMARK', '2025-07-25 09:01:00');

-- 7. payment
INSERT INTO payment (id, member_id, target_member_id, payment_id, order_name, currency, total_amount, paid_amount, merchandise, status, transaction_id, pg_tx_id, receipt_url, fail_code, fail_message, cancel_amount, cancel_reason, created_at, paid_at, cancelled_at) VALUES
(6, 7, 2, 'ORD_1752634800000_a1b2c3', '간식 쏘기', 'KRW', 3000, 3000, 'singlePayment', 'PAID', 'tx_0000000006', 'pg_0000000006', 'https://receipt.example.com/pay/6', NULL, NULL, NULL, NULL, '2025-07-16 12:00:00', '2025-07-16 12:00:25', NULL),
(7, 9, 13, 'ORD_1752724800000_d4e5f6', '간식 쏘기', 'KRW', 5000, 5000, 'singlePayment', 'PAID', 'tx_0000000007', 'pg_0000000007', 'https://receipt.example.com/pay/7', NULL, NULL, NULL, NULL, '2025-07-17 13:00:00', '2025-07-17 13:00:25', NULL),
(8, 14, 10, 'ORD_1752800400000_g7h8i9', '펫 클럽 구독', 'KRW', 9900, 9900, 'automaticPayment', 'PAID', 'tx_0000000008', 'pg_0000000008', 'https://receipt.example.com/pay/8', NULL, NULL, NULL, NULL, '2025-07-18 10:00:00', '2025-07-18 10:00:20', NULL),
(9, 22, 10, 'ORD_1752818400000_j1k2l3', '펫 클럽 구독', 'KRW', 4900, 4900, 'automaticPayment', 'PAID', 'tx_0000000009', 'pg_0000000009', 'https://receipt.example.com/pay/9', NULL, NULL, NULL, NULL, '2025-07-18 15:00:00', '2025-07-18 15:00:20', NULL),
(10, 17, 2, 'ORD_1752922800000_m4n5o6', '간식 쏘기', 'KRW', 10000, 10000, 'singlePayment', 'PAID', 'tx_0000000010', 'pg_0000000010', 'https://receipt.example.com/pay/10', NULL, NULL, NULL, NULL, '2025-07-19 20:00:00', '2025-07-19 20:00:25', NULL),
(11, 12, 7, 'ORD_1752976800000_p7q8r9', '간식 쏘기', 'KRW', 3000, NULL, 'singlePayment', 'READY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-07-20 11:00:00', NULL, NULL),
(12, 20, 5, 'ORD_1753002000000_s1t2u3', '펫 클럽 구독', 'KRW', 3900, 3900, 'automaticPayment', 'PAID', 'tx_0000000012', 'pg_0000000012', 'https://receipt.example.com/pay/12', NULL, NULL, NULL, NULL, '2025-07-20 18:00:00', '2025-07-20 18:00:20', NULL),
(13, 3, 13, 'ORD_1753056000000_v4w5x6', '간식 쏘기', 'KRW', 5000, NULL, 'singlePayment', 'FAILED', NULL, NULL, NULL, 'PAY_PROCESS_FAILED', '카드 한도 초과', NULL, NULL, '2025-07-21 09:00:00', NULL, NULL),
(14, 23, 2, 'ORD_1753160400000_y7z8a9', '간식 쏘기', 'KRW', 5000, 5000, 'singlePayment', 'PAID', 'tx_0000000014', 'pg_0000000014', 'https://receipt.example.com/pay/14', NULL, NULL, NULL, NULL, '2025-07-22 14:00:00', '2025-07-22 14:00:25', NULL),
(15, 4, 10, 'ORD_1752798600000_b1c2d3', '펫 클럽 구독', 'KRW', 9900, 9900, 'automaticPayment', 'PAID', 'tx_0000000015', 'pg_0000000015', 'https://receipt.example.com/pay/15', NULL, NULL, NULL, NULL, '2025-07-18 09:30:00', '2025-07-18 09:30:20', NULL),
(16, 21, 17, 'ORD_1753232400000_e4f5g6', '간식 쏘기', 'KRW', 3000, 3000, 'singlePayment', 'CANCELLED', 'tx_0000000016', 'pg_0000000016', 'https://receipt.example.com/pay/16', NULL, NULL, 3000, '중복 결제', '2025-07-23 10:00:00', '2025-07-23 10:00:20', '2025-07-23 10:10:00'),
(17, 10, 2, 'ORD_1753358400000_h7i8j9', '간식 쏘기', 'KRW', 20000, 20000, 'singlePayment', 'PAID', 'tx_0000000017', 'pg_0000000017', 'https://receipt.example.com/pay/17', NULL, NULL, NULL, NULL, '2025-07-24 21:00:00', '2025-07-24 21:00:30', NULL),
(18, 25, 9, 'ORD_1753380000000_k1l2m3', '간식 쏘기', 'KRW', 1000, NULL, 'singlePayment', 'READY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-07-25 03:00:00', NULL, NULL),
(19, 18, 12, 'ORD_1753502400000_n4o5p6', '간식 쏘기', 'KRW', 5000, NULL, 'singlePayment', 'FAILED', NULL, NULL, NULL, 'PAY_PROCESS_CANCELED', '사용자가 결제를 취소했습니다.', NULL, NULL, '2025-07-26 13:00:00', NULL, NULL),
(20, 16, 24, 'ORD_1753574400000_q7r8s9', '간식 쏘기', 'KRW', 2000, 2000, 'singlePayment', 'PAID', 'tx_0000000020', 'pg_0000000020', 'https://receipt.example.com/pay/20', NULL, NULL, NULL, NULL, '2025-07-27 09:00:00', '2025-07-27 09:00:20', NULL);

-- 8. cancel_payment
INSERT INTO cancel_payment (id, payment_id, cancellation_id, pg_cancellation_id, status, cancel_amount, reason, receipt_url, fail_code, fail_message, requested_at, cancelled_at) VALUES
(2, 16, 'cancel_0000000016', 'pg_cancel_0016', 'SUCCEEDED', 3000, '중복 결제', 'https://receipt.example.com/cancel/16', NULL, NULL, '2025-07-23 10:09:00', '2025-07-23 10:10:00');

-- 9. subscription_plan
INSERT INTO subscription_plan (id, member_id, plan_name, price, description, status) VALUES
(5, 10, '탄이 베이직', 4900, '탄이 전용 피드 열람', 'ACTIVE'),
(6, 10, '탄이 프리미엄', 9900, '전용 피드 + 월간 굿즈 배송', 'ACTIVE'),
(7, 13, '레오 사냥단', 3900, '레오 사냥놀이 풀영상 제공', 'ACTIVE'),
(8, 21, '유키 팬클럽', 2900, '유키 빗질 ASMR 영상 제공', 'ACTIVE');

-- 10. subscription
INSERT INTO subscription (id, member_id, target_member_id, plan_id, billing_key, started_at, ended_at, next_billing_at, status, agreement) VALUES
(4, 14, 10, 6, 'billing_nuri_001', '2025-07-18 10:00:20', NULL, '2025-08-18 10:00:00', 'ACTIVE', 1),
(5, 22, 10, 5, 'billing_dalgi_001', '2025-07-18 15:00:20', NULL, '2025-08-18 15:00:00', 'ACTIVE', 1),
(6, 4, 10, 6, 'billing_coco_002', '2025-07-18 09:30:20', NULL, '2025-08-18 09:30:00', 'ACTIVE', 1),
(7, 20, 5, 3, 'billing_bongsik_001', '2025-07-20 18:00:20', NULL, '2025-08-20 18:00:00', 'ACTIVE', 1),
(8, 9, 13, 7, 'billing_haru_001', '2025-06-25 10:00:00', '2025-07-25 10:00:00', NULL, 'CANCELLED', 0);

-- 11. follow
INSERT INTO follow (id, follower_id, following_id, created_at) VALUES
(8, 7, 2, '2025-07-07 12:00:00'),
(9, 8, 2, '2025-07-07 20:05:00'),
(10, 9, 2, '2025-07-08 09:00:00'),
(11, 10, 2, '2025-07-08 21:00:00'),
(12, 12, 2, '2025-07-09 10:00:00'),
(13, 13, 2, '2025-07-10 12:00:00'),
(14, 14, 2, '2025-07-11 16:00:00'),
(15, 17, 2, '2025-07-14 12:00:00'),
(16, 20, 2, '2025-07-18 19:00:00'),
(17, 22, 2, '2025-07-21 13:00:00'),
(18, 23, 2, '2025-07-22 21:00:00'),
(19, 2, 10, '2025-07-08 21:00:30'),
(20, 4, 10, '2025-07-09 08:00:00'),
(21, 14, 10, '2025-07-11 16:05:00'),
(22, 22, 10, '2025-07-12 09:00:00'),
(23, 23, 10, '2025-07-13 10:00:00'),
(24, 12, 10, '2025-07-14 11:00:00'),
(25, 8, 10, '2025-07-15 18:00:00'),
(26, 7, 13, '2025-07-10 21:35:00'),
(27, 9, 13, '2025-07-11 09:00:00'),
(28, 17, 13, '2025-07-14 12:05:00'),
(29, 21, 13, '2025-07-19 10:00:00'),
(30, 25, 13, '2025-07-25 03:00:00'),
(31, 3, 13, '2025-07-26 12:00:00'),
(32, 20, 5, '2025-07-20 17:30:00'),
(33, 14, 5, '2025-07-21 08:00:00'),
(34, 10, 5, '2025-07-22 19:00:00'),
(35, 7, 3, '2025-07-24 20:01:00'),
(36, 17, 3, '2025-07-24 20:16:00'),
(37, 9, 3, '2025-07-24 21:01:00'),
(38, 9, 7, '2025-07-07 09:45:00'),
(39, 17, 7, '2025-07-07 10:05:00'),
(40, 13, 7, '2025-07-10 22:00:00'),
(41, 7, 21, '2025-07-19 10:00:00'),
(42, 17, 21, '2025-07-19 11:00:00'),
(43, 24, 16, '2025-07-12 20:45:00'),
(44, 11, 16, '2025-07-12 21:05:00'),
(45, 16, 24, '2025-07-23 09:00:00'),
(46, 11, 15, '2025-07-12 09:00:00'),
(47, 26, 15, '2025-07-26 16:05:00'),
(48, 15, 11, '2025-07-09 12:35:00'),
(49, 15, 26, '2025-07-26 16:10:00'),
(50, 12, 22, '2025-07-21 13:05:00'),
(51, 18, 22, '2025-07-21 15:00:00'),
(52, 22, 12, '2025-07-09 18:05:00'),
(53, 4, 12, '2025-07-09 18:25:00'),
(54, 12, 4, '2025-07-10 09:00:00'),
(55, 22, 4, '2025-07-27 18:00:00');

-- 12. notification
INSERT INTO notification (id, member_id, sender_id, notification_type, content, is_checked, created_at) VALUES
(8, 2, 7, 'FOLLOW', '두부가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-07 12:00:01'),
(9, 10, 2, 'FOLLOW', '멍치가 회원님을 팔로우하기 시작했습니다.', 0, '2025-07-08 21:00:31'),
(10, 7, 9, 'COMMENT', '하루가 회원님의 게시글에 댓글을 남겼습니다.', 1, '2025-07-07 09:40:01'),
(11, 2, 7, 'DONATION', '두부가 간식 3,000원을 쏘셨습니다!', 0, '2025-07-16 12:00:26'),
(12, 13, 9, 'DONATION', '하루가 간식 5,000원을 쏘셨습니다!', 0, '2025-07-17 13:00:26'),
(13, 10, 14, 'SUBSCRIPTION', '누리가 탄이 프리미엄 플랜을 구독했습니다.', 1, '2025-07-18 10:00:21'),
(14, 10, 22, 'SUBSCRIPTION', '딸기가 탄이 베이직 플랜을 구독했습니다.', 0, '2025-07-18 15:00:21'),
(15, 2, 17, 'DONATION', '망고가 간식 10,000원을 쏘셨습니다!', 0, '2025-07-19 20:00:26'),
(16, 5, 20, 'SUBSCRIPTION', '봉식이 초코 팬클럽 플랜을 구독했습니다.', 0, '2025-07-20 18:00:21'),
(17, 2, 3, 'COMMENT', '나비가 회원님의 게시글에 댓글을 남겼습니다.', 1, '2025-07-17 14:20:01'),
(18, 3, 2, 'COMMENT', '멍치가 회원님의 게시글에 댓글을 남겼습니다.', 0, '2025-07-24 19:45:01'),
(19, 2, 10, 'DONATION', '탄이가 간식 20,000원을 쏘셨습니다!', 0, '2025-07-24 21:00:31'),
(20, 12, 1, 'PET BIRTHDAY', '내일은 콩이의 생일이에요! 축하 메시지를 남겨보세요.', 0, '2025-12-24 09:00:00'),
(21, 24, 16, 'DONATION', '소리가 간식 2,000원을 쏘셨습니다!', 0, '2025-07-27 09:00:21'),
(22, 13, 7, 'FOLLOW', '두부가 회원님을 팔로우하기 시작했습니다.', 1, '2025-07-10 21:35:01'),
(23, 10, 4, 'SUBSCRIPTION', '코코가 탄이 프리미엄 플랜을 구독했습니다.', 0, '2025-07-18 09:30:21');

-- 13. chat_room
INSERT INTO chat_room (id, member1_id, member2_id, member1_exited, member2_exited, created_at) VALUES
(4, 7, 9, 0, 0, '2025-07-07 22:00:00'),
(5, 2, 10, 0, 0, '2025-07-08 21:00:00'),
(6, 13, 17, 0, 0, '2025-07-14 12:00:00'),
(7, 12, 22, 0, 0, '2025-07-21 13:00:00'),
(8, 11, 15, 1, 0, '2025-07-16 10:00:00'),
(9, 16, 24, 0, 0, '2025-07-23 09:00:00');

-- 14. chat_message
INSERT INTO chat_message (id, room_id, sender_id, message, created_at) VALUES
(8, 4, 7, '하루야 오늘도 자고 있어?', '2025-07-07 22:01:00'),
(9, 4, 9, '응... zzz', '2025-07-07 22:30:00'),
(10, 4, 7, 'ㅋㅋㅋ 내일 놀자!', '2025-07-07 22:31:00'),
(11, 5, 2, '탄이 계단 챌린지 영상 잘 봤어요!', '2025-07-08 21:01:00'),
(12, 5, 10, '감사해요 멍치님! 수영 영상도 기대할게요.', '2025-07-08 21:10:00'),
(13, 5, 2, '다음 주에 올릴게요 ㅎㅎ', '2025-07-08 21:12:00'),
(14, 6, 13, '망고야 사냥놀이 같이 할래?', '2025-07-14 12:01:00'),
(15, 6, 17, '좋아! 낚싯대 하나 남았어?', '2025-07-14 12:05:00'),
(16, 6, 13, '하나 남았어 ㅋㅋ 빨리 와', '2025-07-14 12:06:00'),
(17, 7, 12, '딸기야 반려견 카페 같이 가자!', '2025-07-21 13:01:00'),
(18, 7, 22, '좋아요! 토요일 어때요?', '2025-07-21 13:20:00'),
(19, 8, 15, '미미야 바닥재 뭐 써?', '2025-07-16 10:01:00'),
(20, 8, 11, '종이 베딩 써! 추천해', '2025-07-16 10:30:00'),
(21, 9, 16, '키위야 안녕~ 나 말할 수 있어!', '2025-07-23 09:01:00'),
(22, 9, 24, '우와 부럽다! 나도 연습할게', '2025-07-23 09:15:00');

-- 15. missing_pet_post
INSERT INTO missing_pet_post (id, member_id, missing_date, missing_address, detail, image_url, status, latitude, longitude, created_at, updated_at) VALUES
(4, 8, '2025-07-15', '서울시 은평구 불광천 산책로', '갈색 시바견, 파란 하네스 착용. 겁이 많아 이름 부르면 도망갈 수 있어요.', 'https://s3.example.com/missing/4.jpg', 'MISSING', 37.5980000, 126.9150000, '2025-07-15 20:00:00', '2025-07-15 20:00:00'),
(5, 17, '2025-07-16', '경기도 고양시 일산동구 호수공원', '치즈색 코숏, 꼬리 끝이 꺾여 있어요.', 'https://s3.example.com/missing/5.jpg', 'MISSING', 37.6560000, 126.7660000, '2025-07-16 21:00:00', '2025-07-16 21:00:00'),
(6, 20, '2025-07-18', '울산시 남구 태화강 국가정원', '비글, 귀에 검은 반점. 간식 소리에 반응합니다.', NULL, 'FOUND', 35.5480000, 129.2970000, '2025-07-18 09:00:00', '2025-07-19 18:00:00'),
(7, 11, '2025-07-19', '서울시 관악구 낙성대공원', '흰색 토끼, 오른쪽 귀가 살짝 처져 있어요.', 'https://s3.example.com/missing/7.jpg', 'MISSING', 37.4770000, 126.9580000, '2025-07-19 18:00:00', '2025-07-19 18:00:00'),
(8, 22, '2025-07-20', '서울시 노원구 중계동 은행사거리', '주황색 포메라니안, 분홍 리본 착용.', 'https://s3.example.com/missing/8.jpg', 'MISSING', 37.6450000, 127.0640000, '2025-07-20 20:30:00', '2025-07-20 20:30:00'),
(9, 16, '2025-07-21', '대구시 수성구 수성못', '초록색 코뉴어 앵무새, "안녕"이라고 말해요.', NULL, 'MISSING', 35.8280000, 128.6170000, '2025-07-21 17:00:00', '2025-07-21 17:00:00'),
(10, 25, '2025-07-22', '서울시 서대문구 연희동 주택가', '턱시도 고양이, 목에 방울이 달려 있어요.', 'https://s3.example.com/missing/10.jpg', 'FOUND', 37.5690000, 126.9310000, '2025-07-22 23:00:00', '2025-07-24 10:00:00'),
(11, 14, '2025-07-23', '광주시 북구 무등산 입구', '백구 진돗개, 덩치가 큽니다. 사람을 좋아해요.', 'https://s3.example.com/missing/11.jpg', 'MISSING', 35.1340000, 126.9880000, '2025-07-23 19:00:00', '2025-07-23 19:00:00'),
(12, 23, '2025-07-25', '경기도 파주시 헤이리 예술마을', '흰색 사모예드, 털갈이 중이라 털이 많이 빠진 상태예요.', NULL, 'CANCELLED', 37.7890000, 126.6970000, '2025-07-25 15:00:00', '2025-07-25 21:00:00'),
(13, 26, '2025-07-27', '세종시 호수공원', '케이지에서 탈출한 고슴도치. 몸집이 작아요.', 'https://s3.example.com/missing/13.jpg', 'MISSING', 36.4980000, 127.2710000, '2025-07-27 12:00:00', '2025-07-27 12:00:00');

-- 16. missing_pet_report
INSERT INTO missing_pet_report (id, missing_pet_post_id, member_id, address, detail, image_url, sight_at, latitude, longitude, created_at, updated_at) VALUES
(4, 4, 14, '서울시 은평구 응암역 근처', '파란 하네스 시바견을 봤어요. 불광천 쪽으로 갔습니다.', 'https://s3.example.com/report/4.jpg', '2025-07-16 07:30:00', 37.5985000, 126.9155000, '2025-07-16 07:45:00', '2025-07-16 07:45:00'),
(5, 4, 9, '서울시 은평구 증산역 2번 출구', '비슷한 강아지가 편의점 앞에 있었어요.', NULL, '2025-07-16 19:00:00', 37.5838000, 126.9095000, '2025-07-16 19:10:00', '2025-07-16 19:10:00'),
(6, 5, 21, '경기도 고양시 일산동구 정발산역', '꼬리 꺾인 치즈냥 목격했어요.', 'https://s3.example.com/report/6.jpg', '2025-07-17 22:00:00', 37.6600000, 126.7730000, '2025-07-17 22:15:00', '2025-07-17 22:15:00'),
(7, 6, 12, '울산시 남구 삼산동', '비글을 보호하고 있어요. 연락 주세요!', 'https://s3.example.com/report/7.jpg', '2025-07-19 15:00:00', 35.5390000, 129.3380000, '2025-07-19 15:20:00', '2025-07-19 15:20:00'),
(8, 7, 15, '서울시 관악구 서울대입구역', '흰 토끼가 화단에 숨어 있었어요.', NULL, '2025-07-20 08:00:00', 37.4812000, 126.9527000, '2025-07-20 08:10:00', '2025-07-20 08:10:00'),
(9, 8, 18, '서울시 노원구 하계역', '분홍 리본 단 포메를 봤어요. 주인을 찾는 것 같았어요.', 'https://s3.example.com/report/9.jpg', '2025-07-21 18:30:00', 37.6363000, 127.0680000, '2025-07-21 18:40:00', '2025-07-21 18:40:00'),
(10, 10, 7, '서울시 마포구 연남동 경의선숲길', '방울 소리 나는 턱시도 고양이를 봤어요.', NULL, '2025-07-23 21:00:00', 37.5620000, 126.9250000, '2025-07-23 21:10:00', '2025-07-23 21:10:00'),
(11, 11, 8, '광주시 북구 전남대 후문', '큰 백구 한 마리가 돌아다니고 있었어요.', 'https://s3.example.com/report/11.jpg', '2025-07-24 12:00:00', 35.1780000, 126.9100000, '2025-07-24 12:20:00', '2025-07-24 12:20:00'),
(12, 13, 11, '세종시 정부청사 근처 공원', '풀숲에서 작은 고슴도치를 봤어요.', NULL, '2025-07-28 06:30:00', 36.5040000, 127.2650000, '2025-07-28 06:45:00', '2025-07-28 06:45:00');