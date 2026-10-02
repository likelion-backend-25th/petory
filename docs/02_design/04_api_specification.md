# 1. REST API 명세서 (Petory)

## 목차

- [1. REST API 명세서 (Petory)](#1-rest-api-명세서-petory)
- [1.1 공통 규격](#11-공통-규격)
- [1.2 서버 상태](#12-서버-상태)
- [1.3 인증](#13-인증)
- [1.4 회원 및 프로필](#14-회원-및-프로필)
- [1.5 피드 게시글](#15-피드-게시글)
- [1.6 QnA](#16-qna)
- [1.7 댓글, 좋아요, 북마크](#17-댓글-좋아요-북마크)
- [1.8 팔로우 및 랭킹](#18-팔로우-및-랭킹)
- [1.9 실종동물](#19-실종동물)
- [1.10 파일 업로드](#110-파일-업로드)
- [1.11 결제](#111-결제)
- [1.12 구독 플랜](#112-구독-플랜)
- [1.13 구독 결제](#113-구독-결제)
- [1.14 관리자 회원](#114-관리자-회원)

---

## 1.1 공통 규격

기본 경로는 `/api/v1`. 성공 응답은 공통 래퍼 없이 컨트롤러가 반환하는 DTO, 컬렉션, 숫자, 빈 본문을 그대로 JSON으로 보낸다. 인증이 필요한 API는 `Authorization: Bearer <accessToken>` 헤더를 사용한다.

`SecurityConfig`에서 비로그인 허용 경로는 아래와 같다. 그 외 `/api/v1/**`는 인증이 필요하다.

| 구분 | Method | URI |
| --- | --- | --- |
| 로그인·갱신 | POST | `/api/v1/login`, `/api/v1/refresh` |
| 가입·중복 확인 | POST / GET | `/api/v1/signup`, `/api/v1/email/exists`, `/api/v1/nickname/exists` |
| 피드 조회 | GET | `/api/v1/posts`, `/api/v1/posts/**` |
| 프로필·작성 피드 | GET | `/api/v1/profile/{memberId}`, `/api/v1/profile/{memberId}/posts` |
| 결제 웹훅 | POST | `/api/v1/payments/webhook` |
| 문서 | GET | `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs`, `/v3/api-docs/**` |

QnA, 실종동물, 팔로우, 랭킹, 결제(웹훅 제외), 구독, 파일 Presign, 관리자 API는 인증이 필요하다. Access Token 유효 기간은 1시간(`expiresIn: 3600`), Refresh Token 유효 기간은 7일이다.

### 1.1.1 목록 페이지 응답

피드·QnA·팔로우 목록은 `SliceResponse`를 쓴다. `content`는 항목 배열, `hasNext`는 다음 페이지 여부, `lastPostId`는 다음 요청에 넘길 커서다. 팔로우 목록에서도 커서 필드명은 `lastPostId`이며 값은 `follow.id`다.

```json
{
  "content": [],
  "hasNext": false,
  "lastPostId": null
}
```

실종 목록은 `MissingPetListPageResponse`(`totalCount`, `items`, `nextCursor`, `hasNext`), 랭킹은 `RankingSliceResponse`(`content`, `hasNext`, `lastMemberId`, `lastFollowerCount`)를 쓴다.

### 1.1.2 실패 응답 (`ApiErrorResponse`)

`GlobalRestExceptionHandler`가 `code`, `message`, `status`, `timestamp`, `errors`를 반환한다.

| 예외 | HTTP | code |
| --- | --- | --- |
| Bean Validation 실패 | 400 | `INVALID_INPUT_VALUE` |
| `IllegalArgumentException` | 400 | `BUSINESS_RULE_VIOLATION` |
| 인증 실패·미인증 | 401 | `UNAUTHORIZED_ACCESS` |
| `AccessDeniedException`, `IllegalStateException` | 403 | `FORBIDDEN_OPERATION` |
| `NoSuchElementException` | 404 | `RESOURCE_NOT_FOUND` |
| `PaymentGatewayException` | 502 | `PAYMENT_GATEWAY_ERROR` |
| 그 외 | 500 | `INTERNAL_SERVER_ERROR` |

필터에서 막힌 미인증·인가 실패는 `CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`가 같은 형식으로 401·403을 반환한다.

```json
{
  "code": "UNAUTHORIZED_ACCESS",
  "message": "유효하지 않거나 만료된 Refresh Token입니다.",
  "status": 401,
  "timestamp": "2026-10-02T09:30:00",
  "errors": []
}
```

검증 실패 시 `errors`에 필드별 사유가 담긴다.

```json
{
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값 검증에 실패했습니다.",
  "status": 400,
  "timestamp": "2026-10-02T09:30:00",
  "errors": [
    {
      "field": "content",
      "rejectedValue": "",
      "reason": "댓글 본문은 비어 있을 수 없습니다."
    }
  ]
}
```

---

## 1.2 서버 상태

### 1.2.1 기동 확인

- Method: `GET`
- URI: `/api/v1/running`
- 컨트롤러: `running/RunningController`
- 인증: 필요
- Response Body (HTTP 200):

```json
{
  "status": "UP",
  "message": "Pet Club Backend Server is running!"
}
```

---

## 1.3 인증

컨트롤러: `AuthRestController` (`/api/v1`). 소셜 로그인은 `SecurityConfig`의 OAuth2 로그인과 `OAuth2SuccessHandler`가 처리한다.

### 1.3.1 이메일 로그인

- Method: `POST`
- URI: `/api/v1/login`
- 인증: 불필요
- Request Body (`LoginRequest`):

```json
{
  "email": "nabi@petory.com",
  "password": "password123"
}
```

- Response Body (HTTP 200, `TokenResponse`):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

이메일 또는 비밀번호가 틀리면 HTTP 401, `UNAUTHORIZED_ACCESS`.

### 1.3.2 토큰 갱신 (RTR)

- Method: `POST`
- URI: `/api/v1/refresh`
- 인증: 불필요. 본문의 Refresh Token 서명·만료와 `issued_refresh_token` 저장 여부를 검사한다.
- Request Body (`RefreshTokenRequest`):

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

- Response Body (HTTP 200, `TokenResponse`): 로그인과 같은 형식의 새 Access Token·Refresh Token. 기존 Refresh Token 행을 새 값으로 갱신한다.
- `refreshToken`이 비어 있으면 HTTP 400. 서명이 틀리거나 만료됐거나 DB에 없으면 HTTP 401.

### 1.3.3 소셜 로그인 (Google / Kakao)

브라우저가 아래 주소로 이동하면 Spring Security가 인가 코드를 받고, 성공 시 `https://petory.likelion.shop`으로 302 리다이렉트한다. 쿼리에 `accessToken`, `refreshToken`이 붙는다.

- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/kakao`
- 콜백: `GET /login/oauth2/code/{registrationId}` (`google`, `kakao`)

---

## 1.4 회원 및 프로필

컨트롤러: `MemberRestController` (`/api/v1`).

### 1.4.1 이메일 중복 확인

- Method: `GET`
- URI: `/api/v1/email/exists?email={email}`
- 인증: 불필요
- Response Body (HTTP 200): 이미 가입된 이메일이면 `true`, 아니면 `false`
- 값이 비어 있으면 HTTP 400

### 1.4.2 닉네임 중복 확인

- Method: `GET`
- URI: `/api/v1/nickname/exists?nickname={nickname}`
- 인증: 불필요
- Response Body (HTTP 200): 사용 중이면 `true`, 아니면 `false`

### 1.4.3 회원가입

- Method: `POST`
- URI: `/api/v1/signup`
- 인증: 불필요
- Request Body (`SignUpRequest`):

```json
{
  "email": "nabi@petory.com",
  "password": "password123",
  "nickname": "나비",
  "species": "고양이",
  "sex": "여",
  "birthDate": "2022-03-14",
  "intro": "안녕하세요",
  "profileImage": "",
  "address": "서울시 마포구",
  "isAgreed": true
}
```

`intro` 기본값은 `"안녕하세요"`, `profileImage`·`address` 기본값은 빈 문자열이다. 비밀번호는 BCrypt로 저장한다.

- Response Body (HTTP 200, `SignUpResponse`):

```json
{
  "id": 4,
  "email": "nabi@petory.com",
  "status": "ACTIVE",
  "role": "ROLE_USER",
  "createdAt": "2026-10-02T09:00:00",
  "infoProvideAgreement": "2026-10-02T09:00:00"
}
```

### 1.4.4 프로필 조회

- Method: `GET`
- URI: `/api/v1/profile/{memberId}`
- 인증: 불필요. 로그인 상태면 본인 여부를 구분해 응답 타입이 달라진다.
- 본인 (`MyProfile`): `id`, `email`, `nickname`, `species`, `sex`, `birthDate`, `intro`, `profileImage`, `address`, `status`, `role`, `createdAt`, `infoProvideAgreement`, `postsCount`, `followers`, `followings`
- 타인·비로그인 (`MemberProfile`): `id`, `nickname`, `intro`, `profileImage`, `status`, `role`, `createdAt`, `postsCount`, `followers`, `followings`, `following` (Java 필드명 `isFollowing`)
- 회원이 없으면 HTTP 404

본인 조회 예시:

```json
{
  "id": 1,
  "email": "mungchi@petory.com",
  "nickname": "뭉치",
  "species": "개",
  "sex": "수",
  "birthDate": "2020-05-01",
  "intro": "안녕하세요",
  "profileImage": "https://example.com/me.png",
  "address": "서울시 강남구",
  "status": "ACTIVE",
  "role": "ROLE_USER",
  "createdAt": "2026-08-01T09:15:00",
  "infoProvideAgreement": "2026-08-01T09:15:00",
  "postsCount": 10,
  "followers": 3,
  "followings": 5
}
```

### 1.4.5 프로필 피드 목록

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/posts`
- 인증: 불필요
- Response Body (HTTP 200, `List<MyPagePostResponse>`):

```json
[
  {
    "id": 10,
    "content": "오늘 한강 산책했어요",
    "isSubscriberOnly": false,
    "hashtags": "#산책 #일상",
    "imageUrl": "https://example.com/img1.png",
    "likeCount": 3
  }
]
```

### 1.4.6 프로필 QnA 목록

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/qna`
- 인증: 필요
- Response Body (HTTP 200): 1.4.5와 같은 `MyPagePostResponse` 배열. 해당 회원이 작성한 QnA(`post_main.type = 2`)만 포함한다.

### 1.4.7 내 북마크 목록

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/bookmarks`
- 인증: 필요. 경로의 `memberId`는 로그인 회원 ID와 같아야 한다. 타인이면 HTTP 403.
- Response Body (HTTP 200): `MyPagePostResponse` 배열

### 1.4.8 프로필 수정

- Method: `POST`
- URI: `/api/v1/profile/{memberId}/edit`
- 인증: 필요. 본인만 수정할 수 있다. 타인이면 HTTP 403, 회원이 없으면 HTTP 404.
- Request Body (`MemberProfileEditRequest`):

```json
{
  "nickname": "뭉치",
  "species": "개",
  "sex": "수",
  "birthDate": "2020-05-01",
  "intro": "산책을 좋아해요",
  "profileImage": "https://example.com/me.png",
  "address": "서울시 강남구"
}
```

- Response Body: HTTP 200, 본문 없음

### 1.4.9 회원 탈퇴

- Method: `POST`
- URI: `/api/v1/profile/{memberId}/delete`
- 인증: 필요. 본인만 삭제할 수 있다.
- Response Body: HTTP 200, 본문 없음

### 1.4.10 인증 정보 확인 (디버깅)

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/auth-info`
- Swagger `@Hidden`. 로그인 이메일의 권한, 접속 IP, 인증 여부를 반환한다.

---

## 1.5 피드 게시글

컨트롤러: `PostController` (`/api/v1/posts`). `post_main.type = 1`이 피드다. 목록은 커서 기반이며 `size + 1`건을 조회해 `hasNext`를 판단한다.

### 1.5.1 피드 목록

- Method: `GET`
- URI: `/api/v1/posts`
- 인증: 불필요
- Query: `lastPostId` (선택, 이전 응답의 마지막 게시글 ID), `size` (기본 10)
- 예: `GET /api/v1/posts?lastPostId=25&size=10`
- Response Body (HTTP 200, `SliceResponse<PostListResponse>`):

```json
{
  "content": [
    {
      "id": 12,
      "type": 1,
      "content": "오늘 새로운 산책 코스를 발견했어요",
      "bgmUrl": null,
      "isSubscriberOnly": false,
      "hashtags": "#산책 #일상",
      "createdAt": "2026-10-01T10:00:00",
      "updatedAt": "2026-10-01T10:00:00",
      "memberId": 1,
      "nickname": "뭉치",
      "profileImage": "https://example.com/me.png",
      "imageUrls": [
        "https://example.com/uploads/posts/1/a.jpg"
      ],
      "likeCount": 3,
      "commentCount": 1,
      "viewCount": 20
    }
  ],
  "hasNext": true,
  "lastPostId": 12
}
```

### 1.5.2 해시태그 검색

- Method: `GET`
- URI: `/api/v1/posts/search`
- 인증: 불필요
- Query: `hashtag` (필수, `#` 제외), `lastPostId` (선택), `size` (기본 10)
- 예: `GET /api/v1/posts/search?hashtag=강아지&size=10`
- Response Body (HTTP 200): 1.5.1과 같은 `SliceResponse<PostListResponse>`

### 1.5.3 피드 상세

- Method: `GET`
- URI: `/api/v1/posts/{postId}`
- 인증: 불필요. 로그인 사용자는 조회수(`VIEW`)를 `INSERT IGNORE`로 한 번만 올린다.
- Response Body (HTTP 200, `PostDetailResponse`):

```json
{
  "id": 12,
  "content": "오늘 새로운 산책 코스를 발견했어요",
  "bgmUrl": null,
  "isSubscriberOnly": 0,
  "hashtags": "#산책 #일상",
  "memberId": 1,
  "authorName": "뭉치",
  "authorProfileImage": "https://example.com/me.png",
  "createdAt": "2026-10-01T10:00:00",
  "updatedAt": "2026-10-01T10:00:00",
  "comments": [
    {
      "id": 3,
      "commenterId": 2,
      "commenterNickname": "나비",
      "content": "다음에 같이 가요",
      "createdAt": "2026-10-01T11:00:00"
    }
  ],
  "viewCount": 21
}
```

상세의 `isSubscriberOnly`는 정수(`0` 전체 공개, `1` 구독자 전용)다. 목록 항목은 boolean이다.

### 1.5.4 피드 등록

- Method: `POST`
- URI: `/api/v1/posts`
- 인증: 필요. 작성자 ID는 JWT에서 넣으며 본문의 `memberId`는 사용하지 않는다.
- Request Body (`PostCreateRequest`):

```json
{
  "content": "오늘 새로운 산책 코스를 발견했어요",
  "bgmUrl": "https://example.com/bgm.mp3",
  "isSubscriberOnly": 0,
  "hashtags": "#산책 #일상",
  "imageUrls": [
    "https://example.com/uploads/posts/1/a.jpg"
  ]
}
```

- Response Body (HTTP 201, `PostCreateResponse`):

```json
{
  "id": 13
}
```

### 1.5.5 피드 수정

- Method: `PUT`
- URI: `/api/v1/posts/{postId}`
- 인증: 필요. 작성자만 수정할 수 있다.
- Request Body (`PostUpdateRequest`): `content`, `bgmUrl`, `isSubscriberOnly`, `hashtags`. 이미지 목록은 수정 본문에 없다.
- Response Body: HTTP 200, 본문 없음

### 1.5.6 피드 삭제

- Method: `DELETE`
- URI: `/api/v1/posts/{postId}`
- 인증: 필요. 작성자만 삭제할 수 있다. 삭제 후 버킷 URL로 시작하는 첨부 이미지를 S3에서 지운다.
- Response Body: HTTP 204 No Content

---

## 1.6 QnA

컨트롤러: `QnaController` (`/api/v1/qna`). 같은 `post_main`에서 `type = 2`인 글이다. 요청·응답 DTO는 피드와 같고, 목록·검색·상세·등록·수정·삭제 모두 인증이 필요하다.

| 기능 | Method | URI | 응답 |
| --- | --- | --- | --- |
| 목록 | GET | `/api/v1/qna?lastPostId=&size=10` | `SliceResponse<PostListResponse>` |
| 해시태그 검색 | GET | `/api/v1/qna/search?hashtag=&lastPostId=&size=10` | `SliceResponse<PostListResponse>` |
| 상세 | GET | `/api/v1/qna/{postId}` | `PostDetailResponse` |
| 등록 | POST | `/api/v1/qna` | HTTP 201, `PostCreateResponse` |
| 수정 | PUT | `/api/v1/qna/{postId}` | HTTP 200, 본문 없음 |
| 삭제 | DELETE | `/api/v1/qna/{postId}` | HTTP 204 |

등록 본문은 1.5.4 `PostCreateRequest`, 수정 본문은 1.5.5 `PostUpdateRequest`와 같다.

---

## 1.7 댓글, 좋아요, 북마크

댓글 목록 API는 없다. 댓글은 피드·QnA 상세의 `comments`로 내려온다.

### 1.7.1 댓글 등록

- Method: `POST`
- URI: `/api/v1/posts/{postId}/comments`
- 컨트롤러: `CommentController`
- 인증: 필요
- Request Body (`CommentCreateRequest`):

```json
{
  "content": "좋은 글 잘 읽었습니다"
}
```

- Response Body (HTTP 201, `CommentResponse`):

```json
{
  "id": 8,
  "commenterId": 2,
  "commenterNickname": "나비",
  "content": "좋은 글 잘 읽었습니다",
  "createdAt": "2026-10-02T09:40:00"
}
```

### 1.7.2 댓글 수정

- Method: `PUT`
- URI: `/api/v1/posts/{postId}/comments/{commentId}`
- 인증: 필요. 작성자만 수정할 수 있다.
- Request Body (`CommentUpdateRequest`):

```json
{
  "content": "내용을 조금 고쳤습니다"
}
```

- Response Body (HTTP 200, `CommentResponse`): 1.7.1과 같은 형식

### 1.7.3 댓글 삭제

- Method: `DELETE`
- URI: `/api/v1/posts/{postId}/comments/{commentId}`
- 인증: 필요. 작성자만 삭제할 수 있다.
- Response Body: HTTP 204 No Content

### 1.7.4 좋아요 토글

- Method: `POST`
- URI: `/api/v1/posts/{postId}/likes`
- 컨트롤러: `PostInteractionController`
- 인증: 필요. 본문 없음. 없으면 등록, 있으면 취소한다.
- Response Body (HTTP 200, `LikeToggleResponse`):

```json
{
  "liked": true,
  "likeCount": 4
}
```

### 1.7.5 북마크 토글

- Method: `POST`
- URI: `/api/v1/posts/{postId}/bookmarks`
- 컨트롤러: `PostBookmarkController`
- 인증: 필요. 본문 없음.
- Response Body (HTTP 200, `BookmarkToggleResponse`):

```json
{
  "postId": 3,
  "bookmarked": true
}
```

`post_interaction`은 `LIKE`, `BOOKMARK`, `VIEW`를 회원·게시글·종류 복합 UNIQUE로 한 종류당 한 행만 둔다.

---

## 1.8 팔로우 및 랭킹

### 1.8.1 팔로우 토글

- Method: `POST` 또는 `DELETE`
- URI: `/api/v1/profile/{memberId}/follow`
- 컨트롤러: `FollowController`
- 인증: 필요. 두 메서드 모두 `toggleFollow`를 호출한다. 경로의 `memberId`는 팔로우 대상이고, 로그인 회원이 팔로워다.
- Response Body (HTTP 200, `FollowToggleResponse`):

```json
{
  "following": true,
  "followerCount": 4
}
```

### 1.8.2 팔로워 목록

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/followers`
- 인증: 필요
- Query: `lastFollowId` (선택, `follow.id`), `size` (기본 10)
- Response Body (HTTP 200, `SliceResponse<FollowMemberResponse>`):

```json
{
  "content": [
    {
      "followId": 7,
      "memberId": 3,
      "nickname": "나비",
      "profileImage": "https://example.com/nabi.png",
      "following": true
    }
  ],
  "hasNext": false,
  "lastPostId": 7
}
```

다음 페이지 쿼리는 `lastFollowId`에 위 `lastPostId`(follow PK)를 넣는다.

### 1.8.3 팔로잉 목록

- Method: `GET`
- URI: `/api/v1/profile/{memberId}/followings`
- 인증: 필요
- Query·응답 형식: 1.8.2와 같다.

### 1.8.4 인기펫 랭킹

- Method: `GET`
- URI: `/api/v1/ranking`
- 컨트롤러: `RankingController`
- 인증: 필요
- Query: `lastFollowerCount` (선택), `lastMemberId` (선택), `size` (기본 20). 메인 피드는 `size=5`, 랭킹 페이지는 `size=20`.
- 정렬: ACTIVE 회원을 팔로워 수 내림차순, 같으면 회원 ID 내림차순.
- 예: `GET /api/v1/ranking?size=20&lastFollowerCount=100&lastMemberId=3`
- Response Body (HTTP 200, `RankingSliceResponse`):

```json
{
  "content": [
    {
      "memberId": 3,
      "nickname": "뭉이",
      "profileImage": "https://example.com/mung.png",
      "followerCount": 14
    }
  ],
  "hasNext": true,
  "lastMemberId": 3,
  "lastFollowerCount": 14
}
```

---

## 1.9 실종동물

컨트롤러: `MissingPetController` (`/api/v1/missing-pets`). 목록·상세·등록·수정·상태 변경 모두 인증이 필요하다. 등록 직후 상태는 `MISSING`이고, 작성자만 수정·상태 변경할 수 있다. 상태 전이는 `MISSING`에서 `FOUND` 또는 `CANCELLED`다. 타인 수정은 HTTP 403.

목격 제보(`missing_pet_report`)는 상세 응답의 `reports`로만 내려온다. 제보 등록 API는 없다.

### 1.9.1 목록

- Method: `GET`
- URI: `/api/v1/missing-pets`
- Query: `cursor` (선택, 이전 `nextCursor`), `size` (기본 10)
- Response Body (HTTP 200, `MissingPetListPageResponse`):

```json
{
  "totalCount": 42,
  "items": [
    {
      "id": 1,
      "imageUrl": "https://example.com/images/missing-pet.jpg"
    }
  ],
  "nextCursor": 1,
  "hasNext": true
}
```

### 1.9.2 상세

- Method: `GET`
- URI: `/api/v1/missing-pets/{id}`
- Response Body (HTTP 200, `MissingPetDetailResponse`):

```json
{
  "id": 1,
  "author": {
    "id": 2,
    "nickname": "나비",
    "profileImage": "https://example.com/nabi.png"
  },
  "missingDate": "2026-09-30",
  "missingAddress": "서울특별시 강남구 테헤란로 123",
  "detail": "갈색 푸들, 빨간 목줄을 착용하고 있습니다.",
  "imageUrl": "https://example.com/images/missing-pet.jpg",
  "status": "MISSING",
  "latitude": 37.500123,
  "longitude": 127.036456,
  "createdAt": "2026-09-30T10:00:00",
  "updatedAt": "2026-09-30T11:00:00",
  "reports": [
    {
      "id": 1,
      "reporter": {
        "id": 3,
        "nickname": "뭉이",
        "profileImage": "https://example.com/mung.png"
      },
      "address": "서울특별시 강남구 역삼동 123-45",
      "detail": "공원 입구 근처에서 봤습니다.",
      "imageUrl": "https://example.com/images/report.jpg",
      "sightAt": "2026-09-30T14:30:00",
      "latitude": 37.501234,
      "longitude": 127.03789,
      "createdAt": "2026-09-30T15:00:00"
    }
  ]
}
```

좌표는 `BigDecimal`이며 DB 컬럼은 `DECIMAL(10, 7)`이다. 없는 신고는 현재 `IllegalArgumentException`으로 HTTP 400이다.

### 1.9.3 등록

- Method: `POST`
- URI: `/api/v1/missing-pets`
- 인증: 필요. 작성자는 JWT 회원이다.
- Request Body (`MissingPetCreateRequest`):

```json
{
  "missingDate": "2026-09-30",
  "missingAddress": "서울특별시 강남구 테헤란로 123",
  "detail": "갈색 푸들, 빨간 목줄을 착용하고 있습니다.",
  "imageUrl": "https://example.com/images/missing-pet.jpg",
  "latitude": 37.500123,
  "longitude": 127.036456
}
```

- Response Body (HTTP 200): 생성된 신고 ID 숫자. 예: `1`

### 1.9.4 수정

- Method: `PUT`
- URI: `/api/v1/missing-pets/{id}`
- 인증: 필요. 작성자만 가능.
- Request Body (`MissingPetUpdateRequest`): 1.9.3과 같은 필드(`missingDate`, `missingAddress`, `detail`, `imageUrl`, `latitude`, `longitude`)
- Response Body: HTTP 200, 본문 없음

### 1.9.5 상태 변경

- Method: `PATCH`
- URI: `/api/v1/missing-pets/{id}/status`
- 인증: 필요. 작성자만 가능.
- Request Body (`MissingPetStatusUpdateRequest`):

```json
{
  "status": "FOUND"
}
```

`status` 값: `MISSING`, `FOUND`, `CANCELLED`.

- Response Body: HTTP 200, 본문 없음

---

## 1.10 파일 업로드

### 1.10.1 S3 Presigned URL 발급

- Method: `POST`
- URI: `/api/v1/files/presign`
- 컨트롤러: `FileController`
- 인증: 필요
- Request Body (`PresignUploadRequest`):

```json
{
  "filename": "dog.jpg",
  "contentType": "image/jpeg"
}
```

허용 Content-Type: `image/jpeg`, `image/png`, `image/webp`, `image/gif`, `audio/mpeg`, `audio/mp4`, `audio/wav`.

- Response Body (HTTP 200, `PresignUploadResponse`):

```json
{
  "uploadUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/posts/1/uuid.jpg?X-Amz-Algorithm=...",
  "fileUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/posts/1/uuid.jpg",
  "key": "uploads/posts/1/uuid.jpg"
}
```

객체 키는 `uploads/posts/{memberId}/{UUID}{확장자}`다. `uploadUrl` 유효 시간은 10분이며, 클라이언트가 그 URL로 S3에 PUT한 뒤 `fileUrl`을 게시글 `imageUrls`나 프로필 `profileImage`에 넣는다.

---

## 1.11 결제

컨트롤러: `PaymentController` (`/api/v1/payments`). PortOne V2를 사용한다. 단건 결제(간식 쏘기 등)는 준비 → 프론트 결제창 → 완료 검증 순서다.

### 1.11.1 결제 준비

- Method: `POST`
- URI: `/api/v1/payments/prepare`
- 인증: 필요
- Request Body (`PaymentPrepareRequest`):

```json
{
  "targetMemberId": 2,
  "orderName": "간식 쏘기",
  "totalAmount": 3000,
  "merchandise": "간식 쏘기"
}
```

`targetMemberId`, `orderName`(100자 이하), `totalAmount`(1 이상), `merchandise`(30자 이하)는 필수다. 서버가 `payment` 행을 `READY`로 저장하고 주문번호 `paymentId`를 만든다.

- Response Body (HTTP 200, `PaymentPrepareResponse`):

```json
{
  "paymentId": "pay_20261001120000_10",
  "orderName": "간식 쏘기",
  "totalAmount": 3000,
  "currency": "KRW"
}
```

프론트는 이 값으로 PortOne 결제창을 연다.

### 1.11.2 결제 완료 검증

- Method: `POST`
- URI: `/api/v1/payments/complete`
- 인증: 필요. 로그인 회원의 결제만 검증한다.
- Request Body (`PaymentCompleteRequest`):

```json
{
  "paymentId": "pay_20261001120000_10"
}
```

서버는 웹훅·클라이언트 값을 믿지 않고 PortOne 결제 조회 결과와 저장된 금액을 비교한다.

- `PAID`이면 HTTP 200, 그 상태(`FAILED`, `CANCELLED`, `READY`)이면 HTTP 400. 본문 형식은 같다 (`PaymentCompleteResponse`).

```json
{
  "paymentId": "pay_20261001120000_10",
  "status": "PAID",
  "paidAmount": 3000,
  "message": "결제가 완료되었습니다."
}
```

### 1.11.3 PortOne 웹훅

- Method: `POST`
- URI: `/api/v1/payments/webhook`
- 인증: 불필요. PortOne 서버가 호출한다.
- Request Body (`PortOneWebhookRequest`, 알 수 없는 필드는 무시):

```json
{
  "type": "Transaction.Paid",
  "timestamp": "2026-10-01T12:03:00.000Z",
  "data": {
    "paymentId": "pay_20261001120000_10",
    "storeId": "store-id",
    "transactionId": "transaction-id"
  }
}
```

처리하거나 무시해도 HTTP 200과 빈 본문을 반환한다. 상태 반영은 PortOne API를 다시 조회한 결과를 기준으로 한다.

### 1.11.4 내 결제 내역

- Method: `GET`
- URI: `/api/v1/payments/me`
- 인증: 필요
- Response Body (HTTP 200, `List<PaymentHistoryResponse>`):

```json
[
  {
    "paymentId": "pay_20261001120000_10",
    "targetMemberId": 2,
    "orderName": "간식 쏘기",
    "currency": "KRW",
    "totalAmount": 3000,
    "paidAmount": 3000,
    "status": "PAID",
    "createdAt": "2026-10-01T12:00:00",
    "paidAt": "2026-10-01T12:03:00",
    "cancelledAt": null
  }
]
```

`status`: `READY`, `PAID`, `FAILED`, `CANCELLED`.

---

## 1.12 구독 플랜

컨트롤러: `SubscriptionController` (`/api/v1/subscriptionPlan`). Java `Subscription`은 크리에이터가 등록하는 **플랜**이다. 모두 인증이 필요하다. 생성·수정·삭제는 경로의 `memberId`가 로그인 회원과 같아야 한다.

### 1.12.1 플랜 생성

- Method: `POST`
- URI: `/api/v1/subscriptionPlan/{memberId}`
- Request Body (`SubscriptionCreateRequest`): 본문 `memberId`, 경로 `memberId`, 로그인 회원 ID가 모두 같아야 한다. 타인이면 HTTP 403.

```json
{
  "memberId": 2,
  "planName": "베이직",
  "price": 4900,
  "description": "월간 전용 피드 + 감사 메시지"
}
```

- Response Body: HTTP 200, 본문 없음

### 1.12.2 회원별 플랜 목록

- Method: `GET`
- URI: `/api/v1/subscriptionPlan/{memberId}`
- Response Body (HTTP 200, `List<Subscription>`):

```json
[
  {
    "id": 1,
    "memberId": 2,
    "planName": "베이직",
    "price": 4900,
    "description": "월간 전용 피드 + 감사 메시지",
    "status": "ACTIVE"
  }
]
```

### 1.12.3 플랜 수정

- Method: `PUT`
- URI: `/api/v1/subscriptionPlan/{memberId}`
- 가격은 바꾸지 않는다. 대상 플랜은 본문 `id`로 지정한다. 없으면 HTTP 404, 타인이면 HTTP 403.
- Request Body (`SubscriptionUpdateRequest`):

```json
{
  "id": 1,
  "memberId": 2,
  "planName": "프리미엄",
  "description": "전용 피드 + 월 1회 화상 만남",
  "status": "ACTIVE"
}
```

`status`: `ACTIVE`, `INACTIVE`, `DELETED`.

- Response Body: HTTP 200, 본문 없음

### 1.12.4 플랜 삭제

- Method: `DELETE`
- URI: `/api/v1/subscriptionPlan/{memberId}?id={planId}`
- Query: `id` (플랜 ID, 필수)
- Response Body: HTTP 200, 본문 없음

---

## 1.13 구독 결제

컨트롤러: `SubscriptionPaymentController` (`/api/v1/subscription`). `SubscriptionRecord`가 구독자가 가입한 **구독**이다. 첫 결제는 프론트가 받은 빌링키로 PortOne 빌링키 결제를 호출하고, 성공하면 구독 행을 만든다. 모두 인증이 필요하다.

### 1.13.1 구독 첫 결제

- Method: `POST`
- URI: `/api/v1/subscription/{memberId}`
- 경로 `memberId`는 플랜 소유자(구독 대상)다. 로그인 회원은 구독자이며 본인 플랜은 구독할 수 없다.
- Request Body (`SubscriptionRecordCreateRequest`): `targetMemberId`는 경로 `memberId`, 플랜 소유 회원 ID와 같아야 한다.

```json
{
  "targetMemberId": 2,
  "planId": 1,
  "billingKey": "billing-key-1"
}
```

- 성공: HTTP 200, 본문 없음
- 본인 플랜, 대상 불일치, 중복 구독, 유효하지 않은 빌링키, 결제 실패: HTTP 400
- 플랜이 없거나 삭제됨: HTTP 404

### 1.13.2 내 구독 목록

- Method: `GET`
- URI: `/api/v1/subscription/{memberId}`
- 경로 `memberId`는 로그인 회원(구독자)과 같아야 한다. 타인이면 HTTP 403.
- Response Body (HTTP 200, `List<MySubscriptionsResponse>`). 활성 구독만 포함한다.

```json
[
  {
    "id": 1,
    "memberId": 4,
    "targetMember": "코코",
    "planName": "베이직",
    "startedAt": "2026-10-01",
    "nextBillingAt": "2026-11-01",
    "agreement": true
  }
]
```

### 1.13.3 내 구독 단건

- Method: `GET`
- URI: `/api/v1/subscription/{memberId}/{subscriptionRecordId}`
- 경로 `memberId`는 로그인 회원, 구독의 구독자 ID와 같아야 한다. 해지된 구독은 HTTP 404.
- Response Body (HTTP 200): 1.13.2 항목과 같은 `MySubscriptionsResponse`

### 1.13.4 다음 달 유지 동의 변경

- Method: `PATCH`
- URI: `/api/v1/subscription/{memberId}/{subscriptionRecordId}`
- 본문 `id`는 경로의 구독 ID와 같아야 한다. 불일치하거나 타인이면 HTTP 403.
- Request Body (`SubscriptionRecordUpdateRequest`):

```json
{
  "id": 1,
  "agreement": false
}
```

- Response Body: HTTP 200, 본문 없음

### 1.13.5 구독 해지

- Method: `DELETE`
- URI: `/api/v1/subscription/{memberId}/{subscriptionRecordId}`
- 행을 지우지 않고 상태를 `CANCELLED`로 바꾼다. 이미 해지됐으면 HTTP 404.
- Response Body: HTTP 200, 본문 없음

---

## 1.14 관리자 회원

컨트롤러: `AdminMemberRestController` (`/api/v1/admin/members`). 인증이 필요하다. `SecurityConfig`와 `AdminMemberServiceImpl`에는 역할(`ROLE_ADMIN`) 검사가 없다.

### 1.14.1 회원 목록

- Method: `GET`
- URI: `/api/v1/admin/members`
- Response Body (HTTP 200, `List<AdminMemberResponse>`):

```json
[
  {
    "id": 1,
    "email": "mungchi@petory.com",
    "nickname": "뭉치",
    "species": "개",
    "sex": "수",
    "birthDate": "2020-05-01",
    "profileImage": "https://example.com/me.png",
    "address": "서울시 강남구",
    "status": "ACTIVE",
    "role": "ROLE_USER",
    "createdAt": "2026-08-01T09:15:00"
  }
]
```

### 1.14.2 계정 정지

- Method: `PATCH`
- URI: `/api/v1/admin/members/{memberId}/block`
- 회원 `status`를 `BLOCKED`로 바꾼다.
- 성공: HTTP 204 No Content
- 대상이 없으면 `IllegalArgumentException`으로 HTTP 400, `BUSINESS_RULE_VIOLATION`
