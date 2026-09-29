# 해시태그 검색 기능 스터디

## 목차
- [1. 기능 개요](#1-기능-개요)
- [2. 해시태그 저장 방식](#2-해시태그-저장-방식)
- [3. 검색 조건: 태그 단위로 정확히 찾기](#3-검색-조건-태그-단위로-정확히-찾기)
- [4. 이미지 JOIN과 LIMIT 문제](#4-이미지-join과-limit-문제)
- [5. 커서 기반 무한 스크롤](#5-커서-기반-무한-스크롤)
- [6. 서비스 로직: 검색어 정리와 검증](#6-서비스-로직-검색어-정리와-검증)
- [7. URL에 # 을 넣으면 안 되는 이유](#7-url에--을-넣으면-안-되는-이유)
- [8. 테스트](#8-테스트)
- [9. 한계와 개선 방향](#9-한계와-개선-방향)
- [10. 확인 문제](#10-확인-문제)

---

## 1. 기능 개요

해시태그 하나를 입력하면, 그 해시태그가 달린 일반 게시글(`type = 1`)을 최신순으로 보여준다.
메인 피드와 똑같이 커서 기반 무한 스크롤로 응답한다.

| 항목 | 내용 |
| --- | --- |
| URL | `GET /api/v1/posts/search` |
| 파라미터 | `hashtag` (필수), `lastPostId` (선택), `size` (기본 10) |
| 응답 | `SliceResponse<PostListResponse>` (메인 피드와 동일) |
| 인증 | 필요 없음 (`GET /api/v1/posts/**` 는 `permitAll`) |

요청 예시

```
GET /api/v1/posts/search?hashtag=강아지&size=10
GET /api/v1/posts/search?hashtag=강아지&lastPostId=25&size=10   (다음 페이지)
```

응답 예시

```json
{
  "content": [
    {
      "id": 31,
      "content": "오늘 한강 공원 산책했어요!",
      "hashtags": "#강아지 #산책 #한강",
      "nickname": "멍멍이",
      "imageUrls": ["https://.../1.jpg", "https://.../2.jpg"],
      "likeCount": 3,
      "commentCount": 1
    }
  ],
  "hasNext": true,
  "lastPostId": 31
}
```

코드 흐름

```
PostController.searchPostsByHashtag
  -> PostServiceImpl.searchPostsByHashtag   (검색어 정리, 검증, hasNext 계산)
    -> PostMapper.selectPostListByHashtag   (PostMapper.xml 의 SQL 실행)
```

---

## 2. 해시태그 저장 방식

해시태그는 별도 테이블 없이 `post_main.hashtags` 컬럼 하나에 문자열로 저장된다.

```sql
hashtags TEXT NULL
```

```
'#강아지 #산책 #한강'
'#고양이 #낮잠 #일상'
```

- 태그마다 앞에 `#` 이 붙는다.
- 태그끼리는 공백 한 칸으로 구분한다.
- 해시태그가 없는 글은 `NULL` 이다.

이 형식이 지켜진다는 전제로 검색 쿼리가 만들어져 있다.
게시글 작성/수정 시 프론트가 이 형식대로 보내줘야 검색이 제대로 동작한다.

---

## 3. 검색 조건: 태그 단위로 정확히 찾기

### 3.1 처음 떠올리기 쉬운 방법

```sql
WHERE hashtags LIKE '%강아지%'
```

`강아지` 라는 글자가 어디에든 들어 있으면 다 걸린다.

| hashtags | 결과 | 원하는 결과 |
| --- | --- | --- |
| `#강아지 #산책` | 나옴 | 나와야 함 |
| `#강아지간식` | 나옴 | 나오면 안 됨 |
| `#큰강아지` | 나옴 | 나오면 안 됨 |

### 3.2 앞에 # 을 붙이면

```sql
WHERE hashtags LIKE '%#강아지%'
```

`#큰강아지` 는 `#` 바로 뒤가 `큰` 이라서 이제 안 걸린다.
하지만 `#강아지간식` 은 여전히 걸린다. 뒤쪽 경계가 없기 때문이다.

### 3.3 뒤에 공백까지 붙이면

태그 뒤에는 항상 공백이 온다. 단, **마지막 태그 뒤에는 공백이 없다.**
그래서 컬럼 값 끝에 공백을 하나 붙여서 모든 태그 뒤에 공백이 오게 만든다.

```sql
WHERE CONCAT(hashtags, ' ') LIKE CONCAT('%#', #{hashtag}, ' %')
```

`#{hashtag}` 가 `강아지` 라면 실제로는 이렇게 비교된다.

```
CONCAT(hashtags, ' ')  LIKE  '%#강아지 %'
```

| hashtags | 공백 붙인 값 | `%#강아지 %` 일치? |
| --- | --- | --- |
| `#강아지 #산책` | `#강아지 #산책 ` | O |
| `#산책 #강아지` | `#산책 #강아지 ` | O (마지막 태그도 찾음) |
| `#강아지간식` | `#강아지간식 ` | X |
| `#큰강아지` | `#큰강아지 ` | X |
| `NULL` | `NULL` | X (NULL 과 CONCAT 하면 NULL) |

정리하면 `#` 이 앞쪽 경계, 공백이 뒤쪽 경계 역할을 해서 태그 하나를 정확히 집어낸다.

### 3.4 MyBatis 에서 `'%${hashtag}%'` 를 쓰면 안 되는 이유

```xml
<!-- 절대 금지 -->
WHERE hashtags LIKE '%${hashtag}%'

<!-- 올바른 방법 -->
WHERE hashtags LIKE CONCAT('%', #{hashtag}, '%')
```

- `${}` 는 값을 SQL 문자열에 그대로 이어 붙인다. 사용자가 `' OR '1'='1` 같은 값을 보내면 SQL 인젝션이 된다.
- `#{}` 는 PreparedStatement 의 `?` 로 바인딩되어 안전하다.
- `#{}` 는 따옴표 안에 넣을 수 없으므로 `CONCAT` 으로 `%` 를 붙인다.

---

## 4. 이미지 JOIN과 LIMIT 문제

### 4.1 문제 상황

게시글 1개에 이미지가 3장이면, `LEFT JOIN post_image` 결과는 3행이 된다.

```
p.id | image_url
-----+----------
 31  | 1.jpg
 31  | 2.jpg
 31  | 3.jpg
 30  | a.jpg
 29  | NULL
```

여기에 `LIMIT 3` 을 걸면 **행 3개**를 가져온다. 게시글 3개가 아니다.
MyBatis `<collection>` 이 같은 id 끼리 묶으면 결과는 게시글 **1개**(31번)뿐이다.

- 한 페이지에 10개를 요청했는데 몇 개밖에 안 오는 문제가 생긴다.
- `size + 1` 개로 다음 페이지 여부를 판단하는 로직(5장)도 틀어진다.

### 4.2 해결: 게시글을 먼저 자르고 이미지를 붙인다

```sql
FROM (
    SELECT *
    FROM post_main
    WHERE type = 1
    AND CONCAT(hashtags, ' ') LIKE CONCAT('%#', #{hashtag}, ' %')
    AND id < #{lastPostId}          -- lastPostId 가 있을 때만
    ORDER BY id DESC
    LIMIT #{limit}                  -- 여기서 "게시글" 기준으로 자름
) p
INNER JOIN member m ON p.member_id = m.id
LEFT JOIN post_image img ON p.id = img.post_id
ORDER BY p.id DESC, img.sort_order ASC
```

1. 괄호 안(서브쿼리)에서 조건에 맞는 게시글을 `limit` 개만 뽑는다.
2. 그 결과를 `p` 라는 이름의 임시 테이블처럼 쓴다.
3. 여기에 작성자, 이미지를 JOIN 한다. 이미지 때문에 행이 늘어나도 게시글 수는 이미 정해져 있다.

> 참고: 기존 메인 피드 쿼리(`selectPostListCursor`)는 JOIN 뒤에 LIMIT 을 걸고 있어서 같은 문제가 있다.

### 4.3 resultMap 의 `<collection>`

```xml
<resultMap id="PostListResultMap" type="...PostListResponse">
    <id property="id" column="id"/>
    ...
    <collection property="imageUrls" ofType="java.lang.String">
        <result column="image_url"/>
    </collection>
</resultMap>
```

- `<id>` 값이 같은 행들을 하나의 `PostListResponse` 로 합친다.
- 각 행의 `image_url` 을 `imageUrls` 리스트에 차례로 넣는다.
- 그래서 쿼리 결과가 5행이어도 Java 에서는 게시글 3개가 된다.

---

## 5. 커서 기반 무한 스크롤

### 5.1 OFFSET 방식과 비교

| | OFFSET 방식 | 커서 방식 |
| --- | --- | --- |
| 요청 | `?page=3&size=10` | `?lastPostId=25&size=10` |
| SQL | `LIMIT 10 OFFSET 20` | `WHERE id < 25 ORDER BY id DESC LIMIT 11` |
| 뒤 페이지 성능 | 앞의 행을 다 읽고 버리므로 느려짐 | PK 로 바로 찾아서 일정함 |
| 새 글이 올라오면 | 한 칸씩 밀려서 중복 노출 | 기준 id 가 고정이라 중복 없음 |

SNS 피드처럼 계속 새 글이 올라오고 아래로 스크롤하는 화면에는 커서 방식이 맞다.

### 5.2 size + 1 로 다음 페이지 확인하기

```java
int limit = size + 1;
List<PostListResponse> posts = postMapper.selectPostListByHashtag(keyword, lastPostId, limit);

boolean hasNext = false;
if (posts.size() > size) {
    hasNext = true;
    posts.remove(size);     // 확인용으로 더 가져온 1개는 버린다
}

Long nextCursorId = posts.isEmpty() ? null : posts.get(posts.size() - 1).getId();
```

- 10개가 필요하면 11개를 요청한다.
- 11개가 오면 다음 페이지가 있다는 뜻이다. 마지막 1개는 버리고 10개만 응답한다.
- 10개 이하가 오면 마지막 페이지다.
- COUNT 쿼리를 따로 날리지 않아도 되는 것이 장점이다.

### 5.3 프론트 사용 흐름

```
1) GET /search?hashtag=강아지&size=10
   -> content 10개, hasNext=true, lastPostId=41

2) GET /search?hashtag=강아지&lastPostId=41&size=10
   -> content 10개, hasNext=true, lastPostId=22

3) GET /search?hashtag=강아지&lastPostId=22&size=10
   -> content 4개, hasNext=false  (더 이상 요청하지 않음)
```

---

## 6. 서비스 로직: 검색어 정리와 검증

```java
// 1. 비어 있으면 거절
if (hashtag == null || hashtag.isBlank()) {
    throw new IllegalArgumentException("검색할 해시태그를 입력해주세요.");
}

// 2. 앞뒤 공백 제거, 맨 앞 # 제거
String keyword = hashtag.trim();
if (keyword.startsWith("#")) {
    keyword = keyword.substring(1);
}

// 3. '#' 만 입력했거나 태그를 여러 개 넣으면 거절
if (keyword.isEmpty()) {
    throw new IllegalArgumentException("검색할 해시태그를 입력해주세요.");
}
if (keyword.contains(" ") || keyword.contains("#")) {
    throw new IllegalArgumentException("해시태그는 한 개만 검색할 수 있습니다.");
}
```

| 입력 | 정리 후 | 결과 |
| --- | --- | --- |
| `강아지` | `강아지` | 검색 |
| `#강아지` | `강아지` | 검색 |
| `  #강아지 ` | `강아지` | 검색 |
| `null`, `""`, `"   "` | - | 400 |
| `#` | `""` | 400 |
| `#강아지 #산책` | `강아지 #산책` | 400 (여러 개) |
| `강아지#산책` | `강아지#산책` | 400 (여러 개) |

왜 여러 개를 막는가?
쿼리가 `'%#검색어 %'` 형태라서, `강아지 #산책` 이 들어오면 `'%#강아지 #산책 %'` 가 된다.
이러면 "두 태그가 이 순서로 붙어 있는 글"만 찾게 되어 사용자가 기대하는 결과와 다르다.

`IllegalArgumentException` 은 `GlobalRestExceptionHandler` 가 400 Bad Request 로 바꿔서 응답한다.

---

## 7. URL에 # 을 넣으면 안 되는 이유

URL 에서 `#` 은 **프래그먼트(fragment)** 시작 기호다. 브라우저는 `#` 뒤를 서버로 보내지 않는다.

```
/api/v1/posts/search?hashtag=#강아지
                             ^ 여기부터 서버에 안 감 -> hashtag 값이 빈 문자열
```

해결 방법은 둘 중 하나다.

- `#` 을 빼고 보낸다: `?hashtag=강아지` (권장)
- `%23` 으로 인코딩해서 보낸다: `?hashtag=%23강아지`

프론트에서 axios 를 쓴다면 `params` 옵션을 쓰면 자동으로 인코딩된다.

```javascript
axios.get('/api/v1/posts/search', { params: { hashtag: '#강아지', size: 10 } });
// -> /api/v1/posts/search?hashtag=%23%EA%B0%95%EC%95%84%EC%A7%80&size=10
```

서버는 `#` 이 있든 없든 떼어내고 검색하므로 둘 다 같은 결과가 나온다.

---

## 8. 테스트

### 8.1 Mapper 테스트 (`PostMapperTest`) - 실제 DB 필요

| 테스트 | 확인 내용 |
| --- | --- |
| 정확 일치 | `#검색테스트 #산책`, `#산책 #검색테스트` 는 나오고 `#검색테스트간식`, `#큰검색테스트` 는 안 나온다 |
| 커서 | 가장 최신 글 id 를 `lastPostId` 로 주면 그보다 오래된 글만 최신순으로 나온다 |
| LIMIT | 3개 저장 후 limit 2 로 조회하면 2개만 나온다 |
| 결과 없음 | 없는 태그는 빈 리스트 |

- `@SpringBootTest` + `@Transactional` 이라 테스트가 끝나면 저장한 데이터는 롤백된다.
- `data.sql` 의 기존 데이터와 겹치지 않도록 `#검색테스트`, `#커서테스트` 처럼 테스트 전용 태그를 쓴다.
- MySQL 이 떠 있어야 한다: `docker compose up -d mysql`

### 8.2 Service 테스트 (`PostServiceTest`) - DB 필요 없음

| 테스트 | 확인 내용 |
| --- | --- |
| 검색어 정리 | `"  #강아지 "` 를 넣으면 Mapper 에 `"강아지"`, `limit = 11` 로 전달된다 |
| hasNext | size 2 에 3개가 오면 2개만 응답, `hasNext = true`, `lastPostId` 는 2번째 글 id |
| 결과 없음 | 빈 리스트, `hasNext = false`, `lastPostId = null` |
| 빈 검색어 | `null`, 공백, `#` 은 예외 |
| 여러 태그 | `#강아지 #산책`, `강아지#산책` 은 예외 |

- Mockito 로 `PostMapper` 를 가짜로 만들어서 서비스 로직만 검증한다.

```powershell
.\gradlew.bat test --tests "*PostServiceTest" --tests "*PostMapperTest"
```

---

## 9. 한계와 개선 방향

### 9.1 인덱스를 못 탄다

`LIKE '%...'` 처럼 앞에 `%` 가 붙으면 B-Tree 인덱스를 쓸 수 없다.
게시글 전체를 하나씩 읽으면서 문자열을 비교한다(Full Table Scan).

- 게시글이 수천 건 수준이면 체감 차이가 거의 없다.
- 수십만 건 이상이 되면 느려진다.

`EXPLAIN` 으로 직접 확인해보기

```sql
EXPLAIN
SELECT * FROM post_main
WHERE CONCAT(hashtags, ' ') LIKE '%#강아지 %';
-- type 컬럼이 ALL 이면 풀 스캔
```

### 9.2 LIKE 와일드카드 문자

검색어에 `_` 나 `%` 가 들어가면 LIKE 의 특수문자로 해석된다.

- `_` 는 아무 글자 1개와 일치한다. `#my_dog` 를 검색하면 `#myxdog` 도 나올 수 있다.
- 필요해지면 서비스에서 `\_`, `\%` 로 escape 처리를 추가한다.

### 9.3 태그 형식에 의존한다

`#강아지,#산책` 처럼 쉼표로 저장되거나 공백이 두 칸이면 검색이 틀어진다.
작성/수정 시 서버에서 형식을 한 번 정리해서 저장하면 더 안전하다.

### 9.4 근본적인 개선: 해시태그 테이블 분리 (정규화)

```sql
CREATE TABLE hashtag (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_hashtag_name (name)
);

CREATE TABLE post_hashtag (
    post_id    BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, hashtag_id),
    FOREIGN KEY (post_id) REFERENCES post_main (id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtag (id)
);
```

검색 쿼리

```sql
SELECT p.*
FROM hashtag h
INNER JOIN post_hashtag ph ON ph.hashtag_id = h.id
INNER JOIN post_main p ON p.id = ph.post_id
WHERE h.name = '강아지'          -- 정확히 일치, 인덱스 사용
ORDER BY p.id DESC
LIMIT 11;
```

| | 지금 (문자열 컬럼) | 테이블 분리 |
| --- | --- | --- |
| 구현 난이도 | 쉬움 | 작성/수정/삭제 시 태그 테이블도 같이 관리해야 함 |
| 검색 성능 | 풀 스캔 | 인덱스 사용 |
| 인기 태그 집계 | 어려움 | `GROUP BY hashtag_id` 로 쉬움 |
| 태그 자동완성 | 어려움 | `WHERE name LIKE '강%'` (앞부분 검색은 인덱스 사용) |

---

## 10. 확인 문제

1. `hashtags LIKE '%#강아지%'` 로 검색하면 어떤 게시글이 잘못 포함되는가?
2. `CONCAT(hashtags, ' ')` 에서 끝에 공백을 붙이지 않으면 어떤 게시글을 놓치는가?
3. `hashtags` 가 `NULL` 인 게시글은 왜 검색 결과에 나오지 않는가?
4. 이미지 JOIN 뒤에 `LIMIT 11` 을 걸면 어떤 문제가 생기는가? 서브쿼리는 이를 어떻게 해결하는가?
5. `size + 1` 개를 조회하는 이유는 무엇인가? COUNT 쿼리와 비교하면 장점은?
6. `?hashtag=#강아지` 로 요청하면 서버는 어떤 값을 받는가?
7. MyBatis 에서 `${}` 대신 `#{}` 를 써야 하는 이유는?
8. 게시글이 100만 건이 되면 지금 방식의 어떤 점이 문제가 되고, 어떻게 개선할 수 있는가?

<details>
<summary>정답 보기</summary>

1. `#강아지간식` 처럼 검색어로 **시작하는** 더 긴 태그가 포함된다. (`#큰강아지` 는 `#` 뒤가 `큰` 이라 제외된다.)
2. 마지막 태그. `#산책 #강아지` 에서 `#강아지` 뒤에는 공백이 없어서 `'%#강아지 %'` 와 일치하지 않는다.
3. `CONCAT` 은 인자 중 하나라도 `NULL` 이면 결과가 `NULL` 이고, `NULL LIKE ...` 는 참이 아니기 때문이다.
4. 이미지 수만큼 행이 늘어나 게시글이 요청한 개수보다 적게 온다. 서브쿼리에서 게시글만으로 먼저 LIMIT 을 걸고, 그 뒤에 이미지를 JOIN 하므로 게시글 수가 보장된다.
5. 1개가 더 오면 다음 페이지가 있다고 판단할 수 있다. 전체 개수를 세는 쿼리를 추가로 실행하지 않아도 된다.
6. `#` 뒤는 프래그먼트라서 서버로 전송되지 않는다. `hashtag` 는 빈 문자열이 되고 400 응답을 받는다.
7. `${}` 는 문자열을 그대로 붙여서 SQL 인젝션에 취약하다. `#{}` 는 PreparedStatement 바인딩이라 안전하다.
8. 앞에 `%` 가 붙은 LIKE 는 인덱스를 못 타서 풀 스캔이 된다. `hashtag`, `post_hashtag` 테이블로 분리하면 `name = ?` 조건으로 인덱스를 사용할 수 있다.

</details>
