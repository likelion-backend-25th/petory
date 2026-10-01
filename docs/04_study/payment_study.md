# 간식쏘기 결제 시스템 뜯어보기 (PortOne V2 단건 결제)

> 코드 기준: `feat-webhook/65` 이후 결제 리팩터링(도메인 분리, Dto 접미사 제거, record 전환) 반영 상태
> 공부 방법: 각 장을 읽고 → 코드 파일을 직접 열어 대조 → 장 끝의 질문에 먼저 답해 보고 → 질문 아래 "정답 보기"를 펼쳐 비교

---

## 목차

0. [큰 그림: 전체 흐름과 상태 변화](#0-큰-그림-전체-흐름과-상태-변화)
1. [테이블 두 개: payment, cancel_payment](#1-테이블-두-개-payment-cancel_payment)
2. [계층 구조: DTO와 도메인은 왜 나누나](#2-계층-구조-dto와-도메인은-왜-나누나)
3. [DTO 8개 뜯어보기](#3-dto-8개-뜯어보기)
4. [도메인 Payment](#4-도메인-payment)
5. [Mapper 인터페이스와 XML 기초](#5-mapper-인터페이스와-xml-기초)
6. [/prepare: 결제 사전 등록](#6-prepare-결제-사전-등록)
7. [/complete: 행 잠금과 본인 확인](#7-complete-행-잠금과-본인-확인)
8. [syncWithPortOne: 네 갈래 분기](#8-syncwithportone-네-갈래-분기)
9. [handleFailedPayment: 결제 실패](#9-handlefailedpayment-결제-실패)
10. [handleAmountMismatch: 위변조 자동 취소](#10-handleamountmismatch-위변조-자동-취소)
11. [PortOne API 호출](#11-portone-api-호출)
12. [웹훅](#12-웹훅)
13. [XML UPDATE의 두 번째 안전장치](#13-xml-update의-두-번째-안전장치)
14. [예외 → HTTP 상태 코드 정리](#14-예외--http-상태-코드-정리)
15. [테스트 코드 읽기](#15-테스트-코드-읽기)
16. [알려진 한계와 개선 과제](#16-알려진-한계와-개선-과제)

---

## 0. 큰 그림: 전체 흐름과 상태 변화

### 전체 흐름

```text
[사용자 브라우저]            [우리 서버]                [PortOne]
      │                         │                          │
 ①   │── POST /prepare ───────▶│ READY로 저장              │
      │◀── 주문번호(ORD_...) ───│                          │
      │                         │                          │
 ②   │── 결제창 (주문번호, 금액) ─────────────────────────▶│ 실제 결제
      │◀── 결제 결과 ──────────────────────────────────────│
      │                         │                          │
 ③   │── POST /complete ──────▶│── GET /payments/{id} ──▶│
      │                         │◀── 상태, 실제 금액 ───────│
      │◀── PAID / FAILED ... ───│ DB 확정                   │
      │                         │                          │
 ④   │                         │◀── POST /webhook ────────│ (③과 별개로 PortOne이 직접)
      │                         │── GET /payments/{id} ──▶│
      │                         │ DB 확정 (이미 했으면 건너뜀) │
```

- ①에서 서버는 **나중에 비교할 기준(예정 금액)** 을 DB에 박제해 둔다.
- ②는 브라우저와 PortOne 사이의 일이라 우리 서버는 모른다. 여기서 금액 조작이 일어날 수 있다.
- ③과 ④는 둘 다 "PortOne에 직접 물어보고 DB와 비교"한다. 같은 로직(`syncWithPortOne`)을 쓴다.
- ④가 있는 이유: 사용자가 결제 후 브라우저를 닫거나 네트워크가 끊겨서 ③이 안 오는 경우에도 DB를 확정하기 위해.

### 상태 변화

```text
          ┌──▶ PAID       (PortOne이 PAID + 금액 일치)
READY ────┼──▶ FAILED     (PortOne이 FAILED라고 함)
          └──▶ CANCELLED  (PortOne은 PAID인데 금액 불일치 → 우리가 자동 취소)
```

- READY만 다른 상태로 바뀔 수 있다. PAID, FAILED, CANCELLED는 **최종 상태**다.
- 이 규칙을 코드 두 군데에서 지킨다.
  - Java: `Payment.isReady()` 검사 (8장)
  - SQL: `UPDATE ... WHERE status = 'READY'` (13장)

### 질문

**Q0-1.** 사용자가 결제창에서 금액을 5000원에서 100원으로 조작해서 결제했다. 우리는 어떤 컬럼과 무엇을 비교해서 알아챌 수 있을까?

<details>
<summary>정답 보기</summary>

`/prepare` 때 서버가 `total_amount = 5000`을 DB에 저장해 둔다. `/complete`나 웹훅이 오면 서버가 PortOne에 직접 물어보고, PortOne은 "실제로 100원 결제됐다"고 답한다. `payment.total_amount`(5000)와 PortOne의 `amount.total`(100)이 다르니 위변조로 판단하고 자동 취소한다.

</details>

**Q0-2.** 이미 PAID인 결제에 웹훅이 또 오면 어떻게 처리될까?

<details>
<summary>정답 보기</summary>

`handleWebhook`이 `findByPaymentIdForUpdate`로 결제를 조회한다. 상태는 PAID다. `syncWithPortOne`의 첫 번째 검사(`!payment.isReady()`)에서 바로 반환한다. PortOne API는 호출하지 않는다.

</details>

**Q0-3.** ③ `/complete`만 있으면 충분할 것 같은데, ④ 웹훅은 왜 필요할까?

<details>
<summary>정답 보기</summary>

사용자가 결제 직후 브라우저를 닫거나, 네트워크가 끊기거나, 프론트 버그로 `/complete`가 안 오면 DB가 영원히 READY로 남는다. 웹훅은 PortOne 서버가 직접 보내니까 브라우저 상태와 무관하게 확정할 수 있다.

</details>

---

## 1. 테이블 두 개: payment, cancel_payment

파일: `src/main/resources/schema.sql`

### payment (결제 한 건 = 한 줄)

| 컬럼 | 타입 | NULL | 언제 채워지나 | 의미 |
|---|---|---|---|---|
| `id` | BIGINT PK | X | /prepare | 내부 숫자 PK (AUTO_INCREMENT) |
| `member_id` | BIGINT FK | X | /prepare | 결제자 (JWT에서) |
| `target_member_id` | BIGINT FK | X | /prepare | 후원 대상 |
| `payment_id` | VARCHAR(100) UNIQUE | X | /prepare | 주문번호 `ORD_...` = PortOne의 paymentId |
| `order_name` | VARCHAR(100) | X | /prepare | 주문명 |
| `currency` | VARCHAR(10) | X | /prepare | KRW |
| `total_amount` | INT | X | /prepare | **예정 금액 (비교 기준)** |
| `paid_amount` | INT | O | PAID 확정 시 | 실제 결제 금액 |
| `pay_method` | VARCHAR(30) | X | /prepare | 결제 수단 |
| `status` | VARCHAR(20) | X | 계속 바뀜 | READY / PAID / FAILED / CANCELLED |
| `transaction_id` | VARCHAR(100) | O | PAID 확정 시 | PortOne 거래 ID |
| `pg_tx_id` | VARCHAR(100) | O | PAID 확정 시 | PG사 거래 ID |
| `receipt_url` | VARCHAR(500) | O | PAID 확정 시 | 영수증 |
| `fail_code`, `fail_message` | VARCHAR | O | FAILED/CANCELLED 시 | 실패 사유 |
| `cancel_amount`, `cancel_reason` | INT, VARCHAR | O | CANCELLED 시 | 취소 금액, 사유 |
| `created_at` | DATETIME | X | /prepare | 생성 시각 |
| `paid_at` | DATETIME | O | PAID 확정 시 | 결제 시각 (한국 시간) |
| `cancelled_at` | DATETIME | O | CANCELLED 시 | 취소 시각 |

**포인트**
- NOT NULL인 컬럼은 전부 `/prepare` 시점에 이미 아는 값이다. 결제가 끝나야 아는 값은 전부 NULL 허용이다.
- `UNIQUE KEY uk_payment_payment_id (payment_id)`: 주문번호 중복 방지 + `WHERE payment_id = ?` 조회를 빠르게 + `FOR UPDATE`가 딱 한 줄만 잠그게 해 준다 (7장).
- `ON DELETE CASCADE`: 회원이 삭제되면 그 회원의 결제도 같이 삭제된다. (실서비스라면 결제 기록은 법적으로 보관해야 해서 CASCADE를 쓰지 않는 경우가 많다. 16장)

### cancel_payment (취소 이력)

| 컬럼 | 의미 |
|---|---|
| `payment_id` BIGINT FK | **주문번호 문자열이 아니라 `payment.id`(숫자 PK)** |
| `cancellation_id` UNIQUE | PortOne 취소 ID |
| `pg_cancellation_id` | PG사 취소 ID |
| `status` | 취소 상태 (SUCCEEDED 등) |
| `cancel_amount`, `reason` | 얼마, 왜 |
| `receipt_url` | 취소 영수증 |
| `requested_at`, `cancelled_at` | 요청 시각, 완료 시각 |

**포인트**
- 이름은 `payment_id`인데 값은 `payment.id`다. 이름이 헷갈리니 코드에서는 `paymentPk`라는 파라미터 이름으로 구분한다 (10장).
- 한 결제에 부분 취소가 여러 번 있을 수 있어서 별도 테이블(1:N)로 뺐다. 지금 코드는 전액 자동 취소만 한다.

### 질문

**Q1-1.** `total_amount`는 NOT NULL, `paid_amount`는 NULL 허용인 이유는?

<details>
<summary>정답 보기</summary>

`total_amount`는 `/prepare` 시점에 이미 알고, `paid_amount`는 결제가 끝나야 알 수 있기 때문이다. FAILED면 끝까지 null이다.

</details>

**Q1-2.** `cancel_payment.payment_id`에 주문번호(`ORD_...`)가 아니라 숫자 PK를 넣는 이유는?

<details>
<summary>정답 보기</summary>

숫자 PK가 JOIN과 FK에 더 작고 빠르다. 그리고 FK(`REFERENCES payment(id)`)로 묶어서, 없는 결제에 대한 취소 이력이 들어갈 수 없게 DB가 보장한다.

</details>

---

## 2. 계층 구조: DTO와 도메인은 왜 나누나

### 레이어 그림

```text
프론트엔드 ⇄ [요청/응답 DTO] ⇄ Controller ⇄ Service ⇄ [Payment 도메인] ⇄ Mapper ⇄ DB
                                              ⇅
                                       [PortOne DTO] ⇄ PortOne 서버
```

### DTO

- **백엔드 바깥 전체**와의 경계다. 프론트엔드뿐 아니라 PortOne 같은 외부 서버도 포함한다.
- 기준은 "상대가 우리 백엔드 밖에 있느냐"다.
- 모양을 **상대가** 정한다. 프론트와 약속한 API 스펙, PortOne API 문서.
- 한 번 쓰고 버린다.

### 도메인

- 서비스 안에서 비즈니스 로직이 다루는 **중심 객체**다.
- DB와 오가는 건 역할의 일부이고, `isReady()` 같은 규칙을 가지고 서비스 로직의 주인공이 되는 게 핵심이다.
- 모양을 **우리가** 정한다 (`payment` 테이블).

### 예전 코드의 문제 (Payment 도메인이 생긴 이유)

```text
[예전]
DB에 넣을 때 (INSERT)   → PaymentRequestDto   (필드 8개)
DB에서 꺼낼 때 (SELECT) → PaymentResponseDto  (필드 20개)

[지금]
DB에 넣을 때 / 꺼낼 때 → Payment (필드 20개, 테이블 컬럼과 1:1)
```

- 같은 테이블 한 줄인데 클래스가 두 개였다.
- 이름은 DTO인데 실제로는 프론트와 주고받지 않고 DB하고만 오갔다.
- 진짜 프론트용 DTO인 `PaymentPrepareRequestDto`와 이름이 섞여서 헷갈렸다.
- `"READY".equals(dto.getStatus())` 같은 규칙을 둘 곳이 없었다.

### 현실의 예외

- 우리 팀 `PostDetailResponse`, `CommentResponse`처럼 **여러 테이블을 조인해서 화면용으로 바로 뽑는 조회 전용 쿼리**는 DTO로 바로 받기도 한다.
- 도메인을 거쳐 다시 DTO로 바꾸는 게 번거롭기만 할 때 쓰는 실용적인 선택이다.
- 반대로 결제처럼 **상태가 바뀌고 규칙이 있는 데이터**는 도메인으로 다루는 게 맞다.

### record

- DTO 8개는 전부 Java `record`다. `Payment`는 `class`다.
- record가 자동으로 만들어 주는 것: 생성자, 값 꺼내는 메서드, `equals`/`hashCode`/`toString`.
- 값 꺼내는 메서드 이름: `getTotalAmount()`가 아니라 `totalAmount()`.
- 필드 수가 줄어드는 게 아니라 **코드 줄 수**가 줄어든다. 필드는 상대(프론트/PortOne)와의 약속이라 그대로다.
- JSON 모양도 그대로다. Jackson이 record 컴포넌트 이름을 JSON 키로 쓴다.
- record는 한 번 만들면 값을 바꿀 수 없다(불변).

**`Payment`를 record로 만들지 않은 이유**
- `savePayment`의 `useGeneratedKeys="true" keyProperty="id"`는 INSERT 후 DB가 만든 id를 **객체에 다시 넣는다**.
- record는 불변이라 이게 안 된다. 그래서 `Payment`는 class로 남겼다.

### 이름 규칙

- 팀 규칙: `~Request`, `~Response`. `Dto` 접미사를 붙이지 않는다.
- `dto` 패키지에 있다는 것 자체가 DTO라는 뜻이라 이름에 또 붙일 필요가 없다.

### 질문

**Q2-1.** `PortOnePaymentResponse`는 도메인일까 DTO일까? 이유는?

<details>
<summary>정답 보기</summary>

DTO. PortOne 서버(백엔드 바깥)와 주고받고, 모양을 PortOne이 정하고, 한 번 받아서 쓰고 버린다. 서비스가 필요한 값만 `Payment`/DB로 옮겨 적는다.

</details>

**Q2-2.** 컨트롤러가 `PaymentPrepareResponse` 대신 `Payment`를 그대로 반환하면 어떤 문제가 생길까? 두 가지 이상.

<details>
<summary>정답 보기</summary>

- `failCode`, `pgTxId`, `memberId` 등 내부 정보가 프론트에 노출된다.
- 테이블 컬럼을 바꾸면 API 응답도 같이 바뀌어서 프론트가 깨진다.
- API 스펙과 DB 구조가 묶여서 따로 바꿀 수 없다.

</details>

**Q2-3.** `Payment`를 record로 바꾸면 무엇이 깨질까?

<details>
<summary>정답 보기</summary>

`savePayment`의 `useGeneratedKeys`가 INSERT 후 id를 객체에 넣지 못한다. record는 불변이다. (MyBatis SELECT 결과 매핑도 생성자 방식으로 바꿔야 한다.)

</details>

---

## 3. DTO 8개 뜯어보기

| 상대 | 방향 | 클래스 |
|---|---|---|
| 프론트엔드 | 받음 | `PaymentPrepareRequest`, `PaymentCompleteRequest` |
| 프론트엔드 | 줌 | `PaymentPrepareResponse`, `PaymentCompleteResponse` |
| PortOne | 받음 (PortOne이 먼저 보냄) | `PortOneWebhookRequest` |
| PortOne | 줌 (우리가 요청) | `PortOneCancelRequest` |
| PortOne | 받음 (우리 요청의 응답) | `PortOnePaymentResponse`, `PortOneCancelResponse` |

### 3-1. PaymentPrepareRequest (/prepare 요청)

```java
public record PaymentPrepareRequest(
        @NotNull(message = "결제 대상 회원 ID는 필수입니다.")
        Long targetMemberId,

        @NotBlank(message = "주문명은 필수입니다.")
        @Size(max = 100, message = "주문명은 100자 이하여야 합니다.")
        String orderName,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        Integer totalAmount,

        @NotBlank(message = "결제 수단은 필수입니다.")
        @Size(max = 30, message = "결제 수단은 30자 이하여야 합니다.")
        String payMethod
) {}
```

- **일부러 없는 필드**: `memberId`(결제자), `paymentId`(주문번호), `status`, `currency`. 전부 서버가 정한다.
- `@NotNull`: null만 막는다. `Long`, `Integer`에 쓴다.
- `@NotBlank`: null, `""`, `"   "`을 전부 막는다. `String`에만 쓴다.
- `@Size(max = 100)`: DB `VARCHAR(100)`에 맞췄다. 여기서 안 막으면 DB에서 "Data too long" 에러 → 500.
- `@Positive`: 0과 음수를 막는다.
- 이 어노테이션들은 컨트롤러 파라미터에 `@Valid`가 붙어 있어야 동작한다.

### 3-2. PaymentPrepareResponse (/prepare 응답)

```java
public record PaymentPrepareResponse(String paymentId, String orderName, Integer totalAmount, String currency) {
    public static PaymentPrepareResponse from(Payment payment) {
        return new PaymentPrepareResponse(
                payment.getPaymentId(), payment.getOrderName(),
                payment.getTotalAmount(), payment.getCurrency());
    }
}
```

- 프론트는 이 4개로 PortOne 결제창(`PortOne.requestPayment`)을 띄운다.
- `from(Payment)`: 도메인 → DTO 변환 정적 팩토리 메서드. `Payment` 20개 필드 중 4개만 밖으로 나간다.

### 3-3. PaymentCompleteRequest (/complete 요청)

```java
public record PaymentCompleteRequest(
        @NotBlank(message = "주문번호(paymentId)는 필수입니다.")
        String paymentId
) {}
```

- **금액을 받지 않는다.** 브라우저가 "5000원 냈어요"라고 해도 믿지 않는다. 주문번호만 있으면 금액은 서버가 DB와 PortOne에서 직접 가져온다.

### 3-4. PaymentCompleteResponse (/complete 응답)

```java
public record PaymentCompleteResponse(String paymentId, String status, Integer paidAmount, String message) {}
```

- 프론트는 `status`로 화면을 나눈다.
  - PAID: 성공 화면
  - FAILED: `message`에 PG사 실패 사유 (예: "잔액이 부족합니다.")
  - CANCELLED: 위변조 안내
  - READY: 아직 결제 진행 중
- `paidAmount`는 PAID일 때만 값이 있고 나머지는 null. 그래서 `int`가 아니라 `Integer`.

### 3-5. PortOnePaymentResponse (PortOne 결제 조회 응답)

```java
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
        String status, String id, String transactionId, Amount amount,
        String paidAt, String pgTxId, String receiptUrl, Failure failure) {

    public record Amount(Integer total, Integer paid, Integer cancelled) {}
    public record Failure(String reason, String pgCode, String pgMessage) {}
}
```

- 필드 이름은 **PortOne API 문서가 정한다**. 우리 마음대로 못 바꾼다.
- `@JsonIgnoreProperties(ignoreUnknown = true)`: PortOne 응답에는 필드가 훨씬 많다. 우리가 쓰는 것만 받고 나머지는 무시한다. PortOne이 필드를 추가해도 안 깨진다.
- 중첩 record: JSON의 `{"amount": {"total": 5000, ...}}` 구조를 그대로 옮겼다.
- `paidAt`이 `String`인 이유: `"2026-09-28T04:00:00Z"`(UTC) 형식이라 서비스에서 한국 시간으로 직접 바꾼다 (11장).
- `@Builder`: 테스트에서 가짜 응답을 만들 때 편하려고 붙였다. record에도 Lombok `@Builder`가 된다.

### 3-6. PortOneCancelRequest / PortOneCancelResponse

```java
public record PortOneCancelRequest(String reason) {}

public record PortOneCancelResponse(Cancellation cancellation) {
    public record Cancellation(String status, String id, String pgCancellationId,
                               Integer totalAmount, String receiptUrl, String cancelledAt) {}
}
```

- 취소 요청 본문에 `amount`를 안 넣으면 PortOne은 **전액 취소**한다.

### 3-7. PortOneWebhookRequest

```java
public record PortOneWebhookRequest(String type, String timestamp, Data data) {
    public record Data(String paymentId, String storeId, String transactionId) {}
}
```

- 웹훅 버전 2024-04-25 형식.
- 이 본문은 **누구나 위조할 수 있다**. 그래서 `paymentId`만 꺼내 쓰고 나머지는 믿지 않는다 (12장).

### 질문

**Q3-1.** 프론트가 `"orderName": "   "`(공백 3칸)을 보내면 통과할까? `@NotBlank` 대신 `@NotNull`이었다면?

<details>
<summary>정답 보기</summary>

`@NotBlank`면 공백만 있는 문자열도 막혀서 400. `@NotNull`이었다면 null만 막으니 `"   "`는 통과하고 공백 주문명이 DB에 저장된다.

</details>

**Q3-2.** 해커가 /prepare body에 `"memberId": 5`를 몰래 넣어서 보내면 5번 회원 이름으로 결제가 만들어질까? 이유 두 가지.

<details>
<summary>정답 보기</summary>

안 만들어진다.
1. 받는 칸이 없다. `PaymentPrepareRequest`에는 `memberId` 필드가 없고, Spring Boot의 Jackson은 기본적으로 DTO에 없는 JSON 필드를 에러 없이 조용히 버린다.
2. 서비스는 JWT의 id만 쓴다. 칸이 있었더라도 서비스는 request가 아니라 JWT에서 꺼낸 `currentMemberId`를 넣는다 (`Payment.builder().memberId(currentMemberId)`).

</details>

**Q3-3.** `totalAmount: -5000`을 보내면 어느 단계에서 막히고 응답 코드는 몇일까?

<details>
<summary>정답 보기</summary>

컨트롤러의 `@Valid` 단계에서 `@Positive`에 걸린다. `MethodArgumentNotValidException` → 400. 서비스는 실행되지 않는다.

</details>

**Q3-4.** `PortOnePaymentResponse`에서 `@JsonIgnoreProperties(ignoreUnknown = true)`를 빼면 어떤 일이 생길 수 있을까?

<details>
<summary>정답 보기</summary>

PortOne 응답에는 우리가 선언하지 않은 필드가 많다. Jackson의 `FAIL_ON_UNKNOWN_PROPERTIES`가 켜진 환경이면 역직렬화가 실패해서 결제 조회가 전부 에러가 된다. (Spring Boot 기본값은 꺼져 있지만, 전역 설정에 의존하지 않고 클래스에 명시해 둔 것이다.)

</details>

---

## 4. 도메인 Payment

파일: `domain/Payment.java`

```java
@Getter @NoArgsConstructor @AllArgsConstructor @ToString @Builder
public class Payment {
    private Long id;
    private Long memberId;
    // ... payment 테이블 컬럼 20개와 1:1 ...
    private String status;

    public boolean isReady() {
        return "READY".equals(status);
    }
}
```

| 어노테이션 | 왜 필요한가 |
|---|---|
| `@Getter` | 서비스, DTO 변환(`from`), MyBatis가 값을 읽는다 |
| `@NoArgsConstructor` | MyBatis가 SELECT 결과를 담을 빈 객체를 만든다 |
| `@AllArgsConstructor` | `@Builder`가 내부적으로 쓴다 |
| `@Builder` | 서비스와 테스트에서 필요한 필드만 골라 만들기 |
| `@ToString` | 로그 찍을 때 |

- `@Setter`가 없다. 서비스에서 함부로 값을 바꾸지 못하게. 상태 변경은 전부 Mapper UPDATE로 DB에서 한다.
- MyBatis는 setter가 없어도 **리플렉션으로 필드에 직접** 값을 넣는다 (SELECT 결과, `useGeneratedKeys`의 id).
- `isReady()`: "아직 확정 안 된 결제인가"를 한 곳에서 표현한다. `"READY".equals(status)`처럼 문자열을 앞에 두면 `status`가 null이어도 NPE가 나지 않는다.

### 질문

**Q4-1.** `@NoArgsConstructor`를 지우면 어디서 문제가 생길까?

<details>
<summary>정답 보기</summary>

MyBatis가 SELECT 결과를 담을 빈 `Payment` 객체를 만들지 못한다. (`@AllArgsConstructor`만 있으면 MyBatis가 생성자 매핑을 시도하다가 컬럼 수/순서가 안 맞아 에러가 날 수 있다.) Jackson 등 기본 생성자가 필요한 라이브러리도 영향을 받는다.

</details>

**Q4-2.** `isReady()`를 `status.equals("READY")`로 쓰면 어떤 위험이 있을까?

<details>
<summary>정답 보기</summary>

`status`가 null이면 `NullPointerException`. `"READY".equals(status)`는 null이면 그냥 false.

</details>

---

## 5. Mapper 인터페이스와 XML 기초

파일: `mapper/PaymentMapper.java`, `resources/mapper/PaymentMapper.xml`

### Java와 XML이 연결되는 규칙

```java
@Mapper
public interface PaymentMapper {
    Payment findByPaymentIdForUpdate(@Param("paymentId") String paymentId);
}
```

```xml
<mapper namespace="net.likelion.bebc25.projectpatory.mapper.PaymentMapper">
    <select id="findByPaymentIdForUpdate" parameterType="string" resultType="...domain.Payment">
        ... WHERE payment_id = #{paymentId}
    </select>
</mapper>
```

- `namespace` = 인터페이스 전체 경로
- `id` = 메서드 이름
- `@Param("paymentId")` = XML의 `#{paymentId}` 이름
- 파라미터가 객체 하나면(`savePayment(Payment payment)`) `@Param` 없이 `#{memberId}`처럼 객체 필드 이름을 바로 쓴다.
- 파라미터가 여러 개면(`updatePaymentSuccess(...)`) 각각 `@Param`으로 이름을 붙인다.

### `#{}` 와 `${}`

- `#{}`: `?` 자리에 값을 **바인딩**한다 (PreparedStatement). SQL 인젝션이 막힌다. 결제 XML은 전부 `#{}`.
- `${}`: 문자열을 SQL에 **그대로 붙인다**. 인젝션 위험. 컬럼명/정렬 방향처럼 바인딩이 안 되는 곳에만 쓴다.

### `AS` 별칭

```sql
member_id AS memberId
```

- DB는 snake_case, Java는 camelCase. 별칭으로 맞춰야 MyBatis가 `Payment.memberId`에 넣는다.
- (`mybatis.configuration.map-underscore-to-camel-case: true` 설정을 켜면 별칭 없이도 자동 변환된다.)

### 메서드 목록

| 메서드 | SQL | 쓰는 곳 |
|---|---|---|
| `savePayment(Payment)` | INSERT | /prepare |
| `findByPaymentId(String)` | SELECT 전체 컬럼 | (현재 서비스에서는 안 씀, 테스트용) |
| `findByPaymentIdForUpdate(String)` | SELECT 6칸 + FOR UPDATE | /complete, 웹훅 |
| `updatePaymentSuccess(...)` | UPDATE → PAID | syncWithPortOne |
| `updatePaymentFail(...)` | UPDATE → FAILED/CANCELLED | 실패, 위변조 |
| `insertCancelPayment(...)` | INSERT cancel_payment | 위변조 |

- `update...`, `insert...`가 `int`를 반환하는 이유: **영향받은 행 수**. 지금 서비스는 이 값을 확인하지 않는다 (16장).

### 질문

**Q5-1.** XML의 `namespace`를 오타 내면 어떤 에러가 언제 날까?

<details>
<summary>정답 보기</summary>

앱 실행 후 해당 메서드를 처음 호출할 때 `BindingException: Invalid bound statement (not found)`가 난다. 인터페이스와 XML이 연결되지 않았다는 뜻이다.

</details>

**Q5-2.** `#{paymentId}` 대신 `'${paymentId}'`로 썼다면 어떤 공격이 가능할까?

<details>
<summary>정답 보기</summary>

SQL 인젝션. 예를 들어 `paymentId`에 `' OR '1'='1`을 넣으면 `WHERE payment_id = '' OR '1'='1'`이 되어 조건이 무력화된다. `#{}`는 값을 `?`에 바인딩해서 이런 문자열도 그냥 값으로 취급한다.

</details>

---

## 6. /prepare: 결제 사전 등록

### 0. 프론트가 보내는 요청

```http
POST /api/v1/payments/prepare
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "targetMemberId": 2,
  "orderName": "간식 쏘기",
  "totalAmount": 5000,
  "payMethod": "CARD"
}
```

body에 "누가 결제하는지"(결제자 ID)가 없다. 결제자는 JWT에서 꺼낸다. 이게 첫 번째 포인트다.

### 1. 보안 필터: JWT에서 결제자를 꺼낸다

- 요청이 컨트롤러에 닿기 전에 `JwtAuthenticationFilter`가 먼저 실행된다.
- JWT가 유효하면 사용자 정보를 Spring Security에 등록한다.
- `SecurityConfig`에서 `/prepare`는 `permitAll`이 아니라서 JWT가 없으면 401로 막힌다.

### 2. 컨트롤러

```java
@PostMapping("/prepare")
public ResponseEntity<PaymentPrepareResponse> preparePayment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PaymentPrepareRequest requestDto) {
    PaymentPrepareResponse response = paymentService.preparePayment(userDetails.getId(), requestDto);
    return ResponseEntity.ok(response);
}
```

- `@AuthenticationPrincipal`: 필터가 등록해 둔 로그인 사용자. `userDetails.getId()`가 `currentMemberId`가 된다.
- `@Valid`: 3-1의 검증 어노테이션을 실행한다. 실패하면 `MethodArgumentNotValidException` → 400.

### 3-1. 본인에게 결제하지 못하게

```java
if (currentMemberId.equals(targetMemberId)) {
    throw new IllegalArgumentException("본인에게는 결제할 수 없습니다.");
}
```

- `@Valid`는 **값 하나하나가 올바른 형식인가**를 확인한다. 이건 결제자와 대상의 **관계**라서 서비스에서 검사한다. 위반하면 400.
- `==`가 아니라 `.equals()`를 쓰는 이유: `Long`은 객체라서 `==`는 "같은 객체인가"를 비교한다. 자바는 -128~127 범위만 같은 객체를 재사용하기 때문에, 128 이상이면 값이 같아도 `==`가 false가 될 수 있다.

### 3-2. 후원 대상이 실제로 있는지

```java
Member targetMember = memberMapper.findById(targetMemberId);
if (targetMember == null) {
    throw new NoSuchElementException("결제 대상 회원을 찾을 수 없습니다. id=" + targetMemberId);
}
```

- 없는 회원에게 결제하면 돈은 나갔는데 받을 사람이 없는 상황이 된다. 결제 전에 막고 404를 응답한다.

### 3-3. 주문번호 만들기

```java
String randomText = UUID.randomUUID().toString().substring(0, 8);
String paymentId = "ORD_" + System.currentTimeMillis() + "_" + randomText;
```

- `ORD_`: 사람이 봐도 주문번호라는 걸 알 수 있게.
- `System.currentTimeMillis()`: 만든 시각(ms). 정렬하거나 추적할 때 유용.
- UUID 앞 8자리: 같은 ms에 두 요청이 와도 겹치지 않게.
- **주문번호를 서버가 만드는 게 핵심.** 프론트가 만들게 하면 남의 주문번호를 재사용하는 식의 장난이 가능해진다. 이 번호가 PortOne의 paymentId가 되어서, 이후 모든 조회, 취소, 웹훅이 이 값으로 연결된다.

### 3-4. Payment 도메인을 만들어 저장

```java
Payment payment = Payment.builder()
        .memberId(currentMemberId)          // body가 아니라 JWT에서
        .targetMemberId(targetMemberId)
        .paymentId(paymentId)               // 서버가 만든 값
        .orderName(requestDto.orderName())
        .currency("KRW")
        .totalAmount(requestDto.totalAmount())
        .payMethod(requestDto.payMethod())
        .status("READY")
        .build();
paymentMapper.savePayment(payment);
return PaymentPrepareResponse.from(payment);
```

### 4. 매퍼 XML: INSERT

```xml
<insert id="savePayment" parameterType="net.likelion.bebc25.projectpatory.domain.Payment"
        useGeneratedKeys="true" keyProperty="id">
    INSERT INTO payment (member_id, target_member_id, payment_id, order_name,
                         currency, total_amount, pay_method, status, created_at)
    VALUES (#{memberId}, #{targetMemberId}, #{paymentId}, #{orderName},
            IFNULL(#{currency}, 'KRW'), #{totalAmount}, #{payMethod}, 'READY', NOW())
</insert>
```

- `#{memberId}`: `Payment`의 `memberId` 필드 값이 `?` 자리에 안전하게 바인딩된다.
- `'READY'`, `NOW()`: Java에서 넘기지 않고 SQL에 고정했다. **새 결제는 무조건 READY로 시작한다**는 규칙을 쿼리 수준에서 보장한다. (그래서 Java의 `.status("READY")`는 사실 SQL에 안 쓰인다. 응답/테스트에서 객체 상태를 맞춰 두는 용도.)
- `IFNULL(#{currency}, 'KRW')`: currency가 null이면 KRW. 서비스가 이미 KRW를 넣으니 이중 안전장치.
- `useGeneratedKeys="true" keyProperty="id"`: INSERT 후 DB가 만든 AUTO_INCREMENT 값을 `payment.id`에 채운다. `Payment`에 setter가 없어도 MyBatis가 리플렉션으로 필드에 넣는다.

### /prepare의 진짜 역할

- 막는 게 아니라 **나중에 비교할 기준을 박제**해 두는 것.
- 사용자가 /prepare에서 5000원을 보내 놓고 결제창에서 100원으로 결제해도 /prepare에서는 막을 수 없다. /complete나 웹훅의 금액 비교에서 잡힌다 (8장).

### 질문

**Q6-1.** `targetMemberId`가 1000인 회원에게 1000번 회원이 결제하려 할 때, `==`로 비교했다면 어떻게 될까?

<details>
<summary>정답 보기</summary>

`Long` 1000은 캐시 범위(-128~127) 밖이라 서로 다른 객체일 수 있다. `==`가 false가 되어 본인 결제 검사를 통과해 버린다.

</details>

**Q6-2.** 주문번호를 프론트가 만들어서 보내게 하면 어떤 장난이 가능할까?

<details>
<summary>정답 보기</summary>

이미 존재하는 남의 주문번호를 재사용하거나, 추측하기 쉬운 번호를 만들어 충돌을 일으키거나, 결제 완료된 주문번호로 다시 결제를 시도하는 등의 장난이 가능하다. 서버가 만들면 번호의 형식과 유일성을 서버가 보장한다.

</details>

**Q6-3.** /prepare에서 5000원을 보내 놓고 결제창에서 100원으로 결제하면 /prepare에서 막을 수 있을까? 막을 수 없다면 어디서 잡힐까?

<details>
<summary>정답 보기</summary>

`/prepare`에서는 막을 수 없다. `/complete`나 웹훅의 금액 비교에서 잡힌다.
- `/prepare` 시점에는 요청이 5000원이라서 통과하고, DB에 `total_amount = 5000`이 저장된다.
- 이후 `/complete`나 웹훅이 오면 `syncWithPortOne()`이 PortOne에 조회한다. 실제 결제는 100원이라 DB의 5000과 비교해 불일치를 발견한다.
- `handleAmountMismatch()`가 PortOne에 결제 취소를 요청하고, DB를 CANCELLED로 바꾸고, `cancel_payment`에 이력을 남긴다.
- `/prepare`의 역할은 막기가 아니라 **나중에 비교할 기준을 박제해 두기**다.

</details>

---

## 7. /complete: 행 잠금과 본인 확인

### 0. 프론트가 보내는 요청

결제창이 닫히고 PortOne이 결과를 돌려주면, 프론트는 주문번호만 담아서 보낸다.

```http
POST /api/v1/payments/complete
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{ "paymentId": "ORD_1727500000000_1a2b3c4d" }
```

금액을 보내지 않는다. 금액은 서버가 DB와 PortOne에서 직접 가져온다.

### 1. 보안 필터

- `/complete`는 `permitAll` 목록에 없다. JWT가 없으면 401.
- 결제를 확정하는 사람이 누구인지도 body가 아니라 JWT로 안다.
- 같은 컨트롤러의 `/webhook`만 `permitAll`이다. PortOne 서버는 우리 JWT가 없으니까 (12장).

### 2. 컨트롤러

```java
@PostMapping("/complete")
public ResponseEntity<PaymentCompleteResponse> completePayment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PaymentCompleteRequest requestDto) {

    PaymentCompleteResponse response = paymentService.verifyAndCompletePayment(userDetails.getId(), requestDto);
    if ("PAID".equals(response.status())) {
        return ResponseEntity.ok(response);
    }
    return ResponseEntity.badRequest().body(response);
}
```

- `@Valid`: `paymentId`가 `""`나 `"   "`이면 서비스에 가기 전에 400.
- PAID면 200, 나머지(FAILED, CANCELLED, READY)는 400. 400이어도 body에 `status`와 `message`가 있어서 프론트가 화면을 나눈다.
- `"PAID".equals(response.status())`: 문자열을 앞에 두면 `status`가 null이어도 NPE가 안 난다.
- **따져볼 점:** READY는 "잘못 보냄"이 아니라 "아직 안 끝남"인데 400이다. PAID가 아닌 결과도 200으로 주고 프론트가 `status`로 나누게 하는 설계도 많다. 지금 방식이면 프론트는 400 응답 body까지 읽어야 한다.

### 3. 서비스: 트랜잭션을 열고 행을 잠근다

```java
@Transactional
public PaymentCompleteResponse verifyAndCompletePayment(Long currentMemberId, PaymentCompleteRequest requestDto) {
    String paymentId = requestDto.paymentId();
    Payment payment = paymentMapper.findByPaymentIdForUpdate(paymentId);
```

- `@Transactional`: 메서드 시작 시 트랜잭션이 열리고, 정상 종료 시 커밋, `RuntimeException`이 나면 롤백된다.
- 안에서 부르는 `syncWithPortOne`, `handleAmountMismatch` 등은 `private`이라 **같은 트랜잭션**을 그대로 쓴다. (`@Transactional`은 스프링 프록시를 거쳐야 동작하는데, 같은 클래스 안의 호출은 프록시를 거치지 않는다.)
- 그냥 `findByPaymentId`가 아니라 `ForUpdate` 버전을 쓴다. 행을 잠그기 위해서다.

**왜 잠가야 하나**

결제가 끝나면 `/complete`와 웹훅이 거의 동시에 올 수 있다.

```text
[잠금 없음]
/complete: SELECT(READY) → PortOne 조회 → UPDATE PAID
웹훅     : SELECT(READY) → PortOne 조회 → UPDATE PAID   ← 둘 다 READY를 봤다

[잠금 있음]
/complete: [BEGIN] SELECT FOR UPDATE(잠금) → READY → PortOne 조회 → UPDATE PAID → [COMMIT, 해제]
웹훅     :   [BEGIN] SELECT FOR UPDATE ....... 기다림 ...............................→ PAID 읽음 → isReady() false → 바로 반환
```

### 4. 매퍼 XML: SELECT ... FOR UPDATE

```xml
<select id="findByPaymentIdForUpdate" parameterType="string"
        resultType="net.likelion.bebc25.projectpatory.domain.Payment">
    SELECT
        id,
        member_id AS memberId,
        payment_id AS paymentId,
        total_amount AS totalAmount,
        paid_amount AS paidAmount,
        status
    FROM payment
    WHERE payment_id = #{paymentId}
    FOR UPDATE
</select>
```

- 6칸만 가져온다. 검증에 필요한 것만.
  - `id`: 나중에 `cancel_payment`에 넣을 PK
  - `member_id`: 본인 확인
  - `payment_id`: 주문번호
  - `total_amount`: 금액 비교 기준
  - `paid_amount`, `status`: 이미 처리된 결제인지 확인, 그 경우 응답에 사용
  - 그래서 이 `Payment`의 `orderName`, `targetMemberId`, `receiptUrl` 등은 **null**이다.
- `FOR UPDATE`: 이 행을 트랜잭션이 끝날 때까지 잠근다. 다른 트랜잭션의 `FOR UPDATE`나 `UPDATE`는 기다린다. (일반 `SELECT`는 MySQL InnoDB에서 잠금 없이 스냅샷을 읽기 때문에 기다리지 않는다.)
- 잠금은 **커밋이나 롤백 때** 풀린다. 그래서 3번의 `@Transactional`이 꼭 있어야 한다.
- 잠기는 건 이 주문 한 줄뿐이다. `payment_id`에 UNIQUE 인덱스가 있어서 MySQL이 인덱스로 딱 한 줄만 찾아 잠근다. 인덱스가 없으면 테이블을 훑으면서 지나간 행을 전부 잠가서 사실상 테이블 전체가 잠길 수 있다.

**따져볼 점:** 잠금을 잡은 채로 PortOne HTTP 호출을 한다. `RestTemplateConfig`의 타임아웃(연결 3초, 응답 10초) 때문에 최악의 경우 그 결제 한 줄이 약 13초 잠길 수 있다. 다른 사람 결제에는 영향이 없어서 지금 규모에서는 괜찮은 선택이다.

### 5. 본인 확인: 없는 주문과 남의 주문을 똑같이 거절

```java
if (payment == null) {
    throw new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + paymentId);
}
if (!payment.getMemberId().equals(currentMemberId)) {
    throw new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + paymentId);
}
```

- MyBatis는 결과가 없으면 예외 대신 **null**을 준다. 그래서 null 검사가 먼저다. 순서가 바뀌면 `payment.getMemberId()`에서 NPE → 500.
- DB의 `member_id`와 JWT의 `currentMemberId`를 비교한다. `Long`이라 `.equals()`.
- 두 경우 모두 **같은 메시지**. "남의 주문입니다"라고 따로 알려 주면, 해커가 주문번호를 바꿔 가며 "이 번호는 실제로 있구나"를 알아낼 수 있다.
- 둘 다 `IllegalArgumentException` → 400. 예외가 나면 롤백되면서 잠금도 바로 풀린다.

여기까지 통과하면 **"내 주문이 맞고, 지금 이 행은 나만 만지고 있다"** 는 상태로 `syncWithPortOne(payment)`에 들어간다.

### 질문

**Q7-1.** 해커가 남의 주문번호를 알아내서 자기 JWT로 `/complete`를 보냈다. 어느 단계에서 막히고 응답 코드는 몇일까? 그 사이 잡혔던 행 잠금은 어떻게 될까?

<details>
<summary>정답 보기</summary>

1(보안 필터)은 통과한다. JWT 자체는 유효하니까. 2(컨트롤러)도 통과한다. 4(`FOR UPDATE`)에서 그 행을 잠깐 잠근다. 5(본인 확인)에서 `payment.getMemberId()`(원래 주인)와 `currentMemberId`(해커)가 달라서 `IllegalArgumentException` → 400 "존재하지 않는 주문 번호입니다". 예외로 트랜잭션이 롤백되면서 잠금도 즉시 풀린다. 해커는 그 주문이 실제로 있는지조차 알 수 없다.

</details>

**Q7-2.** `@Transactional`을 지우면 `FOR UPDATE` 잠금은 언제 풀릴까? `/complete`와 웹훅이 동시에 오면 어떤 일이 생길 수 있을까?

<details>
<summary>정답 보기</summary>

트랜잭션이 없으면 MySQL의 autocommit 모드로 SELECT 한 문장이 끝나는 순간 커밋되고 잠금이 풀린다. `FOR UPDATE`가 사실상 의미 없어진다. `/complete`와 웹훅이 둘 다 READY를 읽고, 둘 다 PortOne을 조회하고, 둘 다 UPDATE를 시도한다. (13장의 `AND status = 'READY'` 덕분에 두 번째 UPDATE는 0행이 되지만, 위변조 케이스라면 PortOne 취소 API가 두 번 호출될 수 있다.)

</details>

**Q7-3.** `findByPaymentIdForUpdate`로 받은 `payment`에서 `payment.getOrderName()`을 부르면 무엇이 나올까? 왜?

<details>
<summary>정답 보기</summary>

null. `findByPaymentIdForUpdate`의 SELECT에 `order_name`이 없어서 MyBatis가 그 필드를 채우지 않는다.

</details>

**Q7-4.** `payment_id`에 UNIQUE 인덱스가 없다면 `FOR UPDATE`가 어떻게 달라질까?

<details>
<summary>정답 보기</summary>

MySQL이 `payment_id`로 행을 찾으려고 테이블을 훑으면서 지나간 행에 전부 잠금을 건다. 사실상 테이블 전체가 잠겨서 다른 사람의 결제까지 기다리게 된다.

</details>

**Q7-5.** null 검사와 본인 확인의 순서를 바꾸면 어떻게 될까?

<details>
<summary>정답 보기</summary>

주문이 없을 때 `payment`가 null인데 `payment.getMemberId()`를 먼저 부르니 `NullPointerException` → 500.

</details>

---

## 8. syncWithPortOne: 네 갈래 분기

`/complete`와 웹훅이 **함께** 쓰는 핵심 로직. PortOne에서 실제 결제 내역을 조회하고, DB 금액과 비교해서 PAID/FAILED/CANCELLED로 확정한다.

```text
syncWithPortOne(payment)
 ├─ ① 이미 처리됨? (READY 아님)     → 현재 상태 그대로 반환 (PortOne 호출 X)
 ├─ PortOne 조회
 ├─ ② PortOne FAILED               → handleFailedPayment → FAILED 저장
 ├─ ③ PortOne PAID 아님 (READY 등)  → DB 그대로, READY 반환
 ├─ ④ 금액 불일치                    → handleAmountMismatch → PortOne 취소 + CANCELLED
 └─ ⑤ 전부 통과                      → updatePaymentSuccess → PAID 저장
```

### ① 이미 처리된 결제

```java
if (!payment.isReady()) {
    return new PaymentCompleteResponse(paymentId, payment.getStatus(), payment.getPaidAmount(),
            "이미 처리된 결제입니다.");
}
```

- **멱등성(idempotency)**: 같은 요청이 여러 번 와도 결과가 같다.
- 사용자가 /complete를 두 번 누르거나, 웹훅이 재전송되거나, /complete와 웹훅이 둘 다 와도 한 번만 처리된다.
- PortOne API를 다시 부르지 않는다. 불필요한 외부 호출을 줄인다.
- 이미 PAID면 `status`가 "PAID"라서 컨트롤러는 200을 준다.

### PortOne 조회

```java
PortOnePaymentResponse portOnePayment = getPortOnePayment(paymentId);
String portOneStatus = portOnePayment.status();
```

- `getPortOnePayment`는 `status`나 `amount`가 null이면 예외를 던진다 (11장). 그래서 아래에서 `portOneStatus.equals(...)`를 해도 NPE가 안 난다.

### ② PortOne이 FAILED

```java
if (portOneStatus.equals("FAILED")) {
    return handleFailedPayment(paymentId, portOnePayment);
}
```

- 잔액 부족, 카드 한도 초과 등. 9장.

### ③ PortOne이 PAID가 아님

```java
if (!portOneStatus.equals("PAID")) {
    return new PaymentCompleteResponse(paymentId, "READY", null,
            "결제가 아직 완료되지 않았습니다. (PortOne 상태: " + portOneStatus + ")");
}
```

- PortOne 결제 상태: `READY`, `PENDING`, `VIRTUAL_ACCOUNT_ISSUED`, `PAID`, `FAILED`, `PARTIAL_CANCELLED`, `CANCELLED`.
- PAID도 FAILED도 아니면 **DB를 건드리지 않는다**. 나중에 웹훅이 오거나 /complete를 다시 부르면 그때 확정된다.
- **따져볼 점:** PortOne이 `CANCELLED`인 경우(예: PortOne 콘솔에서 관리자가 취소)도 여기로 와서 영원히 READY로 남는다 (16장).

### ④ 금액 비교

```java
Integer expectedAmount = payment.getTotalAmount();              // DB: /prepare 때 박제한 금액
Integer actualPaidAmount = portOnePayment.amount().total();     // PortOne: 실제 결제 요청 금액

if (!expectedAmount.equals(actualPaidAmount)) {
    return handleAmountMismatch(payment, actualPaidAmount);
}
```

- **이 시스템 보안의 핵심.** 브라우저가 무슨 짓을 해도 DB 금액과 PortOne 금액은 우리 서버가 직접 가져온 값이다.
- `Integer`끼리 `.equals()`. `!=`로 비교하면 128 이상에서 틀린다 (6장 3-1과 같은 이유).
- `amount().total()`(결제 요청 금액)을 비교한다. PortOne 공식 예제도 `total`을 비교한다.

### ⑤ 모든 검사 통과 → PAID

```java
LocalDateTime paidAt = toKoreanTime(portOnePayment.paidAt());
paymentMapper.updatePaymentSuccess(paymentId, portOnePayment.transactionId(), portOnePayment.pgTxId(),
        portOnePayment.receiptUrl(), actualPaidAmount, paidAt);
return new PaymentCompleteResponse(paymentId, "PAID", actualPaidAmount, "결제 성공 및 검증이 완료되었습니다.");
```

```xml
<update id="updatePaymentSuccess">
    UPDATE payment
    SET status = 'PAID',
        transaction_id = #{transactionId},
        pg_tx_id = #{pgTxId},
        receipt_url = #{receiptUrl},
        paid_amount = #{paidAmount},
        paid_at = COALESCE(#{paidAt}, NOW())
    WHERE payment_id = #{paymentId}
      AND status = 'READY'
</update>
```

- `COALESCE(#{paidAt}, NOW())`: PortOne이 결제 시각을 안 주면 지금 시각으로.
- `AND status = 'READY'`: 13장.

### 질문

**Q8-1.** 이미 PAID인 결제에 웹훅이 또 오면 코드 어느 줄에서 끝나고, PortOne API는 몇 번 호출될까?

<details>
<summary>정답 보기</summary>

`syncWithPortOne`의 ① `if (!payment.isReady())`에서 반환한다. PortOne API는 0번 호출된다.

</details>

**Q8-2.** ③에서 DB를 FAILED로 바꾸지 않고 그대로 두는 이유는?

<details>
<summary>정답 보기</summary>

PortOne이 READY/PENDING이라는 건 아직 결제가 진행 중이라는 뜻이다. 여기서 FAILED로 확정하면 나중에 실제로 결제가 성공해도 `AND status = 'READY'` 때문에 PAID로 바꿀 수 없다. 돈은 나갔는데 DB는 FAILED인 최악의 상황이 된다.

</details>

**Q8-3.** 금액 비교를 `expectedAmount != actualPaidAmount`로 쓰면 어떤 버그가 생길까? 어떤 금액부터?

<details>
<summary>정답 보기</summary>

`Integer`끼리 `!=`는 객체 비교다. -128~127은 캐시된 같은 객체라 우연히 맞지만, 128원 이상이면 값이 같아도 다른 객체라서 `!=`가 true가 된다. 정상 결제(5000원 = 5000원)가 전부 위변조로 판단되어 자동 취소된다.

</details>

**Q8-4.** `getPortOnePayment`에서 `body.status() == null` 검사를 빼면 어디서 무슨 에러가 날까?

<details>
<summary>정답 보기</summary>

`portOneStatus`가 null이 되고 `portOneStatus.equals("FAILED")`에서 `NullPointerException` → 500.

</details>

---

## 9. handleFailedPayment: 결제 실패

```java
private PaymentCompleteResponse handleFailedPayment(String paymentId, PortOnePaymentResponse portOnePayment) {
    String failCode = null;
    String failMessage = null;

    PortOnePaymentResponse.Failure failure = portOnePayment.failure();
    if (failure != null) {
        failCode = failure.pgCode();
        failMessage = failure.pgMessage();
    }

    paymentMapper.updatePaymentFail(paymentId, "FAILED", failCode, failMessage, null, null);

    String message = "결제에 실패했습니다.";
    if (failMessage != null) {
        message = failMessage;
    }
    return new PaymentCompleteResponse(paymentId, "FAILED", null, message);
}
```

- `failure`는 FAILED일 때만 오는데, 그래도 null일 수 있어서 방어한다.
- PG사 메시지(예: "잔액이 부족합니다.")가 있으면 그걸 사용자에게 보여 주고, 없으면 기본 메시지.
- `cancelAmount`, `cancelReason`은 null. 실패는 취소가 아니다.
- PortOne에 취소 요청을 하지 않는다. 돈이 안 나갔으니까.

```xml
<update id="updatePaymentFail">
    UPDATE payment
    SET status = #{status},
        fail_code = #{failCode},
        fail_message = #{failMessage},
        cancel_amount = #{cancelAmount},
        cancel_reason = #{cancelReason},
        cancelled_at = IF(#{status} = 'CANCELLED', NOW(), NULL)
    WHERE payment_id = #{paymentId}
      AND status = 'READY'
</update>
```

- FAILED와 CANCELLED가 **같은 쿼리**를 쓴다. `status` 파라미터로 구분.
- `IF(#{status} = 'CANCELLED', NOW(), NULL)`: 취소일 때만 취소 시각을 채운다.

### 질문

**Q9-1.** FAILED일 때 PortOne 취소 API를 부르지 않는 이유는?

<details>
<summary>정답 보기</summary>

FAILED는 돈이 안 나간 상태다. 취소할 결제가 없다.

</details>

**Q9-2.** `updatePaymentFail`에 FAILED를 넘기면 `cancelled_at`은 어떤 값이 될까?

<details>
<summary>정답 보기</summary>

`IF('FAILED' = 'CANCELLED', NOW(), NULL)` → NULL.

</details>

---

## 10. handleAmountMismatch: 위변조 자동 취소

```text
① PortOne에 취소 요청 (돈 돌려주기)
② payment → CANCELLED
③ cancel_payment에 이력 INSERT
```

### ① PortOne 취소

```java
PortOneCancelResponse.Cancellation cancellation = cancelPortOnePayment(paymentId, AMOUNT_MISMATCH_REASON);
```

- 사용자가 100원을 실제로 냈으니 돌려줘야 한다. 우리는 5000원짜리 상품을 100원에 줄 수 없다.
- 취소 금액을 안 넘기면 **전액 취소**.

### ② payment → CANCELLED

```java
paymentMapper.updatePaymentFail(paymentId, "CANCELLED", "AMOUNT_MISMATCH",
        "DB 예정금액과 PG 실결제 금액 불일치", actualPaidAmount, AMOUNT_MISMATCH_REASON);
```

- `cancel_amount`에는 **실제 결제된 금액**(100원)을 넣는다. 예정 금액(5000원)이 아니다.

### ③ cancel_payment 이력

```java
String cancellationId = null;
String pgCancellationId = null;
String cancelStatus = "SUCCEEDED";
Integer cancelAmount = actualPaidAmount;
String receiptUrl = null;
LocalDateTime cancelledAt = LocalDateTime.now();

if (cancellation != null) {
    cancellationId = cancellation.id();
    // ... PortOne 응답에 값이 있으면 덮어쓴다 ...
}

paymentMapper.insertCancelPayment(payment.getId(), cancellationId, pgCancellationId,
        cancelStatus, cancelAmount, AMOUNT_MISMATCH_REASON, receiptUrl, cancelledAt);
```

- **기본값을 먼저 넣고, PortOne 응답에 값이 있으면 덮어쓰는** 패턴. PortOne 응답이 비어 있어도 `cancel_payment`의 NOT NULL 컬럼(`status`, `cancel_amount`, `reason`)을 채울 수 있다.
- `payment.getId()`: 숫자 PK. `findByPaymentIdForUpdate`가 `id`를 SELECT하는 이유가 이것.

### 따져볼 점: 외부 호출과 DB 트랜잭션

PortOne 취소는 **외부 시스템**이라 DB 롤백으로 되돌릴 수 없다.

| 상황 | 결과 |
|---|---|
| ① PortOne 취소가 실패 (예외) | `PaymentGatewayException` → 롤백 → DB는 READY, 돈은 결제된 상태. 다음 /complete나 웹훅 재전송 때 다시 금액 불일치 → 다시 취소 시도. **복구 가능** |
| ① PortOne은 실제로 취소했는데 응답이 타임아웃 | 예외 → 롤백 → DB는 READY인데 **PortOne은 이미 취소됨**. 다음 조회 때 PortOne 상태가 CANCELLED라 8장 ③ 분기(PAID 아님)로 가서 영원히 READY. **불일치** |
| ① 성공 후 ② 또는 ③에서 DB 에러 | 롤백 → 위와 같은 불일치 |

- 한 트랜잭션 안이라, PortOne 취소가 예외를 던지면 그 앞에 한 DB 변경도 같이 롤백된다. 그래서 **예외가 나는 경우에는** 순서를 바꿔도 결과가 같다.
- 순서가 중요해지는 건 DB 변경이 먼저 커밋되는 구조(별도 트랜잭션, 취소 실패를 예외 없이 삼키는 코드 등)일 때다. 그러면 **돈은 결제됐는데 DB는 CANCELLED**가 된다. 그래서 "외부 호출이 성공한 걸 확인한 뒤에 DB를 확정한다"는 원칙대로 PortOne을 먼저 부른다.
- 남는 불일치(PortOne CANCELLED, DB READY)는 PortOne `CANCELLED` 상태를 처리하면 해결된다 (16장 2번).

### 질문

**Q10-1.** `cancel_amount`에 5000(예정 금액)이 아니라 100(실제 금액)을 넣는 이유는?

<details>
<summary>정답 보기</summary>

실제로 사용자 카드에서 나간 돈은 100원이고, 돌려주는 돈도 100원이다. 기록은 실제 돈의 흐름과 맞아야 한다.

</details>

**Q10-2.** PortOne 취소 요청과 DB 업데이트 순서를 바꾸면 어떤 위험이 있을까?

<details>
<summary>정답 보기</summary>

지금처럼 한 트랜잭션 안이면, PortOne 취소가 예외를 던질 때 앞의 DB 변경도 롤백되니 결과는 같다. 하지만 DB 변경이 먼저 커밋되는 구조라면(별도 트랜잭션, 실패를 삼키는 코드) PortOne 취소가 실패했을 때 **돈은 결제됐는데 DB는 CANCELLED**가 된다. 사용자는 돈을 잃고, 우리는 취소된 줄 안다. 그래서 외부 호출 성공을 먼저 확인하고 DB를 확정하는 순서가 원칙이다.

</details>

**Q10-3.** PortOne 취소 응답이 null이면 `cancel_payment`의 `cancellation_id`에는 무엇이 들어갈까? `cancellation_id`가 UNIQUE인데 null이 여러 개 들어가도 괜찮을까?

<details>
<summary>정답 보기</summary>

null. MySQL의 UNIQUE 인덱스는 null을 여러 개 허용한다 (null은 서로 같지 않다고 본다). 그래서 괜찮다.

</details>

---

## 11. PortOne API 호출

### 인증 헤더

```java
private HttpHeaders createPortOneHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "PortOne " + apiSecret);
    return headers;
}
```

- PortOne V2 형식: `Authorization: PortOne {API Secret}`.
- `apiSecret`은 `@Value("${portone.api.secret}")`로 설정 파일/환경 변수에서 온다. **코드나 git에 절대 넣지 않는다.**
- 이 키가 있어야 결제 조회/취소를 할 수 있다. 그래서 반드시 **서버에서만** 호출한다. 프론트에서 부르면 키가 노출된다.

### 결제 조회: getPortOnePayment

```java
try {
    response = restTemplate.exchange(url, HttpMethod.GET, request, PortOnePaymentResponse.class);
} catch (HttpClientErrorException e) {          // 4xx
    if (e.getStatusCode().value() == 404) {
        throw new NoSuchElementException("PortOne에서 결제 내역을 찾을 수 없습니다: " + paymentId);
    }
    throw new PaymentGatewayException("PortOne 결제 조회에 실패했습니다.", e);
} catch (RestClientException e) {               // 5xx, 타임아웃, 네트워크
    throw new PaymentGatewayException("PortOne 결제 조회에 실패했습니다.", e);
}

PortOnePaymentResponse body = response.getBody();
if (body == null || body.status() == null || body.amount() == null) {
    throw new PaymentGatewayException("PortOne 결제 조회 응답이 올바르지 않습니다.");
}
```

- `catch` 순서: `HttpClientErrorException`은 `RestClientException`의 자식이다. 자식을 먼저 잡아야 한다. (반대로 쓰면 컴파일 에러.)
- 404 → `NoSuchElementException` → 우리 응답 404. PortOne에 그런 결제가 없다 = 결제창을 안 열었거나 주문번호가 잘못됨.
- 나머지 → `PaymentGatewayException` → 502 Bad Gateway. "우리 서버가 아니라 상대 서버 문제"라는 뜻.
- 응답 검증: 검증에 필요한 값이 없으면 **통과시키지 않고** 실패로 본다. 결제에서는 애매하면 실패가 안전하다.

### 결제 취소: cancelPortOnePayment

```java
headers.setContentType(MediaType.APPLICATION_JSON);
HttpEntity<PortOneCancelRequest> request = new HttpEntity<>(new PortOneCancelRequest(reason), headers);
response = restTemplate.postForObject(url, request, PortOneCancelResponse.class);
```

- `POST /payments/{paymentId}/cancel`, body `{"reason": "..."}`.
- `exchange`는 `ResponseEntity`(상태 코드, 헤더 포함)를 주고, `postForObject`는 body만 준다.

### RestTemplate 빈과 타임아웃

```java
factory.setConnectTimeout(Duration.ofSeconds(3));
factory.setReadTimeout(Duration.ofSeconds(10));
```

- 서비스 안에서 `new RestTemplate()` 하지 않고 **빈으로 주입**한다. 테스트에서 `@Mock RestTemplate`으로 바꿔 끼울 수 있다.
- 타임아웃이 없으면 PortOne이 응답을 안 줄 때 스레드가 무한정 기다리고, `FOR UPDATE` 잠금도 계속 잡혀 있다.

### UTC → 한국 시간

```java
private LocalDateTime toKoreanTime(String isoDateTime) {
    if (isoDateTime == null) return null;
    OffsetDateTime utcTime = OffsetDateTime.parse(isoDateTime);
    return utcTime.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
}
```

- `"2026-09-28T04:00:00Z"`의 `Z` = UTC. 한국은 UTC+9라서 `2026-09-28 13:00:00`.
- `atZoneSameInstant`: **같은 순간**을 다른 시간대로 표현. (`atZoneSimilarLocal`은 시계 숫자만 유지해서 순간이 바뀐다.)
- DB `DATETIME`은 시간대 정보가 없어서 한국 시간으로 통일해서 넣는다.

### 질문

**Q11-1.** PortOne API를 프론트에서 직접 호출하면 안 되는 이유는?

<details>
<summary>정답 보기</summary>

API Secret이 브라우저 코드에 들어가서 누구나 볼 수 있게 된다. 그 키로 우리 상점의 결제를 조회하고 **취소**까지 할 수 있다. 또 브라우저가 "PortOne이 PAID래요"라고 하는 걸 믿어야 해서 금액 검증이 무의미해진다.

</details>

**Q11-2.** `catch (RestClientException e)`를 `catch (HttpClientErrorException e)`보다 위에 쓰면?

<details>
<summary>정답 보기</summary>

컴파일 에러. 부모(`RestClientException`)가 먼저 다 잡아 버리면 자식 catch는 절대 실행되지 않는 코드(unreachable)가 되기 때문이다.

</details>

**Q11-3.** PortOne 5xx일 때 우리 서버가 500이 아니라 502를 주는 이유는?

<details>
<summary>정답 보기</summary>

500은 "우리 서버 내부 문제", 502 Bad Gateway는 "우리가 중계하는 상대 서버가 이상한 응답을 줬다"는 뜻이다. 원인을 정확히 알려 줘야 프론트/운영자가 대응할 수 있다. 웹훅의 경우 PortOne이 재전송하게 만드는 효과도 있다.

</details>

**Q11-4.** `"2026-09-28T15:30:00Z"`는 한국 시간으로 언제일까?

<details>
<summary>정답 보기</summary>

UTC+9 → 2026-09-29 00:30:00 (날짜가 바뀐다).

</details>

---

## 12. 웹훅

### 왜 필요한가

- 사용자가 결제 후 브라우저를 닫거나, 네트워크가 끊기거나, 프론트 버그로 `/complete`가 안 오면 DB는 영원히 READY.
- PortOne이 **서버 대 서버**로 직접 알려 주면 이 경우도 확정할 수 있다.

### 컨트롤러

```java
@PostMapping("/webhook")
public ResponseEntity<Void> receiveWebhook(@RequestBody PortOneWebhookRequest webhook) {
    paymentService.handleWebhook(webhook);
    return ResponseEntity.ok().build();
}
```

- `SecurityConfig`: `.requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()`. PortOne은 JWT가 없다.
- `@AuthenticationPrincipal`이 없다. 본인 확인도 없다.
- 200을 못 받으면 PortOne이 재전송한다. 그래서 처리했거나 무시한 경우 모두 200.
- `@Valid`가 없다. 형식이 이상한 웹훅도 400으로 거절하지 않고 서비스에서 조용히 무시한다. 400을 주면 PortOne이 계속 재전송하니까.

### 서비스

```java
@Transactional
public void handleWebhook(PortOneWebhookRequest webhook) {
    String type = webhook.type();

    // 1. 결제 승인/실패 이벤트만 처리
    if (!"Transaction.Paid".equals(type) && !"Transaction.Failed".equals(type)) {
        return;
    }
    if (webhook.data() == null || webhook.data().paymentId() == null) {
        return;
    }
    String paymentId = webhook.data().paymentId();

    // 2. 잠가서 조회
    Payment payment = paymentMapper.findByPaymentIdForUpdate(paymentId);

    // 3. DB에 없으면 조용히 끝
    if (payment == null) {
        return;
    }

    // 4. /complete와 같은 로직
    PaymentCompleteResponse result = syncWithPortOne(payment);
}
```

### 핵심: 본문을 믿지 않는다

- 웹훅 URL은 `permitAll`이라 **누구나** 호출할 수 있다.
- 해커가 `{"type": "Transaction.Paid", "data": {"paymentId": "ORD_..."}}`를 보내도, 우리는 **PortOne API에 다시 물어본다**. PortOne이 PAID라고 해야, 그리고 금액이 맞아야 PAID가 된다.
- 본문에서 쓰는 건 `type`(처리할지 말지)과 `paymentId`(무엇을 조회할지)뿐이다.
- 본인 확인이 없어도 되는 이유: "누가 요청했나"가 아니라 "PortOne의 실제 결제 상태"로 판단하니까.

### 조용히 끝내는 경우 vs 예외를 던지는 경우

| 상황 | 처리 | 응답 | PortOne 재전송 |
|---|---|---|---|
| 처리하지 않는 이벤트 (`Transaction.Ready` 등) | return | 200 | X |
| `paymentId` 없음 | return | 200 | X |
| DB에 없는 주문 | return | 200 | X (재전송해도 계속 없음) |
| 이미 처리된 결제 | syncWithPortOne ①에서 반환 | 200 | X |
| PortOne 조회 실패 (5xx, 타임아웃) | `PaymentGatewayException` | 502 | **O (의도적)** |

- 재전송해도 결과가 안 바뀌는 경우는 조용히 끝낸다.
- 일시적인 장애(PortOne 5xx)는 예외를 던져서 **재전송을 유도**한다. 나중에 PortOne이 살아나면 다시 와서 처리된다.

### /complete vs 웹훅 비교

| | /complete | 웹훅 |
|---|---|---|
| 누가 보내나 | 사용자 브라우저 | PortOne 서버 |
| 인증 | JWT | 없음 (`permitAll`) |
| 본인 확인 | O | X |
| DB에 없는 주문 | 예외 → 400 | 조용히 200 |
| 공통 로직 | `syncWithPortOne` | `syncWithPortOne` |
| 응답 | `PaymentCompleteResponse` | body 없음 |

### 질문

**Q12-1.** 웹훅에는 본인 확인이 없다. 왜 없어도 될까?

<details>
<summary>정답 보기</summary>

웹훅은 "누가 요청했나"로 판단하지 않는다. 본문에서 주문번호만 꺼내고, 실제 결제 상태와 금액은 PortOne API에 직접 물어봐서 판단한다. 누가 보냈든 결과는 PortOne의 실제 상태대로만 바뀐다.

</details>

**Q12-2.** 해커가 가짜 `Transaction.Paid` 웹훅을 보내면 결제가 PAID가 될까?

<details>
<summary>정답 보기</summary>

안 된다. 우리 서버가 PortOne API에 다시 물어보는데, 실제로 결제가 안 됐으면 PortOne은 READY나 FAILED를 돌려준다. 결제가 실제로 됐다면 원래 PAID가 되는 게 맞으니 문제없다.

</details>

**Q12-3.** DB에 없는 주문번호로 웹훅이 오면 왜 예외를 던지지 않고 조용히 끝낼까?

<details>
<summary>정답 보기</summary>

예외 → 4xx/5xx 응답 → PortOne이 같은 웹훅을 재전송한다. 그런데 DB에 없는 주문은 몇 번을 다시 보내도 계속 없다. 쓸데없는 재전송만 반복되니 200으로 받고 끝낸다.

</details>

**Q12-4.** 그런데 PortOne 5xx일 때는 왜 예외를 던질까?

<details>
<summary>정답 보기</summary>

PortOne 5xx는 일시적인 장애일 가능성이 크다. 502를 주면 PortOne이 나중에 다시 보내 주고, 그때 PortOne이 살아 있으면 정상 처리된다. 200을 주면 그 웹훅은 영영 다시 안 온다.

</details>

---

## 13. XML UPDATE의 두 번째 안전장치

```sql
UPDATE payment SET status = 'PAID', ...
WHERE payment_id = #{paymentId}
  AND status = 'READY'
```

- Java의 `isReady()`가 **첫 번째 안전장치**, SQL의 `AND status = 'READY'`가 **두 번째 안전장치**.
- 지금은 `FOR UPDATE`와 `isReady()`로 이미 막히지만, 누가 나중에 `FOR UPDATE` 없이 이 UPDATE를 부르는 코드를 짜도 **이미 확정된 결제는 DB가 안 바꿔 준다**.
- 조건에 안 맞으면 UPDATE는 **0행**을 바꾸고 에러 없이 끝난다. 그래서 반환값(`int`)을 확인하면 "실제로 바뀌었나"를 알 수 있다. 지금 서비스는 확인하지 않는다 (16장).

### 질문

**Q13-1.** 이미 PAID인 결제에 `updatePaymentFail(..., "CANCELLED", ...)`가 실행되면 DB는 어떻게 될까? 반환값은?

<details>
<summary>정답 보기</summary>

`WHERE ... AND status = 'READY'` 조건에 안 맞아서 아무 행도 바뀌지 않는다. DB는 PAID 그대로. 반환값은 0. 에러는 나지 않는다.

</details>

---

## 14. 예외 → HTTP 상태 코드 정리

`GlobalRestExceptionHandler` (`@RestControllerAdvice`)가 서비스에서 던진 예외를 HTTP 응답으로 바꾼다.

| 예외 | 코드 | 결제에서 언제 |
|---|---|---|
| (필터) JWT 없음/만료 | 401 | /prepare, /complete |
| `MethodArgumentNotValidException` | 400 | `@Valid` 실패 |
| `IllegalArgumentException` | 400 | 본인에게 결제, 없는 주문, 남의 주문 |
| `NoSuchElementException` | 404 | 후원 대상 없음, PortOne 404 |
| `PaymentGatewayException` | 502 | PortOne 5xx, 타임아웃, 응답 이상, 취소 실패 |
| `Exception` (그 외) | 500 | 예상 못 한 버그 (NPE 등) |
| (정상) PAID | 200 | /complete |
| (정상) FAILED/CANCELLED/READY | 400 | /complete (컨트롤러가 직접) |

- `PaymentGatewayException extends RuntimeException`: `@Transactional`이 롤백한다. (checked exception은 기본적으로 롤백하지 않는다.)

### 질문

**Q14-1.** 서비스에서 `throw new Exception(...)`(checked)을 던지면 `@Transactional`은 롤백할까?

<details>
<summary>정답 보기</summary>

기본적으로 롤백하지 않는다. `@Transactional`은 `RuntimeException`과 `Error`만 롤백한다. checked exception도 롤백하려면 `@Transactional(rollbackFor = Exception.class)`.

</details>

---

## 15. 테스트 코드 읽기

파일: `src/test/java/.../service/PaymentServiceImplTest.java` (17개)

### 구조

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock private PaymentMapper paymentMapper;
    @Mock private MemberMapper memberMapper;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private PaymentServiceImpl paymentService;
}
```

- **단위 테스트**: 스프링을 띄우지 않는다. DB도 PortOne도 없다. 서비스 로직만 검사.
- `@Mock`: 가짜 객체. 아무것도 안 하고 null/0을 돌려준다. `given(...)`으로 동작을 정해 준다.
- `@InjectMocks`: 가짜들을 생성자로 주입해서 진짜 `PaymentServiceImpl`을 만든다 (`@RequiredArgsConstructor` 덕분).
- `apiSecret`은 주입 안 돼서 null. 가짜 RestTemplate이라 상관없다.

### 자주 쓰는 도구

| 도구 | 뜻 |
|---|---|
| `given(mock.method(...)).willReturn(x)` | 이렇게 부르면 x를 돌려줘 |
| `willThrow(e)` | 이렇게 부르면 예외를 던져 |
| `verify(mock).method(...)` | 이 메서드가 이 인자로 불렸는지 확인 |
| `verify(mock, never()).method(...)` | 절대 안 불렸는지 확인 |
| `verifyNoInteractions(mock)` | 이 가짜를 아예 안 건드렸는지 |
| `ArgumentCaptor` | 메서드에 넘어간 인자를 붙잡아서 꺼내 보기 |
| `any()`, `eq(x)`, `anyString()` | 인자 매처. 섞어 쓸 때는 전부 매처로 써야 한다 |

### 테스트 목록

| 테스트 | 검사 내용 |
|---|---|
| `preparePayment_Success` | 주문번호 `ORD_`로 시작, `ArgumentCaptor`로 저장된 Payment의 결제자/대상/READY 확인 |
| `preparePayment_Self` | 본인 결제 → `IllegalArgumentException` |
| `preparePayment_TargetNotFound` | 대상 없음 → `NoSuchElementException` |
| `complete_Success` | 금액 일치 → `updatePaymentSuccess` 호출, paidAt UTC→KST 변환 확인 |
| `complete_AmountMismatch` | 5000 vs 100 → 취소 API, CANCELLED, cancel_payment INSERT, PAID는 `never()` |
| `complete_PortOneFailed` | FAILED + PG 메시지 저장 |
| `complete_NotPaidYet` | PortOne READY → UPDATE 둘 다 `never()` |
| `complete_AlreadyPaid` | 이미 PAID → PortOne 호출 안 함 |
| `complete_PaymentNotFound` | 없는 주문 → `IllegalArgumentException` |
| `complete_OtherMember` | 남의 주문 → `IllegalArgumentException` |
| `complete_PortOneNotFound` | PortOne 404 → `NoSuchElementException` |
| `complete_PortOneServerError` | PortOne 500 → `PaymentGatewayException` |
| `webhook_Paid` | 웹훅 → PAID |
| `webhook_AlreadyPaid` | /complete가 먼저 처리 → PortOne 호출 안 함 |
| `webhook_IgnoredType` | `Transaction.Ready` → DB도 PortOne도 안 건드림 |
| `webhook_PaymentNotFound` | DB에 없음 → 예외 없이 끝 |
| `webhook_PortOneServerError` | PortOne 500 → `PaymentGatewayException` (재전송 유도) |

### 이 테스트가 못 잡는 것

- `FOR UPDATE`가 실제로 잠그는지 (Mapper가 가짜라서)
- XML 쿼리가 맞는지 → `PaymentMapperTest`(DB 필요)가 담당
- 컨트롤러의 200/400 분기, `@Valid` → 컨트롤러 테스트(`@WebMvcTest`) 필요
- 동시성 → 여러 스레드로 동시에 호출하는 통합 테스트 필요

### 실행

```powershell
.\gradlew.bat test --tests "*PaymentServiceImplTest"
```

### 질문

**Q15-1.** `complete_AlreadyPaid`에서 "PortOne을 다시 호출하지 않았다"는 걸 어떻게 확인할까?

<details>
<summary>정답 보기</summary>

`verify(restTemplate, never()).exchange(...)` 또는 `verifyNoInteractions(restTemplate)`로 가짜 RestTemplate이 호출되지 않았음을 확인한다.

</details>

**Q15-2.** 이 단위 테스트가 전부 통과해도 `FOR UPDATE`가 빠진 버그는 잡을 수 없다. 왜?

<details>
<summary>정답 보기</summary>

`paymentMapper`가 가짜(Mock)라서 SQL이 실행되지 않는다. `findByPaymentIdForUpdate`는 `given`으로 정해 둔 객체를 돌려줄 뿐, 실제 잠금이 걸리는지는 알 수 없다. 이걸 확인하려면 실제 DB를 붙인 통합 테스트에서 두 스레드로 동시에 호출해 봐야 한다.

</details>

---

## 16. 알려진 한계와 개선 과제

| # | 문제 | 영향 | 개선 방향 |
|---|---|---|---|
| 1 | 상태를 문자열 `"READY"`, `"PAID"`로 다룬다 | 오타(`"PAYD"`)를 컴파일러가 못 잡음 | `PaymentStatus` enum |
| 2 | PortOne `CANCELLED` 상태를 처리하지 않는다 | 콘솔에서 취소하거나 10장 불일치 상황이면 영원히 READY | ③ 분기 전에 CANCELLED 처리 추가 |
| 3 | `Transaction.Cancelled` 웹훅을 무시한다 | 위와 같음 | 이벤트 처리 추가 |
| 4 | 웹훅 서명 검증이 없다 | 누구나 호출 가능 → 우리 서버가 PortOne API를 불필요하게 호출 (재조회 덕분에 데이터는 안전) | PortOne 웹훅 시크릿으로 서명 검증 |
| 5 | 웹훅의 `storeId`를 확인하지 않는다 | 다른 상점 웹훅도 처리 시도 | 우리 storeId와 비교 |
| 6 | DB엔 있는데 PortOne엔 없는 주문의 웹훅 → 404 | PortOne이 재전송 반복 | 웹훅에서는 404도 조용히 처리 |
| 7 | UPDATE 반환값(영향받은 행 수)을 확인하지 않는다 | 0행이어도 PAID 응답 | `if (updated != 1)` 검사 |
| 8 | 금액 상한이 없다 | `totalAmount: 999999999`도 /prepare 통과 | `@Max` 추가 |
| 9 | 결제 상태 조회 GET API가 없다 | 프론트가 READY 이후 다시 확인할 방법이 /complete 재호출뿐 | `GET /payments/{paymentId}` |
| 10 | READY도 400 | 프론트가 400 body를 읽어야 함 | 비즈니스 결과는 200 + status |
| 11 | 오래된 READY가 쌓인다 | 결제창만 열고 닫은 주문이 계속 READY | 스케줄러로 N시간 지난 READY 정리 |
| 12 | 결제 테이블 `ON DELETE CASCADE` | 회원 탈퇴 시 결제 기록 삭제 | 결제 기록은 보관 (소프트 삭제) |
| 13 | `schema.sql`에 DROP + `sql.init.mode: always` | 운영 서버 재시작 시 DB 초기화 | 운영에서는 `never` |
