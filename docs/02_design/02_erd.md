# 1. 데이터베이스 모델링 및 ERD 명세서 (사자그램 SNS)

## 목차
- [1. 데이터베이스 모델링 및 ERD 명세서 (사자그램 SNS)](#1-데이터베이스-모델링-및-erd-명세서-사자그램-sns)
- [1.1 엔티티 관계 다이어그램 (ERD)](#11-엔티티-관계-다이어그램-erd)
- [1.2 테이블별 상세 컬럼 명세](#12-테이블별-상세-컬럼-명세)

---

## 1.1 엔티티 관계 다이어그램 (ERD)

사자그램 서비스의 7개 핵심 도메인 테이블과 결제 및 정기 구독을 위한 2개 테이블의 전체 구조도.

```mermaid
erDiagram
    MEMBER ||--o{ PET : "owns"
    MEMBER ||--o{ POST_MAIN : "writes"
    MEMBER ||--o{ COMMENT : "writes"
    MEMBER ||--o{ POST_LIKE : "likes"
    MEMBER ||--o{ BOOKMARK : "bookmarks"
    MEMBER ||--o{ PAYMENT : "pays"
    MEMBER ||--o{ PAYMENT : "receives_payout"
    MEMBER ||--o{ SUBSCRIPTION : "subscribes"
    MEMBER ||--o{ SUBSCRIPTION : "receives_subscription"
    MEMBER ||--o{ FOLLOW : "follower"
    MEMBER ||--o{ FOLLOW : "following"
    MEMBER ||--o{ NOTIFICATIONS : "receives"
    MEMBER ||--o{ ATTENDANCES : "checks-in"
    MEMBER ||--o{ CHAT_ROOM : "participates(member1)"
    MEMBER ||--o{ CHAT_ROOM : "participates(member2)"
    MEMBER ||--o{ CHAT_MESSAGE : "sends"
    MEMBER ||--o{ MISSING_PET_POST : "writes"
    MEMBER ||--o{ MISSING_PET_REPORT : "reports"

    POST_MAIN ||--o{ POST_IMAGE : "contains"
    POST_MAIN ||--o{ COMMENT : "has"
    POST_MAIN ||--o{ POST_LIKE : "liked_by"
    POST_MAIN ||--o{ BOOKMARK : "bookmarked_by"
    POST_MAIN ||--o{ POST_HASHTAG : "tagged_with"

    CHAT_ROOM ||--o{ CHAT_MESSAGE : "contains"

    MISSING_PET_POST ||--o{ MISSING_PET_REPORT : "receives_reports"

    MEMBER {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR email "로그인 이메일 (UNIQUE)"
        VARCHAR password "BCrypt 암호화 비밀번호"
        VARCHAR nickname "화면 표시용 닉네임"
        VARCHAR intro "한줄 자기소개"
        VARCHAR profile_image "프로필 사진 S3 URL"
        VARCHAR address "회원 주소"
        VARCHAR status "계정 상태 (ACTIVE, BLOCKED)"
        VARCHAR role "권한 (ROLE_USER, ROLE_ADMIN)"
        DATETIME created_at "계정 생성 일시"
        DATETIME info_provide_agreement "개인정보 제공 동의 일시"
    }

    PET {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "소유 회원 ID (member.id 참조)"
        VARCHAR species "반려동물 종"
        VARCHAR name "반려동물 이름"
        VARCHAR sex "성별"
        DATE birth_date "생년월일"
        INT age "나이"
        DATETIME created_at "등록 일시"
    }

    POST_MAIN {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "작성자 회원 ID (member.id 참조)"
        TEXT content "피드 본문 내용"
        VARCHAR bgm_url "배경음악 S3 URL"
        TINYINT is_subscriber_only "구독자 전용 여부 (0:전체, 1:구독자)"
        DATETIME created_at "최초 작성 일시"
        DATETIME updated_at "최종 수정 일시"
    }

    POST_IMAGE {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT post_id FK "피드 게시글 ID (post_main.id 참조)"
        VARCHAR image_url "첨부 이미지 S3 URL"
        INT sort_order "슬라이드 순서 (DEFAULT 0)"
    }

    COMMENT {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT post_id FK "피드 게시글 ID (post_main.id 참조)"
        BIGINT member_id FK "댓글 작성자 ID (member.id 참조)"
        TEXT content "댓글 내용"
        DATETIME created_at "댓글 등록 일시"
    }

    POST_LIKE {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "좋아요 회원 ID (member.id 참조)"
        BIGINT post_id FK "피드 게시글 ID (post_main.id 참조)"
        DATETIME created_at "등록 일시"
    }

    BOOKMARK {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "북마크 회원 ID (member.id 참조)"
        BIGINT post_id FK "피드 게시글 ID (post_main.id 참조)"
        DATETIME created_at "등록 일시"
    }

    PAYMENT {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "결제 회원 ID (member.id 참조)"
        BIGINT target_member_id FK "대상 회원 ID (member.id 참조)"
        VARCHAR imp_uid "결제 승인 고유 번호"
        VARCHAR merchant_uid "주문 식별자 (UNIQUE)"
        INT amount "결제 금액"
        VARCHAR pay_type "결제 유형"
        VARCHAR status "결제 상태 (READY, PAID, FAILED, CANCELLED)"
        VARCHAR pay_method "결제 수단"
        DATETIME created_at "결제 요청 시각"
    }

    SUBSCRIPTION {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "구독 신청 회원 ID (member.id 참조)"
        BIGINT target_member_id FK "구독 대상 회원 ID (member.id 참조)"
        VARCHAR customer_uid "정기 결제 빌링키"
        VARCHAR plan_name "플랜 이름"
        INT price "매월 결제 금액"
        INT total_months "누적 구독 개월 수 (DEFAULT 1)"
        VARCHAR status "구독 상태 (ACTIVE, PAUSED, CANCELLED)"
        DATETIME next_billing_at "다음 결제 예정일"
        DATETIME started_at "최초 구독 시작일"
        DATETIME ended_at "구독 해지 완료일"
    }

    FOLLOW {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT follower_id FK "팔로우 신청 회원 ID (member.id 참조)"
        BIGINT following_id FK "팔로우 대상 회원 ID (member.id 참조)"
        DATETIME created_at "팔로우 일시"
    }

    NOTIFICATIONS {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "수신 회원 ID (member.id 참조)"
        VARCHAR notification_type "알림 유형"
        VARCHAR al_content "알림 메시지 내용"
        TINYINT is_checked "확인 여부 (0:안읽음, 1:읽음)"
        DATETIME created_at "알림 발생 시각"
    }

    ATTENDANCES {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "출석 회원 ID (member.id 참조)"
        DATETIME created_at "출석 버튼 클릭 일시"
    }

    CHAT_ROOM {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member1_id FK "참여자 1 ID (member.id 참조)"
        BIGINT member2_id FK "참여자 2 ID (member.id 참조)"
        DATETIME created_at "채팅방 생성 일시"
    }

    CHAT_MESSAGE {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT room_id FK "채팅방 ID (chat_room.id 참조)"
        BIGINT sender_id FK "발신자 회원 ID (member.id 참조)"
        TEXT message "메시지 본문"
        DATETIME created_at "메시지 전송 시각"
    }

    MISSING_PET_POST {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT member_id FK "작성자 회원 ID (member.id 참조)"
        VARCHAR pet_name "실종 반려동물 이름"
        VARCHAR species "종"
        VARCHAR sex "성별"
        INT age "나이"
        DATE missing_date "실종일자"
        VARCHAR missing_address "실종장소"
        TEXT detail "특이사항"
        VARCHAR image_url "대표 사진 S3 URL"
        DATETIME created_at "작성 시각"
        DATETIME updated_at "수정 일시"
    }

    MISSING_PET_REPORT {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT missing_pet_post_id FK "실종 게시글 ID (missing_pet_post.id 참조)"
        BIGINT member_id FK "제보자 회원 ID (member.id 참조)"
        VARCHAR address "목격 장소"
        TEXT detail "목격 상황 설명"
        VARCHAR image_url "제보 사진 S3 URL"
        DATETIME created_at "제보 등록 일시"
        DATETIME updated_at "제보 수정 일시"
    }

    POST_HASHTAG {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT post_id FK "사용된 피드 ID (post_main.id 참조)"
        VARCHAR name "해시태그 키워드"
    }
```

---

## 1.2 테이블별 상세 컬럼 명세

### 1.2.1 member (회원 기본)
| 컬럼명        | 데이터 타입       | 제약 조건                     | 설명                   |
|:-----------|:-------------|:--------------------------|:---------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT        | 회원 고유 식별자            |
| email      | VARCHAR(100) | NOT NULL, UNIQUE          | 로그인 아이디 (이메일)        |
| password   | VARCHAR(255) | NOT NULL                  | BCrypt 암호화된 비밀번호     |
| nickname   | VARCHAR(50)  | NOT NULL                  | 화면 표시용 닉네임           |
| species    | VARCHAR(50)  | NOT NULL                  | 동물 종 카테고리 (개, 고양이 등) |
| sex        | VARCHAR(10)  | NULL                      | 성별                   |
| birth_date | DATE         | NULL                      | 생년월일                 |
| intro                  | VARCHAR(255) | NULL                          | 한줄 자기소개                              |
| profile_image          | VARCHAR(255) | NULL                          | 회원 프로필 사진 S3 URL                     |
| address                | VARCHAR(255) | NULL                          | 회원주소                                 |
| status                 | VARCHAR(50)  | NOT NULL , DEFAULT 'ACTIVE'   | 계정 상태 (ACTIVE, BLOCKED)              |
| role                   | VARCHAR(20)  | NOT NULL, DEFAULT 'ROLE_USER' | 권한 (ROLE_USER, ROLE_VIP, ROLE_ADMIN) |
| created_at             | DATETIME     | DEFAULT CURRENT_TIMESTAMP     | 계정 생성 일시                             |
| info_provide_agreement | DATETIME     | Null                          | 개인정보 제3자 제공 동의 일시|




### 1.2.2 post_main (피드 게시글)
| 컬럼명                | 데이터 타입       | 제약 조건                                                 | 설명                               |
|:-------------------|:-------------|:------------------------------------------------------|:---------------------------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT                                    | 피드 게시글 고유 식별자                    |
| member_id          | BIGINT       | NOT NULL, FK(member.id ON DELETE CASCADE)             | 작성자 회원 ID                        |
| content            | TEXT         | NOT NULL                                              | 피드 본문 내용                         |
| bgm_url            | VARCHAR(255) | NULL                                                  | 배경음악 S3 URL                      |
| is_subscriber_only | TINYINT(1)   | NOT NULL, DEFAULT 0                                   | 유료 구독자 전용 여부 (0: 전체공개, 1: 구독자전용) |
| hashtags           | TEXT            | NULL                                                  | 해시태그 문자열 (예: "#강아지 #산책 #일상")                    |
| created_at         | DATETIME     | DEFAULT CURRENT_TIMESTAMP                             | 피드 최초 작성 일시                      |
| updated_at         | DATETIME     | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 피드 최종 수정 일시                      |


### 1.2.3 post_image
| 컬럼명        | 데이터 타입       | 제약 조건                                         | 설명               |
|:-----------|:-------------|:----------------------------------------------|:-----------------|
| id         | BIGINT       | PK, AUTO_INCREMENT                            | 이미지 식별자          |
| post_id    | BIGINT       | NOT NULL, FK (post_main.id ON DELETE CASCADE) | 피드 게시글 식별자       |
| image_url  | VARCHAR(255) | NOT NULL                                      | 피드 첨부 이미지 s3 URL |
| sort_order | INT          | NOT NULL, DEFAULT 0                           | 이미지 슬라이드 순서      |

1:n구조 게시글하나에 여러 장의 사진이 올라가므로 single 테이블 방식으로는 깔금하게 저장 관리하기 어렵다.
db저장 특성상 자동정렬이 되지 않아서 유저가 올린 사진 순서와 펫 클럽 슬라이스 ui를 올바르게 렌더링하기 위해 sort_order가 필요

### 1.2.4 comment (게시글댓글)
| 컬럼명        | 데이터 타입 | 제약 조건                                         | 설명            |
|:-----------| :--- |:----------------------------------------------|:--------------|
| id         | BIGINT | PK, AUTO_INCREMENT                            | 댓글 고유 식별자     |
| post_id    | BIGINT | NOT NULL, FK (post_main.id ON DELETE CASCADE) | 댓글이 달린 피드 식별자 |
| member_id  | BIGINT | NOT NULL, FK (member.id ON DELETE CASCADE)    | 댓글 작성자 식별자    |
| content    | TEXT | NOT NULL                                      | 댓글 텍스트 내용     |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP                     | 댓글 등록 일시      |


### 1.2.5 post_interaction (피드 인터랙션-좋아요 & 북마크 통합)
| 컬럼명       | 데이터 타입      | 제약 조건                                         | 설명                      |
|:----------|:------------|:----------------------------------------------|:------------------------|
| id        | BIGINT      | PK, AUTO_INCREMENT                            | 좋아요 식별자                 |
| member_id | BIGINT      | NOT NULL, FK (member.id ON DELETE CASCADE)    | 좋아요를 누른 회원 ID           |
| post_id   | BIGINT      | NOT NULL, FK (post_main.id ON DELETE CASCADE) | 대상 피드 게시글 ID            |
| type      | VARCHAR(20) | NOT NULL | 인터랙션 유형(LIKE, BOOKMARK) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP                     |  등록 일시                  |
- 고유 제약조건: UNIQUE KEY `uk_member_post_like` (`member_id`, `post_id`, `interaction_type`)
- 데이터의 중복을 차단하기 위해 unique key설정

### 1.2.6 payment (결제 이력)
| 컬럼명              | 데이터 타입       | 제약 조건                                      | 설명                                     |
|:-----------------|:-------------|:-------------------------------------------|:---------------------------------------|
| id               | BIGINT       | PK, AUTO_INCREMENT                         | 결제 내역 식별자                              |
| member_id        | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 결제 회원(동물 유저) ID                        |
| target_member_id | BIGINT       | NOT NULL, FK                               | 후원 대상 동물 유저                            |
| imp_uid          | VARCHAR(100) | NULL                                       | 결제 승인 고유 번호                            |
| merchant_uid     | VARCHAR(100) | NOT NULL, UNIQUE                           | 자체 생성 주문 식별자 (예: ORD_20260917_001)     |
| amount           | INT          | NOT NULL                                   | 결제 금액                                  |
| category         | VARCHAR(30)  | NOT NULL                                   | 결제 상품 (간식 쏘기, 펫 클럽 구독)                 | 
| status           | VARCHAR(20)  | NOT NULL                                   | 결제 상태 (READY, PAID, FAILED, CANCELLED) |
| pay_type         | VARCHAR(30)  | NOT NULL                                   | 결제 수단 (card, point 등)                  |
| created_at       | DATETIME     | DEFAULT CURRENT_TIMESTAMP                  | 결제 요청 시각                               |
| completed_at     | DATETIME     | DEFAULT CURRENT_TIMESTAMP                  | 결제 완료 시각                               |

### 1.2.7 subscription_plan (정기 후원 플랜)
| 컬럼명         | 데이터 타입      | 제약 조건                                      | 설명               |
|:------------|:------------|:-------------------------------------------|:-----------------|
| id          | BIGINT      | PK, AUTO_INCREMENT                         | 구독 플랜 식별자        |
| member_id   | BIGINT      | NOT NULL, FK (member.id ON DELETE CASCADE) | 해당 플랜을 만든 회원 식별자 |
| plan_name   | VARCHAR(50) | NOT NULL                                   | 플랜 이름            |
| price       | INT         | NOT NULL                                   | 플랜 가격            |
| description | TEXT        | NOT NULL                                   | 플랜 설명            |
- 고유 제약조건: UNIQUE KEY `uk_member_post_like` (`member_id`, `plan_name`)

### 1.2.8 subscription (펫클럽 정기 후원)
| 컬럼명              | 데이터 타입       | 제약 조건                                      | 설명                                |
|:-----------------|:-------------|:-------------------------------------------|:----------------------------------|
| id               | BIGINT       | PK, AUTO_INCREMENT                         | 구독 식별자                            |
| member_id        | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자                         |
| target_member_id | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 대상 회원 식별자                      |
| plan_id          | VARCHAR(50)  | NOT NULL, DEFAULT 'VIP_MONTHLY'            | 구독 플랜 식별자                         |
| customer_uid     | VARCHAR(100) | NOT NULL                                   | 정기 결제 카드 빌링키                      |
| status           | VARCHAR(20)  | NOT NULL, DEFAULT 'ACTIVE'                 | 구독 상태 (ACTIVE, PAUSED, CANCELLED) |
| started_at       | DATETIME     | DEFAULT CURRENT_TIMESTAMP                  | 시작일                               |
| ended_at         | DATETIME     | NULL                                       | 만료일                               |
| next_billing_at  | DATETIME     | NULL                                       | 다음 자동 결제 예정일                      |
| agreement        | TINYINT      | NOT NULL, DEFAULT 1                        | 자동결제 동의여부 (0: 비동의, 1: 동의)         |

### 1.2.9subscription_history (팔로우)
| 컬럼명             | 데이터 타입   | 제약 조건                                            | 설명           |
|:----------------|:---------|:-------------------------------------------------|:-------------|
| id              | BIGINT   | PK, AUTO_INCREMENT                               | 이력 식별자       |
| subscription_id | BIGINT   | NOT NULL, FK (subscription.id ON DELETE CASCADE) | 상위 구독 ID    |
| status          | VARCHAR(20)   | NOT NULL,                                        | 구독 상태 (ACTIVE, PAUSED, CANCELLED)|
| started_at      | DATETIME | DEFAULT CURRENT_TIMESTAMP                        | 해당 상태 시작/재개 일시      |
| ended_at        | DATETIME | NULL                                             | 해당 상태 종료/변경 일시 (진행 중일 경우 NULL)       |


### 1.2.10 follow (팔로우)
| 컬럼명          | 데이터 타입   | 제약 조건                                      | 설명           |
|:-------------|:---------|:-------------------------------------------|:-------------|
| id           | BIGINT   | PK, AUTO_INCREMENT                         | 팔로우 식별자      |
| follower_id  | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE) | 팔로우 하는 회원    |
| following_id | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE) | 팔로우 대상 회원 ID |
| created_at   | DATETIME | DEFAULT CURRENT_TIMESTAMP                  | 팔로우 일시       |
UNIQUE KEY uk_follower_following (follower_id, following_id)

### 1.2.11 notifications(알림)
| 컬럼명       | 데이터 타입       | 제약 조건                                      | 설명           |
|:----------|:-------------|:-------------------------------------------|:-------------|
| id        | BIGINT       | PK, AUTO_INCREMENT                         | 알림 식별자       |
| member_id | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 회원 식별자       |
| sender_id | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 알림을 유발한 회원 ID |
| notification_type | VARCHAR(30)  | NOT NULL                                   | 알림 유형(FOLLOW, COMMENT, DONATION, SUBSCRIPTION, pet birthday) |
| al_content        | VARCHAR(255) | NOT NULL                                   | 알림 메시지 내용                                                    |
| is_checked        | TINYINT(1) | NOT NULL, DEFAULT 0                                  | 알림 확인 여부 (0: 안읽음, 1: 읽음)                                     |
| created_at        | DATETIME     | DEFAULT CURRENT_TIMESTAMP                  | 알림 발생 시각                                                     |

### 1.2.12 chat_room(1:1 채팅방)
| 컬럼명            | 데이터 타입   | 제약 조건                                      | 설명        |
|:---------------|:---------|:-------------------------------------------|:----------|
| id             | BIGINT   | PK, AUTO_INCREMENT                         | 채팅방 식별자   |
| member1_id     | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE) | 채팅 참여자 1  |
| member2_id     | BIGINT     | NOT NULL, FK(member.id ON DELETE CASCADE)                               | 채팅 참여자 2  |
| member1_exited | TINYINT(1)     | NOT NULL, DEFAULT 0                             | 참여자 1 나가기 여부 (0: 참여중, 1: 나감)  |
| member2_exited | TINYINT(1)     | NOT NULL, DEFAULT 0                              | 참여자 2 나가기 여부 (0: 참여중, 1: 나감) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP                  | 채팅방 생성 일시 |

