-- 1. member
INSERT INTO member (id, email, password, nickname, species, sex, birth_date, intro, profile_image, address, status,
                    role, created_at, info_provide_agreement)
VALUES (100, 'admain@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '관리자냥', '고양이', '여',
        '2020-01-01', '사자그램 관리자입니다.', 'https://s3.example.com/profile/admin.jpg', '서울시 강남구', 'ACTIVE', 'ROLE_ADMIN',
        '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
       (101, 'mundgchi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '멍치', '개', '남',
        '2021-03-15', '산책 좋아하는 골든리트리버!', 'https://s3.example.com/profile/mungchi.jpg', '서울시 마포구', 'ACTIVE', 'ROLE_VIP',
        '2025-02-10 11:00:00', '2025-02-10 11:00:00'),
       (102, 'naabi@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '나비', '고양이', '여',
        '2022-07-20', '창가에서 낮잠 자는 게 취미예요.', 'https://s3.example.com/profile/nabi.jpg', '서울시 송파구', 'ACTIVE', 'ROLE_USER',
        '2025-03-05 09:30:00', NULL),
       (103, 'cofco@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '코코', '개', '여',
        '2020-11-08', '간식 없으면 시무룩해요.', 'https://s3.example.com/profile/coco.jpg', '경기도 성남시', 'ACTIVE', 'ROLE_USER',
        '2025-04-12 14:20:00', '2025-04-12 14:20:00'),
       (104, 'chdoco@petory.com', '$2a$10$MEkEZscMjV6GZ0BWMWrRx.b79OinhxXQ.3utuf/yvQBmXwY3ZHT.y', '초코', '개', '남',
        '2019-05-30', '공놀이 최강자 🎾', 'https://s3.example.com/profile/choco.jpg', '부산시 해운대구', 'ACTIVE', 'ROLE_USER',
        '2025-05-01 16:00:00', NULL),
       (105, NULL, NULL, '카카오토리', '고양이', '남', '2023-01-12', '카카오로 가입했어요.', 'https://s3.example.com/profile/kakao.jpg',
        '서울시 종로구', 'ACTIVE', 'ROLE_USER', '2025-06-15 10:00:00', '2025-06-15 10:00:00');

-- 15. missing_pet_post
INSERT INTO missing_pet_post (id, member_id, missing_date, missing_address, detail, image_url, status, latitude,
                              longitude, created_at, updated_at)
VALUES (100, 103, '2025-07-08', '경기도 성남시 분당구 정자동 공원 근처', '흰색 푸들, 빨간 목걸이 착용. 이름이 코코입니다.',
        'https://s3.example.com/missing/1.jpg', 'MISSING', 37.3595000, 127.1052000, '2025-07-08 20:00:00',
        '2025-07-08 20:00:00'),
       (101, 105, '2025-06-25', '서울시 종로구 청계천 일대', '회색 코숏, 왼쪽 귀에 작은 상처 있음.', 'https://s3.example.com/missing/2.jpg', 'FOUND',
        37.5700000, 126.9820000, '2025-06-25 18:30:00', '2025-06-28 12:00:00'),
       (102, 104, '2025-07-01', '부산시 해운대구 달맞이길', '갈색 믹스견. 찾아주셔서 감사합니다. 신고 취소합니다.', NULL, 'CANCELLED', 35.1587000,
        129.1604000, '2025-07-01 07:00:00', '2025-07-01 22:00:00');

-- 16. missing_pet_report
INSERT INTO missing_pet_report (id, missing_pet_post_id, member_id, address, detail, image_url, sight_at, latitude,
                                longitude, created_at, updated_at)
VALUES (100, 100, 101, '성남시 분당구 수내동 카페거리', '빨간 목걸이 흰 푸들 비슷한 아이를 봤어요. 사람 가까이 오진 않았습니다.', 'https://s3.example.com/report/1.jpg',
        '2025-07-09 08:30:00', 37.3781000, 127.1142000, '2025-07-09 09:00:00', '2025-07-09 09:00:00'),
       (101, 101, 102, '성남시 분당구 정자역 3번 출구', '목격 후 바로 사진 찍었어요. 아직 근처에 있을 수 있어요.', 'https://s3.example.com/report/2.jpg',
        '2025-07-09 17:00:00', 37.3660000, 127.1082000, '2025-07-09 17:20:00', '2025-07-09 17:20:00'),
       (102, 102, 102, '서울시 종로구 광장시장 근처', '회색 고양이 구조해서 보호소에 연락했습니다.', NULL, '2025-06-27 14:00:00', 37.5704000, 126.9996000,
        '2025-06-27 15:00:00', '2025-06-27 15:00:00');