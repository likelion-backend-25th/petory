# 1. 시스템 아키텍처 설계서 (사자그램 SNS)

## 목차
- [1. 시스템 아키텍처 설계서 (사자그램 SNS)](#1-시스템-아키텍처-설계서-사자그램-sns)
- [1.1 시스템 아키텍처 개요](#11-시스템-아키텍처-개요)
- [1.2 AWS 인프라 및 배포 아키텍처](#12-aws-인프라-및-배포-아키텍처)
- [1.3 결제/구독 및 데이터 흐름도](#13-결제구독-및-데이터-흐름도)

---

## 1.1 시스템 아키텍처 개요

사자그램 플랫폼은 클라이언트와 서버가 물리적으로 완전히 분리된 계층형 REST API 아키텍처 채택.

```mermaid
graph TD
    Client["React SPA 클라이언트 (브라우저)"]
    CloudFront["AWS CloudFront (CDN)"]
    S3_Static["AWS S3 정적 웹 호스팅"]
    S3_Media["AWS S3 미디어 버킷 (/uploads)"]
    EC2["AWS EC2 (Ubuntu 24.04)"]
    Docker_App["Spring Boot API 서버 (컨테이너)"]
    Docker_DB["MySQL 9.x 데이터베이스 (컨테이너)"]
    PG_Gateway["결제/구독 게이트웨이 (PG)"]

    Client -->|정적 리소스 요청| CloudFront
    CloudFront -->|오리진 페치| S3_Static
    Client -->|이미지 스트리밍/조회| CloudFront
    CloudFront -->|미디어 파일 페치| S3_Media

    Client -->|REST API 요청 (JSON / JWT)| EC2
    EC2 --> Docker_App
    Docker_App -->|MyBatis 쿼리| Docker_DB
    Docker_App -->|이미지 파일 직접 업로드| S3_Media

    Client -->|결제창 SDK 호출| PG_Gateway
    PG_Gateway -->|결제 결과 응답| Client
    Client -->|결제 사후검증 요청| Docker_App
    Docker_App -->|REST API 검증 및 빌링키 요청| PG_Gateway
    PG_Gateway -->|비동기 웹훅 Webhook| Docker_App
```

---

## 1.2 AWS 인프라 및 배포 아키텍처

### 1.2.1 프론트엔드 호스팅 (AWS S3 + CloudFront)
- 정적 사이트 빌드 배포:
  - React 애플리케이션을 빌드한 정적 결과물(HTML, JS, CSS, Asset)을 AWS S3 버킷에 업로드.
  - S3 버킷 앞단에 AWS CloudFront를 구성하여 SSL/TLS(HTTPS) 인증서 적용 및 글로벌 엣지 캐싱 제공.
  - SPA 특성에 맞춰 라우팅 경로 새로고침 시 404 에러 대신 index.html을 반환하도록 CloudFront 사용자 정의 오류 응답(Error Response) 200 설정.

### 1.2.2 백엔드 및 데이터베이스 배포 (AWS EC2 + Docker Compose)
- 백엔드 컨테이너 환경:
  - AWS EC2 t3.medium 인스턴스에 Docker 및 Docker Compose 구성.
  - Spring Boot API 서버 애플리케이션 컨테이너(8080 포트)와 MySQL 9.x 데이터베이스 컨테이너(3306 포트) 내부 도커 네트워크 격리 연동.
  - 호스트 80/443 포트로 유입되는 API 트래픽을 컨테이너 8080 포트로 포워딩.

### 1.2.3 미디어 스토리지 및 파일 업로드 (AWS S3)
- 이미지 업로드 파이프라인:
  - 회원이 피드 이미지나 프로필 이미지를 등록할 때, 프론트엔드의 Multipart/form-data 요청을 Spring Boot 서버가 수신.
  - Spring Boot 서버는 AWS SDK for Java 2.x를 사용하여 S3 버킷의 `uploads/posts/` 및 `uploads/profiles/` 경로에 고유 UUID 파일명으로 안전하게 업로드.
  - 업로드 완료 후 생성된 S3 퍼블릭 객체 URL 또는 CloudFront 미디어 배포 URL을 DB의 `image_url` 컬럼에 영속화.

### 1.2.4 도메인 간 리소스 공유 (CORS 정책)
- React 클라이언트(CloudFront 도메인)와 Spring Boot API 서버(EC2 도메인) 간 통신을 위해 Spring Security WebConfig에 CORS 정책 적용.
- 허용 오리진: 프론트엔드 CloudFront 배포 도메인 및 로컬 개발 주소
- 허용 헤더: Authorization, Content-Type, X-Requested-With
- 노출 헤더: Authorization, Location
- 허용 메서드: GET, POST, PUT, PATCH, DELETE, OPTIONS

---

## 1.3 결제/구독 및 데이터 흐름도

VIP 멤버십 구독 및 단건 결제 위변조를 차단하기 위한 3단계 결제 검증 흐름.

### 1.3.1 단건 결제 및 사후 검증 흐름
1. 결제 준비 (클라이언트 -> 백엔드): 클라이언트가 결제 요청 전 서버에 `POST /api/v1/payments/prepare`를 호출하여 주문 번호(merchant_uid)와 결제 예정 금액 등록.
2. 결제창 호출 (클라이언트 -> 결제 게이트웨이): 브라우저 결제창 SDK를 실행하여 결제 모듈 창을 띄우고 사용자가 카드 결제 완료.
3. 결제 완료 통보 (결제 게이트웨이 -> 클라이언트): 결제 모듈이 브라우저 콜백으로 결제 승인 고유 식별자(imp_uid)와 주문 번호(merchant_uid) 반환.
4. 사후 검증 및 저장 (클라이언트 -> 백엔드): 클라이언트가 백엔드 `POST /api/v1/payments/complete`로 imp_uid를 전송. 백엔드는 결제사 REST API 서버로 직접 결제 내역을 단건 조회하여 실제 결제된 금액과 DB의 예정 금액이 일치하는지 위변조를 확인한 뒤 결제 완료(PAID) 상태로 갱신.

### 1.3.2 VIP 정기 구독 빌링키 및 자동 결제 흐름
1. 빌링키 발급: 유료 회원이 카드 정보를 입력하면 결제 게이트웨이로부터 재사용 가능한 빌링키(customer_uid) 발급.
2. 구독 정보 저장: 백엔드 subscription 테이블에 회원 ID, 빌링키, 다음 결제 예정일, VIP 상태 기록.
3. 정기 결제 배치 실행: Spring Boot의 스케줄러(Scheduler)가 매일 자정에 실행되어 당일 결제 대상 회원들의 빌링키를 이용해 결제사 비인증 결제 API 호출.
4. 정기 결제 갱신 및 처리: 결제 성공 시 VIP 만료일을 1개월 연장하고, 결제 실패 시 재시도 큐에 등록하거나 구독을 일시 중지 상태로 변경.
