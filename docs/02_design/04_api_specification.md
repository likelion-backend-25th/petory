# 1. REST API 명세서 및 더미 데이터 규격서 (사자그램 SNS)

## 목차
- [1. REST API 명세서 및 더미 데이터 규격서 (사자그램 SNS)](#1-rest-api-명세서-및-더미-데이터-규격서-사자그램-sns)
- [1.1 공통 응답 규격](#11-공통-응답-규격)
- [1.2 인증 및 회원 API](#12-인증-및-회원-api)
- [1.3 피드 게시글 API](#13-피드-게시글-api)
- [1.4 소셜 인터랙션 API (댓글/좋아요/북마크)](#14-소셜-인터랙션-api-댓글좋아요북마크)
- [1.5 결제 및 VIP 구독 API](#15-결제-및-vip-구독-api)

---

## 1.1 공통 응답 규격

### 1.1.1 성공 응답 포맷 (HTTP 200 / 201)
- 별도의 공통 래퍼 봉투 없이 각 컨트롤러 계층에서 DTO 단건 객체 또는 List 컬렉션을 HTTP Body로 직접 반환.
- 단건 응답 샘플 (PostResponse):
```json
{
  "id": 1,
  "memberId": 1,
  "content": "스프링 부트 4와 MyBatis 연동 실습 중입니다! 매퍼 XML과 동적 SQL 바인딩이 생각보다 매우 강력하네요.",
  "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
  "likeCount": 3,
  "createdAt": "2026-08-11T10:00:00",
  "updatedAt": "2026-08-11T10:00:00"
}
```
- 다건 목록 응답 샘플 (List<PostResponse>):
```json
[
  {
    "id": 1,
    "memberId": 1,
    "content": "스프링 부트 4와 MyBatis 연동 실습 중입니다! 매퍼 XML과 동적 SQL 바인딩이 생각보다 매우 강력하네요.",
    "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
    "likeCount": 3,
    "createdAt": "2026-08-11T10:00:00",
    "updatedAt": "2026-08-11T10:00:00"
  },
  {
    "id": 2,
    "memberId": 1,
    "content": "오늘 점심은 판교 맛집에서 라멘을 먹었습니다. 개발할 땐 든든하게 먹어야 집중이 잘 됩니다!",
    "imageUrl": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624",
    "likeCount": 1,
    "createdAt": "2026-08-12T12:30:00",
    "updatedAt": "2026-08-12T12:30:00"
  }
]
```

### 1.1.2 실패 및 에러 응답 포맷 (ApiErrorResponse)
- HTTP 상태 코드: 400, 401, 403, 404, 500
```json
{
  "code": "AUTH_INVALID_TOKEN",
  "message": "유효하지 않거나 만료된 토큰입니다.",
  "status": 401,
  "timestamp": "2026-08-25T14:30:00",
  "errors": []
}
```
- Bean Validation 검증 실패 시 에러 상세(`errors`) 포맷:
```json
{
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값 검증에 실패하였습니다.",
  "status": 400,
  "timestamp": "2026-08-25T14:30:00",
  "errors": [
    {
      "field": "content",
      "rejectedValue": "",
      "reason": "본문 내용은 필수 입니다."
    }
  ]
}
```

---

## 1.2 인증 및 회원 API

### 1.2.1 사용자 로그인 및 토큰 발급
- Method: `POST`
- URI: `/api/v1/auth/login`
- 인증 필요 여부: 불필요 (Public)
- Request Body (LoginRequest):
```json
{
  "email": "admin@example.com",
  "password": "password123!"
}
```
- Response Body (HTTP 200, TokenResponse):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzI2NTAwMDAwLCJleHAiOjE3MjY1MDM2MDB9.abc...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzI2NTAwMDAwLCJleHAiOjE3Mjc3MDk2MDB9.xyz...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 1.2.2 액세스 토큰 갱신 (RTR)
- Method: `POST`
- URI: `/api/v1/auth/refresh`
- 인증 필요 여부: 불필요 (Refresh Token 자체 검증)
- Request Body (RefreshTokenRequest):
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzI2NTAwMDAwLCJleHAiOjE3Mjc3MDk2MDB9.xyz..."
}
```
- Response Body (HTTP 200, TokenResponse):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzI2NTA0MDAwLCJleHAiOjE3MjY1MDc2MDB9.new_abc...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzI2NTA0MDAwLCJleHAiOjE3Mjc3MTM2MDB9.new_xyz...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 1.2.3 내 프로필 조회
- Method: `GET`
- URI: `/api/v1/members/me`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Response Body (HTTP 200, MemberProfileResponse - schema.sql 1번 회원 기준):
```json
{
  "id": 1,
  "email": "admin@example.com",
  "nickname": "스프링러버",
  "profileImage": "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde",
  "role": "ROLE_ADMIN",
  "createdAt": "2026-08-01T09:15:00"
}
```

---

## 1.3 피드 게시글 API

### 1.3.1 게시글 목록 조회 및 검색
- Method: `GET`
- URI: `/api/v1/posts`
- Query Parameters (PostSearchRequest):
  - `keyword`: 본문 검색 키워드 (선택, 예: `스프링`)
  - `memberId`: 특정 작성자 회원 ID 필터 (선택, 예: `1`)
  - `targetMemberIds`: 다중 회원 ID 목록 필터 (선택)
  - `sortOrder`: 정렬 조건 (선택, `latest`, `oldest` 등)
- Response Body (HTTP 200, List<PostResponse> - schema.sql 실제 데이터):
```json
[
  {
    "id": 1,
    "memberId": 1,
    "content": "스프링 부트 4와 MyBatis 연동 실습 중입니다! 매퍼 XML과 동적 SQL 바인딩이 생각보다 매우 강력하네요.",
    "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
    "likeCount": 3,
    "createdAt": "2026-08-11T10:00:00",
    "updatedAt": "2026-08-11T10:00:00"
  },
  {
    "id": 2,
    "memberId": 1,
    "content": "오늘 점심은 판교 맛집에서 라멘을 먹었습니다. 개발할 땐 든든하게 먹어야 집중이 잘 됩니다!",
    "imageUrl": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624",
    "likeCount": 1,
    "createdAt": "2026-08-12T12:30:00",
    "updatedAt": "2026-08-12T12:30:00"
  },
  {
    "id": 3,
    "memberId": 2,
    "content": "자바 25의 새로운 패턴 매칭 문법과 레코드 패턴을 프로젝트에 도입해 보았습니다. 코드가 훨씬 간결해집니다.",
    "imageUrl": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97",
    "likeCount": 4,
    "createdAt": "2026-08-13T14:00:00",
    "updatedAt": "2026-08-13T14:00:00"
  }
]
```

### 1.3.2 신규 피드 등록
- Method: `POST`
- URI: `/api/v1/posts`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Request Body (PostCreateRequest):
```json
{
  "content": "Docker Compose를 활용하여 로컬 개발 환경에 MySQL과 Redis 컨테이너를 1분 만에 구축하는 방법 정리.",
  "imageUrl": "https://images.unsplash.com/photo-1629654297299-c8506221ca97"
}
```
- Response Headers:
  - `Location: /api/v1/posts/11`
- Response Body (HTTP 201 Created, PostResponse):
```json
{
  "id": 11,
  "memberId": 1,
  "content": "Docker Compose를 활용하여 로컬 개발 환경에 MySQL과 Redis 컨테이너를 1분 만에 구축하는 방법 정리.",
  "imageUrl": "https://images.unsplash.com/photo-1629654297299-c8506221ca97",
  "likeCount": 0,
  "createdAt": "2026-08-25T14:30:00",
  "updatedAt": "2026-08-25T14:30:00"
}
```

### 1.3.3 피드 단건 상세 조회
- Method: `GET`
- URI: `/api/v1/posts/{id}`
- Path Variable: `id` (게시글 식별자 ID, 예: `1`)
- Response Body (HTTP 200, PostDetailResponse - schema.sql 1번 게시글 및 댓글 연동):
```json
{
  "id": 1,
  "content": "스프링 부트 4와 MyBatis 연동 실습 중입니다! 매퍼 XML과 동적 SQL 바인딩이 생각보다 매우 강력하네요.",
  "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
  "createdAt": "2026-08-11T10:00:00",
  "author": {
    "id": 1,
    "nickname": "스프링러버",
    "profileImage": "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde"
  },
  "comments": [
    {
      "id": 1,
      "commenterId": 2,
      "commenterNickname": "자바마스터",
      "content": "스프링 부트와 MyBatis 조합은 국내 실무 엔터프라이즈 환경에서 여전히 널리 쓰입니다! 응원합니다.",
      "createdAt": "2026-08-11T10:30:00"
    },
    {
      "id": 2,
      "commenterId": 3,
      "commenterNickname": "코딩하는고양이",
      "content": "동적 SQL <where>랑 <if> 태그 다룰 때 가독성에 신경 쓰시면 훨씬 유지보수하기 편해져요!",
      "createdAt": "2026-08-11T14:10:00"
    },
    {
      "id": 3,
      "commenterId": 4,
      "commenterNickname": "개발꿈나무",
      "content": "저도 지금 MyBatis 공부 중인데 많은 자극이 되네요!",
      "createdAt": "2026-08-12T09:20:00"
    }
  ]
}
```

### 1.3.4 피드 본문 수정
- Method: `PUT`
- URI: `/api/v1/posts/{id}`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Request Body (PostUpdateRequest):
```json
{
  "content": "스프링 부트 4와 MyBatis 연동 실습 내용 보강 완료! 동적 쿼리 성능 최적화 팁 추가.",
  "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c"
}
```
- Response Body (HTTP 200, PostResponse):
```json
{
  "id": 1,
  "memberId": 1,
  "content": "스프링 부트 4와 MyBatis 연동 실습 내용 보강 완료! 동적 쿼리 성능 최적화 팁 추가.",
  "imageUrl": "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
  "likeCount": 3,
  "createdAt": "2026-08-11T10:00:00",
  "updatedAt": "2026-08-25T15:00:00"
}
```

### 1.3.5 피드 삭제
- Method: `DELETE`
- URI: `/api/v1/posts/{id}`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Path Variable: `id` (삭제 대상 게시글 ID)
- Response Body: HTTP 204 No Content

---

## 1.4 소셜 인터랙션 API (댓글/좋아요/북마크)

### 1.4.1 댓글 등록
- Method: `POST`
- URI: `/api/v1/posts/{postId}/comments`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Path Variable: `postId` (대상 게시글 ID, 예: `1`)
- Request Body:
```json
{
  "content": "좋은 글 잘 읽었습니다! 가독성이 확실히 좋아 보이네요."
}
```
- Response Body (HTTP 201 Created, CommentResponse):
```json
{
  "id": 13,
  "commenterId": 1,
  "commenterNickname": "스프링러버",
  "content": "좋은 글 잘 읽었습니다! 가독성이 확실히 좋아 보이네요.",
  "createdAt": "2026-08-25T15:30:00"
}
```

### 1.4.2 댓글 삭제
- Method: `DELETE`
- URI: `/api/v1/posts/{postId}/comments/{commentId}`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Path Variable:
  - `postId`: 대상 게시글 ID
  - `commentId`: 삭제 대상 댓글 ID
- Response Body: HTTP 204 No Content

### 1.4.3 피드 좋아요 토글
- Method: `POST`
- URI: `/api/v1/posts/{postId}/likes`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Path Variable: `postId` (대상 게시글 ID, 예: `1`)
- Response Body (HTTP 200, LikeToggleResponse):
```json
{
  "liked": true,
  "likeCount": 4
}
```

### 1.4.4 피드 북마크 토글
- Method: `POST`
- URI: `/api/v1/posts/{postId}/bookmarks`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Path Variable: `postId` (대상 게시글 ID, 예: `3`)
- Response Body (HTTP 200 더미 데이터):
```json
{
  "postId": 3,
  "bookmarked": true
}
```

---

## 1.5 결제 및 VIP 구독 API

### 1.5.1 결제 사전 준비 및 금액 등록
- Method: `POST`
- URI: `/api/v1/payments/prepare`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Request Body:
```json
{
  "merchantUid": "ORD_20260825_1001",
  "amount": 9900
}
```
- Response Body (HTTP 200):
```json
{
  "merchantUid": "ORD_20260825_1001",
  "amount": 9900,
  "status": "READY"
}
```

### 1.5.2 결제 사후 검증 및 승인
- Method: `POST`
- URI: `/api/v1/payments/complete`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Request Body:
```json
{
  "impUid": "imp_1234567890",
  "merchantUid": "ORD_20260825_1001"
}
```
- Response Body (HTTP 200):
```json
{
  "paymentId": 1,
  "impUid": "imp_1234567890",
  "merchantUid": "ORD_20260825_1001",
  "amount": 9900,
  "status": "PAID",
  "paidAt": "2026-08-25T16:00:00"
}
```

### 1.5.3 VIP 정기 구독 빌링키 등록 및 활성화
- Method: `POST`
- URI: `/api/v1/subscriptions`
- 인증 필요 여부: 필수 (`Authorization: Bearer <accessToken>`)
- Request Body:
```json
{
  "customerUid": "billing_user_1_card_9981",
  "planName": "VIP_MONTHLY"
}
```
- Response Body (HTTP 201 Created):
```json
{
  "subscriptionId": 1,
  "memberId": 1,
  "planName": "VIP_MONTHLY",
  "price": 9900,
  "status": "ACTIVE",
  "role": "ROLE_VIP",
  "nextBillingAt": "2026-09-25T00:00:00",
  "startedAt": "2026-08-25T16:05:00"
}
```

### 1.5.4 결제 비동기 웹훅(Webhook) 수신
- Method: `POST`
- URI: `/api/v1/payments/webhook`
- 인증 필요 여부: 불필요 (결제 게이트웨이 서버 호출)
- Request Body (결제 게이트웨이 전송 규격):
```json
{
  "imp_uid": "imp_1234567890",
  "merchant_uid": "ORD_20260825_1001",
  "status": "paid"
}
```
- Response Body (HTTP 200): `"OK"`
