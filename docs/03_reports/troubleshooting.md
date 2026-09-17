# 1. 트러블슈팅 및 기술적 의사결정 보고서 (사자그램 SNS)

## 목차
- [1. 트러블슈팅 및 기술적 의사결정 보고서 (사자그램 SNS)](#1-트러블슈팅-및-기술적-의사결정-보고서-사자그램-sns)
- [1.1 인증 보안: Refresh Token Rotation (RTR) 및 재발급 동시성](#11-인증-보안-refresh-token-rotation-rtr-및-재발급-동시성)
- [1.2 미디어 저장소: AWS S3 이미지 업로드 및 CloudFront CORS](#12-미디어-저장소-aws-s3-이미지-업로드-및-cloudfront-cors)
- [1.3 데이터베이스: 해시태그 1NF 정규화 및 MyBatis N+1 문제 해결](#13-데이터베이스-해시태그-1nf-정규화-및-mybatis-n1-문제-해결)
- [1.4 결제 신뢰성: 결제 금액 사후 검증 및 망 취소 처리](#14-결제-신뢰성-결제-금액-사후-검증-및-망-취소-처리)
- [1.5 상호작용 동시성: 좋아요 토글 중복 요청 및 카운트 정합성 보장](#15-상호작용-동시성-좋아요-토글-중복-요청-및-카운트-정합성-보장)

---

## 1.1 인증 보안: Refresh Token Rotation (RTR) 및 재발급 동시성

### 1.1.1 문제 상황
- Access Token의 유효기간(1시간) 만료 시 프론트엔드가 `/api/v1/auth/refresh`를 호출하여 새 토큰을 받아오는 구조에서, 페이지 로딩 시 여러 개의 비동기 API 요청이 거의 동시에 401 에러를 수신함.
- 브라우저가 짧은 순간 여러 번의 토큰 갱신 요청을 전송하면서, 서버에서 이미 폐기된 구버전 Refresh Token으로 두 번째 요청이 유입되어 인증이 강제 종료되는 문제 발생.

### 1.1.2 원인 분석
- RTR(Refresh Token Rotation) 전략에 따라 한 번 사용된 Refresh Token은 즉시 무효화되고 새 토큰 쌍이 발급되는데, 첫 번째 갱신 요청이 처리되는 동안 날아온 병렬 요청들이 구 토큰을 전송하여 유효성 검증에 실패함.

### 1.1.3 해결 방안
- 프론트엔드(React/axios) 인터셉터에 토큰 갱신 락(Promise 체이닝) 적용:
  - 401 발생 시 최초 1개의 요청만 갱신 API를 호출하고, 갱신이 진행되는 동안 유입된 다른 요청들은 대기 큐에 보관.
  - 새 Access Token이 발급되면 대기 중이던 모든 요청의 헤더를 새 토큰으로 일괄 교체 후 재시도하도록 구성.
- 백엔드 Redis 또는 DB에 재발급 유예 시간(Grace Period, 약 10~30초)을 두어 네트워크 지연으로 인한 동시 요청을 정상 수용하도록 처리.

---

## 1.2 미디어 저장소: AWS S3 이미지 업로드 및 CloudFront CORS

### 1.2.1 문제 상황
- React 프론트엔드에서 S3에 업로드된 이미지를 `<canvas>`나 프로필 이미지로 로드할 때 `No 'Access-Control-Allow-Origin' header is present on the requested resource` CORS 에러 발생.
- 한글 및 공백이 포함된 파일명(예: `내 사진 1.png`)을 S3에 직접 업로드했을 때 다운로드 URL이 깨지거나 접근 불가 403 발생.

### 1.2.2 원인 분석
- S3 버킷의 CORS 설정에 프론트엔드 도메인이 등록되지 않았거나 CloudFront 캐시에서 Origin 헤더를 무시하여 캐싱된 응답 반환.
- 원본 파일명을 그대로 객체 키로 사용할 경우 URL 인코딩 불일치 및 중복 덮어쓰기 위험 발생.

### 1.2.3 해결 방안
- 파일명 UUID 난수화:
  - 업로드 시 원본 파일명을 버리고 `UUID.randomUUID().toString() + "_" + ext` 형태로 저장하여 URL 특수문자 충돌 및 중복 원천 차단.
- S3 버킷 CORS 정책 설정 및 CloudFront Cache Policy에서 `Origin`, `Access-Control-Request-Headers`를 캐시 키에 포함하도록 Forwarding 설정.

---

## 1.3 데이터베이스: 해시태그 1NF 정규화 및 MyBatis N+1 문제 해결

### 1.3.1 문제 상황
- 피드 목록 10개를 조회할 때, 각 피드에 연결된 해시태그 목록(0~N개)을 가져오기 위해 피드 1건당 해시태그 SELECT 쿼리가 1번씩 추가 실행되어 총 11번의 쿼리가 발생하는 N+1 문제 발생.

### 1.3.2 원인 분석
- `schema.sql`에서 제1정규형(1NF)을 만족하기 위해 `post_hashtag` 테이블을 분리하였으나, MyBatis 매퍼에서 피드 목록 조회 후 루프를 돌며 해시태그를 건건이 조회함.

### 1.3.3 해결 방안
- MyBatis `LEFT OUTER JOIN`과 `<resultMap>`의 `<collection>` 태그를 활용한 단일 쿼리 매핑:
```xml
<select id="selectPostListWithHashtags" resultMap="PostResponseMap">
    SELECT p.id, p.content, p.image_url, p.like_count, p.created_at,
           m.nickname, m.profile_image,
           h.id AS tag_id, h.tag_name
    FROM post p
    INNER JOIN member m ON p.member_id = m.id
    LEFT OUTER JOIN post_hashtag h ON p.id = h.post_id
    ORDER BY p.id DESC
    LIMIT #{limit} OFFSET #{offset}
</select>
```
- 단 1번의 조인 쿼리로 피드 기본 정보와 해시태그 리스트를 한 번에 DTO 컬렉션으로 조립하여 DB I/O 부하 90% 이상 절감.

---

## 1.4 결제 신뢰성: 결제 금액 사후 검증 및 망 취소 처리

### 1.4.1 문제 상황
- 클라이언트 브라우저의 개발자 도구를 조작하여 9,900원짜리 VIP 구독 결제 금액을 100원으로 변조한 뒤 결제를 시도할 수 있는 위변조 취약점 존재.
- 외부 PG사 결제는 승인되었으나 백엔드 서버 DB에 구독 정보를 기록하는 트랜잭션 도중 예외가 발생하여 사용자는 돈이 빠져나갔는데 VIP 등급은 올라가지 않는 결제 불일치 발생.

### 1.4.2 원인 분석
- 결제 완료 콜백(imp_uid)만 믿고 서버에서 실제 결제된 금액을 PG사 본서버와 교차 검증하지 않으면 금액 위변조를 막을 수 없음.
- 결제 승인과 로컬 DB 트랜잭션이 분리된 2-Phase 상황에서 롤백 발생 시 외부 결제 취소 호출이 누락됨.

### 1.4.3 해결 방안
- 2단계 사전/사후 검증 체계 도입:
  - 1단계: 결제 시작 전 서버 DB에 주문번호(merchant_uid)와 상품 정가(9,900원)를 `payment` 테이블에 READY 상태로 기록.
  - 2단계: 결제 완료 후 PG사 결제 검증 REST API를 서버 대 서버로 직접 호출하여 PG사에 기록된 `amount`와 우리 DB의 `amount`가 1원이라도 다르면 즉시 PG사 전액 결제 취소 API를 실행하고 결제를 무효화.
- 망 취소(Compensating Transaction) 보장:
  - DB 등록 실패 시 `catch` 블록에서 PG사 자동 결제 취소 API를 호출하도록 롤백 보상 트랜잭션 작성.
  - 비동기 유실 방지를 위해 결제 웹훅(Webhook) 엔드포인트를 열어두어 최종 상태 정합성 보장.

---

## 1.5 상호작용 동시성: 좋아요 토글 중복 요청 및 카운트 정합성 보장

### 1.5.1 문제 상황
- 사용자가 네트워크가 느린 환경에서 좋아요 하트 버튼을 빠르게 연타할 때, 여러 개의 토글 요청이 동시 처리되면서 `Duplicate entry for key 'uk_member_post_like'` 데이터베이스 에러가 발생하거나 `post` 테이블의 `like_count`가 음수가 되는 데이터 왜곡 발생.

### 1.5.2 원인 분석
- 좋아요 등록(INSERT)과 카운트 증가(UPDATE)가 트랜잭션 격리 수준에 따라 동시성 레이스 컨디션(Race Condition)을 유발함.

### 1.5.3 해결 방안
- DB 레벨의 복합 유니크 인덱스(`uk_member_post_like(member_id, post_id)`)를 통해 데이터 무결성 1차 방어.
- 백엔드 좋아요 토글 로직을 단일 원자적 쿼리 또는 비관적 락으로 제어:
  - `INSERT IGNORE` 또는 MyBatis 조건부 쿼리를 활용하여 중복 INSERT 예외 발생 시 안전하게 무시.
  - 카운트 갱신 시 `like_count = like_count + 1` 및 `GREATEST(like_count - 1, 0)` 수식을 적용하여 음수 발생 원천 방지.
- 프론트엔드 레벨에서 하트 버튼 클릭 시 디바운스(Debounce) 또는 요청 완료 전 버튼 비활성화(Disabled) 처리.