### 1.2.13 chat_message(채팅 메시지 이력)
| 컬럼명        | 데이터 타입   | 제약 조건                                         | 설명        |
|:-----------|:---------|:----------------------------------------------|:----------|
| id         | BIGINT   | PK, AUTO_INCREMENT                            | 메시지 식별자   |
| room_id    | BIGINT   | NOT NULL, FK (chat_room.id ON DELETE CASCADE) | 속한 채팅방 ID |
| sender_id  | BIGINT   | NOT NULL, FK  (member.id 참조)                  | 메시지 보낸 사람 |
| message    | TEXT     | NOT NULL                                      | 메시지 본문    |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP                     | 매시지 전송 시각 |

### 1.2.14 missing_pet_post(실종 신고 게시글)
| 컬럼명             | 데이터 타입        | 제약 조건                                      | 설명                   |
|:----------------|:--------------|:-------------------------------------------|:---------------------|
| id              | BIGINT        | PK, AUTO_INCREMENT                         | 실종 신고 게시글 고유 ID      |
| member_id       | BIGINT        | NOT NULL, FK (member.id ON DELETE CASCADE) | 작성자 회원 ID            |
| missing_date    | DATE          | NOT NULL                                   | 실종일자                 |
| missing_address | VARCHAR(255)  | NOT NULL                                   | 실종장소                 |
| detail          | TEXT          |  NULL                                      | 특이사항                 |
| image_url       | VARCHAR(255)  | NULL                                       | 실종 반려동물 대표사진  S3 URL |
| latitude        | DECIMAL(10,7) | NULL                                       | 위도                   |
| longitude       | DECIMAL(10,7) | NULL                                       | 경도                   |
| created_at      | DATETIME      | DEFAULT CURRENT_TIMESTAMP                  | 작성 시각                |
| updated_at      | DATETIME      | DEFAULT CURRENT_TIMESTAMP                                       | 게시글 수정 일시            |

