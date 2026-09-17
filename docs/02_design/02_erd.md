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
    MEMBER ||--|| MEMBER_DETAIL : "1:1 수직 분할"
    MEMBER ||--o{ POST : "1:N 작성"
    POST ||--o{ POST_HASHTAG : "1:N 태그 분할"
    POST ||--o{ COMMENT : "1:N 댓글 작성"
    MEMBER ||--o{ COMMENT : "1:N 댓글 작성"
    MEMBER ||--o{ POST_LIKE : "1:N 좋아요"
    POST ||--o{ POST_LIKE : "1:N 피드 좋아요"
    MEMBER ||--o{ BOOKMARK : "1:N 보관"
    POST ||--o{ BOOKMARK : "1:N 피드 보관"
    MEMBER ||--o{ PAYMENT : "1:N 결제 이력"
    MEMBER ||--o| SUBSCRIPTION : "1:1 VIP 정기 구독"

    MEMBER {
        bigint id PK "회원 고유 식별자"
        varchar email "이메일 (UK)"
        varchar password "암호화된 비밀번호"
        varchar nickname "닉네임"
        varchar profile_image "프로필 이미지 S3 URL"
        varchar role "회원 권한 (ROLE_USER, ROLE_VIP, ROLE_ADMIN)"
        datetime created_at "가입 일시"
    }

    MEMBER_DETAIL {
        bigint member_id PK, FK "회원 기본 키"
        text introduction "자기소개 한 줄"
        varchar address "주소"
        varchar marketing_agreed "마케팅 수신 동의 (Y/N)"
    }

    POST {
        bigint id PK "피드 고유 식별자"
        bigint member_id FK "작성자 회원 ID"
        text content "피드 본문 텍스트"
        varchar image_url "피드 첨부 이미지 S3 URL"
        int like_count "좋아요 집계 수"
        datetime created_at "등록 일시"
        datetime updated_at "수정 일시"
    }

    POST_HASHTAG {
        bigint id PK "해시태그 매핑 식별자"
        bigint post_id FK "피드 게시글 ID"
        varchar tag_name "해시태그 단어"
    }

    COMMENT {
        bigint id PK "댓글 고유 식별자"
        bigint post_id FK "피드 게시글 ID"
        bigint member_id FK "작성자 회원 ID"
        text content "댓글 내용"
        datetime created_at "등록 일시"
    }

    POST_LIKE {
        bigint id PK "좋아요 고유 식별자"
        bigint member_id FK "좋아요 누른 회원 ID"
        bigint post_id FK "피드 게시글 ID"
        datetime created_at "등록 일시"
    }

    BOOKMARK {
        bigint id PK "북마크 고유 식별자"
        bigint member_id FK "보관한 회원 ID"
        bigint post_id FK "피드 게시글 ID"
        datetime created_at "등록 일시"
    }

    PAYMENT {
        bigint id PK "결제 고유 식별자"
        bigint member_id FK "결제 회원 ID"
        varchar imp_uid "결제 승인 고유번호"
        varchar merchant_uid "상점 고유 주문번호 (UK)"
        int amount "결제 금액"
        varchar status "결제 상태 (READY, PAID, FAILED, CANCELLED)"
        varchar pay_method "결제 수단 (card, trans 등)"
        datetime paid_at "결제 완료 일시"
        datetime created_at "등록 일시"
    }

    SUBSCRIPTION {
        bigint id PK "구독 고유 식별자"
        bigint member_id FK "구독 회원 ID (UK)"
        varchar customer_uid "정기결제 빌링키"
        varchar plan_name "구독 플랜명 (VIP_MONTHLY)"
        int price "월 결제 금액"
        varchar status "구독 상태 (ACTIVE, PAUSED, CANCELLED)"
        datetime next_billing_at "다음 자동 결제 예정일"
        datetime started_at "구독 시작일"
        datetime ended_at "구독 종료일"
    }
```

---

## 1.2 테이블별 상세 컬럼 명세

### 1.2.1 member (회원 기본)
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 회원 고유 식별자 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 로그인 아이디 (이메일) |
| password | VARCHAR(255) | NOT NULL | BCrypt 암호화된 비밀번호 |
| nickname | VARCHAR(50) | NOT NULL | 화면 표시용 닉네임 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'ROLE_USER' | 권한 (ROLE_USER, ROLE_VIP, ROLE_ADMIN) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 계정 생성 일시 |

### 1.2.2 pet (반려동물 프로필)
| 컬럼명           | 데이터 타입       | 제약 조건              | 설명                 |
|:--------------|:-------------|:-------------------|:-------------------|
| id            | BIGINT       | PK, AUTO_INCREMENT | 반려동물 고유 식별자        |
| member_id     | BIGINT       | FK, NOT NULL       | 회원 식별자             |
| name          | VARCHAR(50)  | NOT NULL           | 반려동물 이름            |
| birth_date    | DATE         | NULL               | 생년월일               |
| profile_image | VARCHAR(255) | NULL               | 펫 프로필 사진           |
| bio           | VARCHAR(255) | NULL               | 마이페이지 및 프로필용 한줄 소개 |
| created_at    | DATETIME     | DEFAULT_TIMESTAMP  | 펫 프로필 등록 일시        |


### 1.2.3 post_main (피드 게시글)
| 컬럼명                | 데이터 타입 | 제약 조건                               | 설명            |
|:-------------------| :--- |:------------------------------------|:--------------|
| id                 | BIGINT | PK, AUTO_INCREMENT                  | 피드 게시글 고유 식별자 |
| pet_id             | BIGINT | NOT NULL, FK                        | 작성 반려동물 식별자   |
| content            | TEXT | NOT NULL                            | 피드 본문 내용      |
| bgm_url            | VARCHAR(255) | NULL                                | 배경음악 S3 URL   |
| is_subscriber_only | TINYINT(1) | NOT NULL, DEFAULT 0| 유료 구독자 전용 여부 (0: 전체공개, 1: 구독자전용)   |
| created_at         | DATETIME | DEFAULT CURRENT_TIMESTAMP           | 피드 최초 작성 일시   |
| updated_at         | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 피드 최종 수정 일시   |



| 컬럼명        | 데이터 타입       | 제약 조건                                    | 설명               |
|:-----------|:-------------|:-----------------------------------------|:-----------------|
| id         | BIGINT       | PK, AUTO_INCREMENT                       | 해시태그 매핑 식별자      |
| post_id    | BIGINT       | NOT NULL, FK (post.id ON DELETE CASCADE) | 피드 게시글 식별자       |
| image_url  | VARCHAR(255) | NOT NULL                                 | 피드 첨부 이미지 s3 URL |
| sort_order | INT          | NOT NULL, DEFAULT)                       | 이미지 슬라이드 순서      |

1:n구조 게시글하나에 여러 장의 사진이 올라가므로 single 테이블 방식으로는 깔금하게 저장 관리하기 어렵다.
db저장 특성상 자동정렬이 되지 않아서 유저가 올린 사진 순서와 펫 클럽 슬라이스 ui를 올바르게 렌더링하기 위해 sort_order가 필요

### 1.2.5 comment (게시글댓글)
| 컬럼명        | 데이터 타입 | 제약 조건                                      | 설명            |
|:-----------| :--- |:-------------------------------------------|:--------------|
| id         | BIGINT | PK, AUTO_INCREMENT                         | 댓글 고유 식별자     |
| post_id    | BIGINT | NOT NULL, FK (post.id ON DELETE CASCADE)   | 댓글이 달린 피드 식별자 |
| member_id  | BIGINT | NOT NULL, FK (member.id ON DELETE CASCADE) | 댓글 작성자 식별자    |
| content    | TEXT | NOT NULL                                   | 댓글 텍스트 내용     |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP                  | 댓글 등록 일시      |


### 1.2.6 post_like (피드 좋아요 - N:M 매핑)
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 식별자 |
| member_id | BIGINT | NOT NULL, FK (member.id ON DELETE CASCADE) | 좋아요를 누른 회원 ID |
| post_id | BIGINT | NOT NULL, FK (post.id ON DELETE CASCADE) | 대상 피드 게시글 ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 좋아요 등록 일시 |
- 고유 제약조건: UNIQUE KEY `uk_member_post_like` (`member_id`, `post_id`)
- 데이터의 중복을 차단하기 위해 unique key설정

### 1.2.7 bookmark (피드 북마크 - N:M 매핑)
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 북마크 식별자 |
| member_id | BIGINT | NOT NULL, FK (member.id ON DELETE CASCADE) | 북마크한 회원 ID |
| post_id | BIGINT | NOT NULL, FK (post.id ON DELETE CASCADE) | 대상 피드 게시글 ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 북마크 등록 일시 |
- 고유 제약조건: UNIQUE KEY `uk_member_post_bookmark` (`member_id`, `post_id`)

### 1.2.8 payment (결제 이력)
| 컬럼명           | 데이터 타입       | 제약 조건                                      | 설명                                     |
|:--------------|:-------------|:-------------------------------------------|:---------------------------------------|
| id            | BIGINT       | PK, AUTO_INCREMENT                         | 결제 내역 식별자                              |
| member_id     | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 결제 회원 ID                               |
| target_pet_id | BIGINT       | NOT NULL, FK                               | 후원할 동물                                 |
| imp_uid       | VARCHAR(100) | NULL                                       | 결제 승인 고유 번호                            |
| merchant_uid  | VARCHAR(100) | NOT NULL, UNIQUE                           | 자체 생성 주문 식별자 (예: ORD_20260917_001)     |
| amount        | INT          | NOT NULL                                   | 결제 금액                                  |
| pay_type      | VARCHAR(30)  | NOT NULL                                   | 결제 유형(간식쏘기, 펫클럽 구독)                    |
| status        | VARCHAR(20)  | NOT NULL                                   | 결제 상태 (READY, PAID, FAILED, CANCELLED) |
| pay_method    | VARCHAR(30)  | NOT NULL                                   | 결제 수단 (card, point 등)                  |
| paid_at       | DATETIME     | NULL                                       | 실제 결제 완료 시각                            |
| created_at    | DATETIME     | DEFAULT CURRENT_TIMESTAMP                  | 결제 요청 시각                               |

### 1.2.9 subscription (펫클럽 정기 후원)
| 컬럼명             | 데이터 타입 | 제약 조건                                      | 설명 |
|:----------------| :--- |:-------------------------------------------| :--- |
| id              | BIGINT | PK, AUTO_INCREMENT                         | 구독 식별자 |
| member_id       | BIGINT | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자 |
| target_pet_id   | BIGINT | NOT NULL, FK (pet.id ON DELETE CASCADE)    | 구독 회원 식별자 |
| customer_uid    | VARCHAR(100) | NOT NULL                                   | 정기 결제 카드 빌링키 |
| plan_name       | VARCHAR(50) | NOT NULL, DEFAULT 'VIP_MONTHLY'            | 구독 플랜 이름 |
| price           | INT | NOT NULL                                   | 매월 정기 결제 금액 (예: 9,900원) |
| status          | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE'                 | 구독 상태 (ACTIVE, PAUSED, CANCELLED) |
| next_billing_at | DATETIME | NOT NULL                                   | 다음 자동 결제 예정일 |
| started_at      | DATETIME | DEFAULT CURRENT_TIMESTAMP                  | 최초 구독 시작일 |
| ended_at        | DATETIME | NULL                                       | 구독 해지 완료일 |

### 1.2.10 follow (팔로우)
| 컬럼명        | 데이터 타입   | 제약 조건                                              | 설명        |
|:-----------|:---------|:---------------------------------------------------|:----------|
| member_id  | BIGINT   | NOT NULL, UNIQUE, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자 |
| pet_id     | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE)         | 구독 회원 식별자 |
| created_at | DATETIME | DEFAUL CURRENT_TIMESTAMP                           | 팔로우 일시    |

### 1.2.11 notifications(알림)
| 컬럼명               | 데이터 타입       | 제약 조건                                      | 설명                                            |
|:------------------|:-------------|:-------------------------------------------|:----------------------------------------------|
| id                | BIGINT       | PK, AUTO_INCREMENT                         | 알림 식별자                                        |
| member_id         | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자                                     |
| notification_type | VARCHAR(30)  | NOT NULL                                   | 알림 유형(FOLLOW, COMMENT, DONATION, SUBSCRIPTION |
| member_id         | BIGINT       | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자                                     |
| al_content        | VARCHAR(255) | NOT NULL                                   | 알림 메시지 내용                                     |
| created_at        | DATETIME     | DEFAUL CURRENT_TIMESTAMP                   | 알림 발생 시각                                              |

### 1.2.12 attendances(출석체크)
| 컬럼명        | 데이터 타입   | 제약 조건                                      | 설명        |
|:-----------|:---------|:-------------------------------------------|:----------|
| id         | BIGINT   | PK, AUTO_INCREMENT                         | 출석 기록 식별자 |
| member_id  | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE) | 구독 회원 식별자 |
| check_date | DATE     | NOT NULL                                   | 출석일자      |
| created_at | DATETIME | DEFAUL CURRENT_TIMESTAMP                   | 실제 출석 버튼 클릭 일시          |

### 1.2.13 chat_room(1:1 채팅방)
| 컬럼명        | 데이터 타입   | 제약 조건                                      | 설명        |
|:-----------|:---------|:-------------------------------------------|:----------|
| id         | BIGINT   | PK, AUTO_INCREMENT                         | 채팅방 식별자   |
| member1_id | BIGINT   | NOT NULL, FK (member.id ON DELETE CASCADE) | 채팅 참여자 1  |
| member2_id | BIGINT     | NOT NULL, FK                               | 채팅 참여자 2  |
| created_at | DATETIME | DEFAUL CURRENT_TIMESTAMP                   | 채팅방 생성 일시 |

### 1.2.14 chat_message(채팅 메시지 이력)
| 컬럼명        | 데이터 타입   | 제약 조건                                         | 설명        |
|:-----------|:---------|:----------------------------------------------|:----------|
| id         | BIGINT   | PK, AUTO_INCREMENT                            | 메시지 식별자   |
| room_id    | BIGINT   | NOT NULL, FK (chat_room.id ON DELETE CASCADE) | 속한 채팅방 ID |
| sender_id  | BIGINT   | NOT NULL, FK  (member.id 참조)                  | 메시지 보낸 사람 |
| message    | TEXT     | NOT NULL                | 메시지 본문    |
| created_at | DATETIME | DEFAUL CURRENT_TIMESTAMP                      | 매시지 전송 시각 |

