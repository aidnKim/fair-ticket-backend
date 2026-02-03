# Fair Ticket: 공정한 티켓 예매 시스템

> **좌석 선점 경쟁에서의 데이터 무결성과 동시성 제어를 최우선으로 고려한 티켓 예매 플랫폼입니다.**

<br>

## 프로젝트 소개
**공정한 기회를 보장하는 콘서트 티켓 예매 시스템**입니다.
동시다발적인 좌석 선점 요청에서도 **데이터의 정합성(Data Integrity)** 을 보장하고, 포트원(PortOne) 결제 연동을 통해 **안전한 결제 프로세스**를 제공하는 백엔드 시스템을 구축하는 데 집중했습니다.

* **개발 기간:** 2025.01 ~ (진행 중, MVP)
* **개발 인원:** 1인 (풀스택)
* **배포 주소:** [서비스 바로가기 (Click)](https://www.fairticket.store)
* **Frontend Repository:** [Github Link](https://github.com/aidnKim/fair-ticket-frontend)

<br>

## 기술 스택 (Tech Stack)

| 구분 | 기술 |
| :-- | :-- |
| **Backend** | Java 17, Spring Boot 3.5, Spring Data JPA, Spring Security |
| **Cache/Queue** | Redis (Redisson), Redis 
| **Database** | Oracle Cloud (Production) |
| **Payment** | PortOne 결제 API 연동 |
| **DevOps** | Docker, Docker Compose, Nginx (Reverse Proxy), Let's Encrypt (HTTPS) |
| **Auth** | JWT (JSON Web Token) |

<br>

## 시스템 아키텍처 (Architecture)
Docker Compose를 활용하여 Backend, Frontend, Redis, Certbot(SSL 인증서) 컨테이너를 구성했습니다. Nginx를 Reverse Proxy로 두어 HTTPS 통신과 보안을 강화했으며, Oracle Cloud Autonomous Database를 통해 안정적인 데이터 저장소를 구축했습니다.

<img width="1437" height="1082" alt="Image" src="https://github.com/user-attachments/assets/ad6c4247-f7a7-4ffe-b67a-6a005451a7e9" />

<br>

## 핵심 기술적 성과 (Troubleshooting)

### 1. 좌석 동시 선점 문제 해결 (비관적 락 적용)
* **문제:** 동시에 여러 사용자가 같은 좌석을 선점하려고 할 때, **Race Condition**으로 인해 하나의 좌석이 중복 예약되는 문제 발생 가능성.
* **해결:** JPA Repository에 **`@Lock(LockModeType.PESSIMISTIC_WRITE)`** 을 적용하여 좌석 조회 시점에 **비관적 락(Pessimistic Lock)** 을 획득. 트랜잭션이 끝날 때까지 다른 트랜잭션의 접근을 차단.
* **결과:** 동시 요청 상황에서도 **좌석 중복 예약 0%**를 보장하는 안정적인 시스템 구현.

### 2. 데이터 무결성 보장을 위한 트랜잭션 관리
* **문제:** 예약 생성, 좌석 상태 변경, 잔여 좌석 수 감소 등 여러 작업이 하나의 단위로 처리되어야 하는데, 중간에 오류 발생 시 데이터 불일치 위험 존재.
* **해결:** `ReservationService`와 `PaymentService`의 핵심 로직에 **`@Transactional`** 어노테이션을 적용.
* **결과:** 예외 발생 시 전체 로직이 **Rollback**되도록 처리하여, 어떤 상황에서도 **데이터 정합성**을 보장하는 원자성(Atomicity) 확보.

### 3. 예약 만료 자동화 (Scheduler)
* **문제:** 결제를 완료하지 않은 예약이 좌석을 무한히 점유하여 다른 사용자의 예매 기회를 박탈.
* **해결:** Spring `@Scheduled` 어노테이션을 활용하여 주기적으로 만료된 예약(PENDING 상태 & 만료 시간 경과)을 일괄 취소하고 좌석을 해제하는 **Scheduler** 구현.
* **결과:** 사용자 개입 없이 **5분 경과 시 자동으로 좌석 해제**, 공정한 예매 기회 보장.

### 4. 결제 검증 실패 시 서버 측 자동 환불 처리
* **문제:** 프론트엔드에서 결제 성공 후 백엔드 검증 과정에서 실패할 경우, 사용자 브라우저 종료나 네트워크 끊김으로 **환불 요청이 누락**되어 돈만 빠져나가는 위험 존재.
* **해결:** 검증과 취소의 책임을 **서버로 일원화**. `PaymentService`에서 검증 실패 감지 시 **즉시 포트원 API를 호출**하여 자동 환불 처리.
* **결과:** 네트워크 불안정 상황에서도 **환불 누락 0%**, 통신 횟수 2회 → 1회로 단축, Fail-Safe 설계 적용.
* 👉 [상세 과정 보기](./docs/troubleshooting-01-payment-verification.md)

### 5. YAML 8진수 파싱으로 인한 포트원 API 인증 실패 해결
* **문제:** 포트원 API Key를 application.yml에 설정했으나 **401 Unauthorized** 에러 지속. 하드코딩 시에만 정상 동작.
* **해결:** `0`으로 시작하는 값이 **YAML 스펙에 의해 8진수로 자동 변환**되는 문제 발견. 따옴표(`"`)로 감싸 **문자열로 명시적 선언**.
* **결과:** API Key가 정확히 로드되어 **포트원 인증 정상 동작**. API Key, 전화번호 등 0으로 시작하는 값 설정 시 주의사항 학습.
* 👉 [상세 과정 보기](./docs/troubleshooting-02-yaml-parsing-issue.md)

### 6. Redis 캐시 직렬화 오류 해결
* **문제:** @Cacheable 적용 시 LocalDateTime 필드에서 SerializationException 발생
* **해결:** RedisConfig에 JavaTimeModule 등록, GenericJackson2JsonRedisSerializer에 커스텀 ObjectMapper 적용
* **결과:** DTO에 개별 어노테이션 없이 전역 설정으로 JSON 직렬화 정상 동작

### 7. Redis 카운터 기반 실시간 잔여석 관리
* **문제:** 캐시 적용 시 잔여석이 실시간 반영되지 않는 문제
* **해결:** 캐시 대상(공연 정보)과 실시간 데이터(잔여석)를 분리, Redis Atomic Counter로 잔여석 관리
* **결과:** 캐시 성능 유지하면서 잔여석 실시간 반영

### 8. Redis 기반 대기열 시스템 구축
* **문제:** 인기 공연 오픈 시 동시 접속 폭주로 서버 과부하 우려
* **해결:** Redis Sorted Set으로 대기열 관리, Scheduler로 주기적 입장 허용, Filter로 API 접근 제어
* **결과:** 서버 안정성 확보, 순차적 입장으로 공정한 예매 기회 보장

<br>

## ERD (Database Design)
공연, 스케줄, 좌석, 예약, 결제, 사용자 데이터가 유기적으로 연결되도록 정규화된 모델링을 수행했습니다.


<a href="https://dbdiagram.io/d/FairTicket-6977834abd82f5fce2a3c8f0" target="_blank">
  <img width="1485" height="517" alt="Image" src="https://github.com/user-attachments/assets/e6839b51-297a-4418-b5d0-abf51a1b3044" />
</a>
<br>

> 👆 **위 이미지를 클릭**하면 상세 컬럼과 관계를 확인할 수 있습니다.


<br>

## 주요 기능 (Key Features)
* **공연/스케줄 관리:** 공연 정보, 일정별 회차, 좌석 등급 및 가격 관리
* **좌석 예매:** 실시간 좌석 현황 조회, 비관적 락 기반 좌석 선점, 5분 임시 예약 시스템
* **결제 시스템:** 포트원(아임포트) 연동을 통한 실제 결제 처리 및 검증
* **결제 취소/환불:** 사용자 요청에 따른 결제 취소 및 좌석 자동 해제
* **사용자 인증:** JWT 기반 로그인/회원가입, Spring Security 연동

<br>

## 로드맵 (Roadmap)

현재 MVP 단계이며, 다음 기능들을 순차적으로 개발 예정입니다.

| 단계 | 기능 | 목표 |
| :--: | :-- | :-- |
| **1** | ~~**Redis 캐시 도입**~~ ✅ | 캐시 적용, 대기열 시스템 구축 완료 |
| **2** | **Kafka 메시지 큐 도입** | 대기열 고도화, 분산 환경 지원 |
| **3** | **AI 매크로 탐지 시스템** | 사용자 행동 패턴 분석을 통한 비정상 접근(매크로) 실시간 차단 |

> 🎯 **최종 목표:** AI 기반의 Fair-Guard 시스템으로 모든 사용자에게 공정한 예매 기회를 보장하는 플랫폼

<br>

## API 엔드포인트 (주요)
| Method | Endpoint | Description |
| :--: | :-- | :-- |
| `GET` | `/api/v1/concerts` | 전체 공연 목록 조회 |
| `GET` | `/api/v1/concerts/{id}` | 공연 상세 조회 (스케줄 포함) |
| `GET` | `/api/v1/seats/{scheduleId}` | 특정 회차의 좌석 현황 조회 |
| `POST` | `/api/v1/reservations` | 좌석 예약 (임시 선점) |
| `POST` | `/api/v1/payments` | 결제 처리 (포트원 연동) |
| `DELETE` | `/api/v1/reservations/{id}` | 예약 취소 (환불 포함) |
| `POST` | `/api/v1/users/signup` | 회원가입 |
| `POST` | `/api/v1/users/login` | 로그인 (JWT 발급) |
| `POST` | `/api/v1/queue/{scheduleId}` | 대기열 등록 |
| `GET` | `/api/v1/queue/{scheduleId}/position` | 대기 순번 조회 |

<br>

## 실행 방법 (How to Run)
```bash
# 1. 저장소 복제
git clone [https://github.com/aidnKim/fair-ticket-backend.git](https://github.com/aidnKim/fair-ticket-backend.git)

# 2. 환경 변수 설정 (application.properties 또는 docker-compose.yml)
#    - DB 연결 정보
#    - JWT_SECRET
#    - PORTONE_API_KEY / PORTONE_API_SECRET

# 3. Docker Compose로 실행
docker-compose up -d --build