### 1.2.15 missing_pet_report(실종 동물 목격 제보)
| 컬럼명                 | 데이터 타입       | 제약 조건                                                | 설명                            |
|:--------------------|:-------------|:-----------------------------------------------------|:------------------------------|
| id                  | BIGINT       | PK, AUTO_INCREMENT                                   | 실종 신고 게시글 고유 ID               |
| missing_pet_post_id | BIGINT       | NOT NULL, FK (missing_pet_post.id ON DELETE CASCADE) | 대상 실종 신고 게시글 ID               |
| member_id           | BIGINT       | NOT NULL, FK(member.id ON DELETE CASCADE)            | 제보자 회원 ID                     |
| address             | VARCHAR(255) | NOT NULL                                             | 목격 장소                         |
| detail              | TEXT         | NULL                                                 | 목격 상황 및 상태 설명(추가 추천)          |
| image_url           | VARCHAR(255) | NULL                                                 | 제보자가 찰영한 이미지 S3 URL           |
| sight_at            | DATETIME     | NOT NULL   , DEFAULT 'MISSING'                        | 실제 동물을 목격한 일시 |
| status              | VARCHAR(20)  | NOT NULL                                             |   상태(MISSING, FOUND, CANCELLED)               |
| latitude        | DECIMAL(10,7) | NULL                                       | 위도                   |
| longitude       | DECIMAL(10,7) | NULL                                       | 경도                   |
| created_at          | DATETIME     | DEFAULT CURRENT_TIMESTAMP                            | 제보 등록 일시             |
| updated_at          | DATETIME     | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP                           | 제보 수정 일시             |

