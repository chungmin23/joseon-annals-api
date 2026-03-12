# 조선실록톡 - Spring Boot API

> RAG 기술과 하이브리드 검색을 적용한 조선시대 페르소나 챗봇 프로젝트

조선왕조실록 데이터를 기반으로 조선시대 역사 인물(왕)과 대화할 수 있는 AI 챗봇 서비스의 백엔드 API 서버입니다.
FastAPI ML 서버와 연동하여 RAG(Retrieval-Augmented Generation) + LLM 응답을 실시간 SSE 스트리밍으로 제공합니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.10 |
| Build | Gradle |
| DB (RDB) | PostgreSQL |
| DB (NoSQL) | AWS DynamoDB (채팅 메시지) |
| Cache | Caffeine (인메모리) |
| Auth | JWT (JJWT 0.12.6), BCrypt, Google OAuth 2.0 |
| Streaming | SSE (SseEmitter) + Spring WebFlux (WebClient) |
| ML 연동 | FastAPI (RAG + LangChain + OpenAI) |
| 결제 | Polar |
| Email | Gmail SMTP |
| API 문서 | Springdoc OpenAPI (Swagger UI) |

---

## 시스템 아키텍처

```
┌─────────────────┐
│  React (Web)    │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│  Spring Boot    │  ← 이 저장소
│  API (8080)     │
└────┬─────┬──────┘
     │     │
     ▼     ▼
┌──────────┐  ┌──────────────┐
│PostgreSQL│  │  FastAPI     │
│+DynamoDB │  │  ML (8000)   │
└──────────┘  └──────┬───────┘
                     ▼
              ┌──────────────┐
              │   OpenAI API │
              └──────────────┘
```

### 레이어 구조

```
Presentation  │ Controller, DTO
Service       │ *Service (@Service)
Logic         │ *Finder, *Reader, *Manager, *Handler (@Component)
Data Access   │ *Repository, *Client (@Repository / @Component)
```

- 참조 방향은 위 → 아래 단방향만 허용
- `@Transactional`은 Logic Layer에만 적용
- Logic Layer에서 반환 시 JPA Entity 직접 노출 금지 → 도메인 객체 변환 후 반환

---

## 프로젝트 구조

```
src/main/java/com/spring/ai/joseonannalapi/
├── api/
│   ├── controller/v1/          # REST 컨트롤러 (Auth, Chat, Persona, User, Content)
│   ├── controller/v1/dto/      # Request / Response DTO
│   └── support/                # @LoginUser, UserArgumentResolver
├── service/                    # 비즈니스 오케스트레이션
├── domain/                     # 도메인 로직 (Finder, Manager, Handler 등)
├── storage/                    # JPA 엔티티, Repository, 외부 Client
│   ├── client/                 # FastApiChatClient, GoogleOAuthClient
│   └── dynamo/                 # DynamoDB 메시지 클라이언트
├── config/                     # JWT, CORS, Cache, Tomcat, WebClient 등
└── common/                     # ApiResponse, 예외 클래스
```

---

## API 엔드포인트

| 도메인 | Method | 경로 | 인증 | 설명 |
|---|---|---|---|---|
| **Auth** | POST | `/api/v1/auth/signup` | ✗ | 이메일 회원가입 |
| | POST | `/api/v1/auth/login` | ✗ | 이메일 로그인 |
| | POST | `/api/v1/auth/refresh` | ✗ | 토큰 갱신 |
| | POST | `/api/v1/auth/logout` | ✗ | 로그아웃 |
| | POST | `/api/v1/auth/google` | ✗ | 구글 OAuth 로그인 |
| | POST | `/api/v1/auth/forgot-password` | ✗ | 임시 비밀번호 발송 |
| | PUT | `/api/v1/auth/change-password` | ✓ | 비밀번호 변경 |
| **Persona** | GET | `/api/v1/personas` | ✗ | 페르소나 목록 (`?era=`) |
| | GET | `/api/v1/personas/{id}` | ✗ | 페르소나 상세 |
| | GET | `/api/v1/personas/recommend` | ✓ | 관심사 기반 추천 |
| | GET | `/api/v1/personas/daily` | ✓ | 일간 추천 |
| **Chat** | POST | `/api/v1/chat/rooms` | ✓ | 채팅방 생성 |
| | GET | `/api/v1/chat/rooms` | ✓ | 채팅방 목록 |
| | GET | `/api/v1/chat/rooms/{id}` | ✓ | 채팅방 상세 |
| | GET | `/api/v1/chat/rooms/{id}/messages` | ✓ | 메시지 목록 |
| | POST | `/api/v1/chat/rooms/{id}/messages` | ✓ | 메시지 전송 (동기) |
| | POST | `/api/v1/chat/rooms/{id}/messages/stream` | ✓ | 메시지 전송 (SSE 스트리밍) |
| | GET | `/api/v1/chat/daily-usage` | ✓ | 일일 사용량 조회 |
| **User** | GET | `/api/v1/users/me` | ✓ | 내 정보 조회 |
| | PUT | `/api/v1/users/me/interests` | ✓ | 관심사 업데이트 |
| | GET | `/api/v1/users/me/stats` | ✓ | 채팅 통계 |
| **Content** | GET | `/api/v1/contents/recommend/{personaId}` | ✓ | 페르소나별 콘텐츠 추천 |
| | POST | `/api/v1/contents/library` | ✓ | 라이브러리 저장 |
| | GET | `/api/v1/contents/library` | ✓ | 라이브러리 목록 |
| | DELETE | `/api/v1/contents/library/{id}` | ✓ | 라이브러리 삭제 |
| | GET | `/api/v1/contents/rooms/{roomId}/recommendations` | ✓ | 룸별 실시간 추천 |

> Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## 인증 방식

이 서버는 **내부 네트워크** 환경을 가정합니다.
외부 Gateway에서 인증이 완료된 요청이 `X-User-Id` 헤더에 사용자 ID를 담아 전달되며, `UserArgumentResolver`가 이를 `@LoginUser User` 파라미터로 주입합니다.

Auth API (`/api/v1/auth/*`)는 Gateway 연계 목적으로 존재하며, 이 서버 자체에서 JWT 검증은 수행하지 않습니다.

---

## SSE 스트리밍 구조

채팅 메시지 스트리밍 엔드포인트는 FastAPI의 SSE 응답을 그대로 클라이언트에 중계합니다.

```
클라이언트 ← SseEmitter ← ChatService(Flux) ← FastAPI(/api/chat/stream)

이벤트 타입:
  {"type": "token",  "content": "경이"}          → 토큰 실시간 전달
  {"type": "done",   "keywords": [...], "sources": [...]}  → 완료, DB 저장 트리거
  {"type": "saved",  "messageId": "..."}          → 저장 완료 알림
```

---

## 환경 변수

`application-local.yml` 또는 환경 변수로 설정합니다.

```yaml
# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/joseon
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=

# JWT
JWT_SECRET=your-secret-key-here

# FastAPI ML 서버
FASTAPI_BASE_URL=http://localhost:8000

# Google OAuth
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# AWS DynamoDB
AWS_REGION=ap-northeast-2
DYNAMODB_ENDPOINT=          # 로컬 DynamoDB 사용 시 (예: http://localhost:8001)

# Gmail SMTP
GMAIL_USERNAME=
GMAIL_APP_PASSWORD=

# Polar 결제
POLAR_WEBHOOK_SECRET=
POLAR_API_KEY=
POLAR_PRODUCT_PRICE_ID=
POLAR_SUCCESS_URL=
POLAR_ORGANIZATION_ID=
```

---

## 로컬 실행

### 사전 요구사항

- Java 17
- PostgreSQL 실행 중 (`joseon` 데이터베이스 생성 필요)
- FastAPI ML 서버 실행 중 (`http://localhost:8000`)
- AWS DynamoDB Local 또는 실제 AWS 자격증명

### 실행

```bash
# 환경 변수 설정 후
./gradlew bootRun --args='--spring.profiles.active=local'
```

또는 `application-local.yml`에 직접 값을 입력 후:

```bash
./gradlew bootRun
```

### 빌드

```bash
./gradlew build
java -jar build/libs/joseon-annal-api-0.0.1-SNAPSHOT.jar
```

### 테스트

```bash
./gradlew test
```

---

## 주요 설계 결정

| 항목 | 결정 | 이유 |
|---|---|---|
| 메시지 저장소 | DynamoDB | 채팅 메시지는 쓰기/읽기가 많고 스키마 유연성이 필요 |
| SSE 구현 | `SseEmitter` (Spring MVC) | `Flux<String>` 반환은 Spring MVC Tomcat 환경에서 즉시 flush 보장 안됨 |
| Tomcat TCP_NODELAY | `true` | Nagle 알고리즘이 SSE 토큰 즉시 전송을 방해함 |
| RefreshToken | DB 저장, 유저당 1개 | 탈취 시 서버에서 즉시 무효화 가능 |
| 콘텐츠 추천 | 사전 큐레이션 DB | 실시간 외부 API 비용 없이 안정적인 추천 제공 |
| 룸별 추천 캐시 | 인메모리 (`RoomRecommendationStore`) | 대화 키워드 기반 실시간 갱신, 영속성 불필요 |

---

## 문서

```
docs/
├── PRD.md                          # 기능 요구사항
├── spring.md                       # 시스템 설계 PRD
├── DEVELOPMENT_PLAN.md             # 개발 계획
├── API_SPEC.md                     # API 명세 (Swagger 샘플 포함)
├── DB_SPEC.md                      # DB 스키마
├── api-spec/                       # 기능별 AI-SPEC 명세서
│   ├── AI-SPEC-AUTH-LOGIN.md
│   ├── AI-SPEC-AUTH-TOKEN.md
│   ├── AI-SPEC-AUTH-GOOGLE.md
│   ├── AI-SPEC-AUTH-PASSWORD.md
│   ├── AI-SPEC-CHAT-ROOM.md
│   ├── AI-SPEC-CHAT-MESSAGE.md
│   ├── AI-SPEC-PERSONA.md
│   ├── AI-SPEC-USER.md
│   └── AI-SPEC-CONTENT.md
└── error_history/                  # 장애 회고 보고서
    └── 20260225-joseon-annal-api-SSE-buffering.md
```
