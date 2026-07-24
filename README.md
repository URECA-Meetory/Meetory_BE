# Meetory Backend

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![SpringSecurity](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Wrapper-02303A?logo=gradle&logoColor=white)

> Meetory의 REST API 서버입니다. 회원/인증, 모임(팀) 매칭, 모임 관리, 게시판, 쪽지 기능의 비즈니스 로직과 데이터 영속성을 담당합니다.
>
> 전체 서비스 소개와 Frontend 연동 구조는 [Organization README](./README-organization.md)를, 화면/UI 관련 내용은 [Frontend README](./README-frontend.md)를 참고해 주세요.

---

## 목차

- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [실행 방법](#실행-방법)
- [API 명세](#api-명세)
- [공통 응답 / 예외 처리](#공통-응답--예외-처리)
- [인증 & 보안](#인증--보안)
- [도메인 모델](#도메인-모델)
- [테스트](#테스트)
- [DB 마이그레이션](#db-마이그레이션)

---

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1, Spring MVC(Web MVC), Spring Data JPA |
| Security | Spring Security, JWT (`jjwt` 0.13) |
| DB | MySQL 8 (JPA/Hibernate) |
| Build | Gradle (wrapper 포함) |
| Test | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| 기타 | Lombok, Bean Validation(Jakarta Validation) |

---

## 프로젝트 구조

```
src/main/java/com/meetory
│
├── MeetoryApplication.java
│
├── auth            # 회원가입 / 로그인 / 로그아웃 / JWT
│   ├── controller   AuthController
│   ├── dto          LoginRequest, LoginResponse, SignupRequest, EmailCheckResponse
│   ├── entity        TokenBlacklist
│   ├── jwt           JwtTokenProvider, JwtAuthenticationFilter
│   ├── repository    TokenBlacklistRepository
│   └── service        AuthService
│
├── user            # 회원 프로필 / 온보딩
│   ├── controller   UserController
│   ├── dto          ProfileResponse, ProfileUpdateRequest, OnboardingRequest,
│   │                 PasswordUpdateRequest, AccountDeleteRequest
│   ├── entity        User, Role
│   ├── repository    UserRepository
│   └── UserService.java
│
├── team            # 모임(팀) 매칭 / 신청 / 관리
│   ├── controller   TeamController
│   ├── dto          TeamListResponse, TeamDetailResponse, TeamCreateRequest,
│   │                 TeamApplyResponse, TeamApplicationResponse, TeamMemberResponse,
│   │                 MyTeamResponse
│   ├── entity        Team, TeamCategory, TeamStatus
│   ├── repository    TeamRepository
│   └── service        TeamService
│
├── member          # Team - User 신청/가입 관계(조인 엔티티)
│   ├── entity        Member, MemberStatus
│   └── repository    MemberRepository
│
├── message         # 모임장 문의(쪽지)
│   ├── controller   MessageController
│   ├── dto          InquiryRequest, ReplyRequest, InboxResponse,
│   │                 ThreadSummaryResponse, ThreadDetailResponse, MessageItemResponse
│   ├── entity        MessageThread, MessageItem
│   ├── repository    MessageThreadRepository, MessageItemRepository
│   └── service        MessageService
│
├── board           # 게시판
│   ├── controller   BoardController
│   ├── dto          BoardCreateRequest, BoardUpdateRequest, BoardResponse, BoardDetailResponse
│   ├── entity        Board
│   ├── repository    BoardRepository
│   └── service        BoardService
│
├── common          # 공통 응답 / 예외 처리
│   ├── ApiResponse.java
│   └── exception     CustomException, ErrorCode, GlobalExceptionHandler
│
└── config          # Security / CORS / 예외 핸들러
    ├── SecurityConfig.java
    ├── JsonAuthEntryPoint.java
    └── JsonAccessDeniedHandler.java
```

```
src/main/resources
├── application.properties
├── db/schema.sql                          # 초기 스키마
├── db/migration/V2__add_user_onboarding.sql
├── db/migration/V3__replace_onboarding_profile_fields.sql
├── db/migration/V4__add_board_table.sql
├── db/seed/board_test_data.sql
└── static/team-test.html, board.html      # API 수동 테스트용 정적 페이지
```

---

## 실행 방법

### 1) 사전 준비

- JDK 21
- MySQL 8 (로컬 또는 원격)

`src/main/resources/application.properties`에서 DB 접속 정보와 JWT 시크릿을 설정합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/meetorydb?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=ureca
spring.datasource.password=ureca

spring.jpa.hibernate.ddl-auto=validate   # 스키마는 db/schema.sql, db/migration/*.sql 로 직접 관리
jwt.secret=meetory-secret-key-must-be-at-least-32-bytes-long-for-hs256

server.port=8080
cors.allowed-origins=http://localhost:8080,http://localhost:5173,http://localhost:3000
```

> `spring.jpa.hibernate.ddl-auto=validate` 이므로, 최초 실행 전 `db/schema.sql`과 `db/migration/*.sql`을 순서대로 적용해 테이블을 미리 생성해야 합니다.

### 2) 서버 실행

```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

기본 주소: `http://localhost:8080`

### 3) 수동 테스트용 정적 페이지

- 모임 매칭 API 테스트: `http://localhost:8080/team-test.html`
- 게시판 API 테스트: `http://localhost:8080/board.html`
- PowerShell 스크립트: `scripts/test-board-api.ps1`

---

## API 명세

모든 응답은 `ApiResponse<T> = { success, message, data }` 형태이며, 인증(🔒)이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

### 인증 (`/api/auth`)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 이메일 중복 확인 | GET | `/api/auth/check-email?email=` | - |
| 회원가입 | POST | `/api/auth/signup` | - |
| 로그인 | POST | `/api/auth/login` | - |
| 로그아웃 | POST | `/api/auth/logout` | 🔒 |

### 회원 프로필 (`/api/users/me`)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 내 프로필 조회 | GET | `/api/users/me` | 🔒 |
| 닉네임 변경 | PATCH | `/api/users/me` | 🔒 |
| 온보딩 완료(나이/성별/관심사 저장) | PUT | `/api/users/me/onboarding` | 🔒 |
| 온보딩 건너뛰기 | POST | `/api/users/me/onboarding/skip` | 🔒 |
| 비밀번호 변경 | PUT | `/api/users/me/password` | 🔒 |
| 회원 탈퇴 | DELETE | `/api/users/me` | 🔒 |

### 모임 매칭 (`/api/teams`)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 모임 목록 조회 | GET | `/api/teams` | - |
| 내가 속한 모임 목록 | GET | `/api/teams/my` | 🔒 |
| 모임 상세 조회 | GET | `/api/teams/{teamId}` | - |
| 모임 개설 | POST | `/api/teams` | 🔒 |
| 모임 신청 | POST | `/api/teams/{teamId}/apply` | 🔒 |
| 모임 탈퇴 | DELETE | `/api/teams/{teamId}/leave` | 🔒 |
| 팀원(승인된 멤버) 목록 | GET | `/api/teams/{teamId}/members` | 🔒 |
| 대기중 신청 목록(리더 전용) | GET | `/api/teams/{teamId}/applications` | 🔒 |
| 신청 수락(리더 전용) | POST | `/api/teams/{teamId}/applications/{memberId}/approve` | 🔒 |
| 신청 거절(리더 전용) | POST | `/api/teams/{teamId}/applications/{memberId}/reject` | 🔒 |

### 쪽지 / 문의하기 (`/api/messages`)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 모임 리더에게 문의(최초 쪽지) | POST | `/api/messages/teams/{teamId}/inquiry` | 🔒 |
| 대화방 답장 | POST | `/api/messages/threads/{threadId}/reply` | 🔒 |
| 내 쪽지함(안읽음/읽음) | GET | `/api/messages/threads` | 🔒 |
| 쪽지(대화방) 상세 조회(읽음 처리 포함) | GET | `/api/messages/threads/{threadId}` | 🔒 |

### 게시판 (`/api/boards`)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 게시글 작성 | POST | `/api/boards` | 🔒 |
| 게시글 목록 조회 | GET | `/api/boards` | - |
| 게시글 상세 조회 | GET | `/api/boards/{boardId}` | - |
| 게시글 수정(작성자만) | PUT | `/api/boards/{boardId}` | 🔒 |
| 게시글 삭제(작성자만) | DELETE | `/api/boards/{boardId}` | 🔒 |

---

## 공통 응답 / 예외 처리

### `ApiResponse<T>`

```json
{
  "success": true,
  "message": "성공",
  "data": { }
}
```

- 성공: `ApiResponse.success(data)` / `ApiResponse.success(message, data)`
- 실패: `ApiResponse.fail(message)`

### 예외 처리 흐름

- 비즈니스 예외는 `CustomException(ErrorCode)` 형태로 던지며, `GlobalExceptionHandler`가 `ErrorCode`에 정의된 HTTP 상태 코드와 메시지로 일괄 변환합니다.
- Bean Validation 실패(`MethodArgumentNotValidException`)는 첫 번째 필드 오류 메시지를 담아 `400 Bad Request`로 응답합니다.
- 인증 실패(`401`) / 권한 없음(`403`) 은 `JsonAuthEntryPoint`, `JsonAccessDeniedHandler`가 JSON 형태로 일관되게 응답합니다.

---

### 주요 `ErrorCode`

| 코드 | 상태 | 메시지 |
|---|---|---|
| `DUPLICATE_EMAIL` | 409 | 이미 가입된 이메일입니다 |
| `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호가 일치하지 않습니다 |
| `INVALID_TOKEN` / `ALEADY_LOGGED_OUT` | 401 | 유효하지 않은 토큰 / 이미 로그아웃된 토큰 |
| `TEAM_NOT_FOUND` / `SELF_APPLY_NOT_ALLOWED` / `TEAM_NOT_RECRUITING` / `ALREADY_APPLIED` / `TEAM_FULL` | 404/403/409 | 모임 관련 예외 |
| `NOT_TEAM_LEADER` / `NOT_TEAM_MEMBER` / `LEADER_CANNOT_LEAVE` | 403 | 모임 권한 관련 예외 |
| `THREAD_NOT_FOUND` / `NOT_THREAD_PARTICIPANT` / `SELF_INQUIRY_NOT_ALLOWED` | 404/403 | 쪽지 관련 예외 |
| `BOARD_NOT_FOUND` / `FORBIDDEN_ACTION` | 404/403 | 게시판 관련 예외 |
| `INVALID_PASSWORD` / `SAME_AS_OLD_PASSWORD` | 401/400 | 비밀번호 변경 관련 예외 |

전체 목록은 `common/exception/ErrorCode.java`에서 확인할 수 있습니다.

---

## 인증 & 보안

- **JWT 발급/검증**: `JwtTokenProvider`가 로그인 시 `userId`, `role`을 담은 Access Token을 발급하고 서명/만료를 검증합니다.
- **인증 필터**: `JwtAuthenticationFilter`가 매 요청마다 `Authorization: Bearer {token}` 헤더를 읽어 토큰이 유효하고 블랙리스트에 없는 경우 `SecurityContext`에 인증 정보를 설정합니다.
- **로그아웃**: 로그아웃 시 토큰을 DB(`token_blacklist`)에 저장해 만료 전이라도 재사용을 차단합니다.
- **인가 정책**(`SecurityConfig`):
  - `permitAll`: `/api/auth/**`, 정적 리소스, `/api/teams/**`(GET, 목록/상세/멤버 조회 등 일부 제외), `/api/boards/**`(GET)
  - `authenticated`: `/api/users/me/**`, `/api/teams/my`(GET), `/api/teams/{id}/applications`(GET), `/api/teams/{id}/members`(GET), 그 외 쓰기 작업 전반
- **CORS**: `application.properties`의 `cors.allowed-origins` 값을 기반으로 허용 Origin을 구성합니다.
- **비밀번호 저장**: `BCryptPasswordEncoder`로 해시하여 저장합니다.

---

## 도메인 모델

| 엔티티 | 설명 |
|---|---|
| `User` | 회원 정보(이메일/비밀번호/닉네임/역할). 온보딩 정보(`age`, `gender`, `hobbies`, `onboardingCompleted`) 포함 |
| `Team` | 모임(팀). 제목/카테고리/소개/정원/리더/모집상태(`모집중`, `모집완료`, `종료`) |
| `Member` | `Team`-`User` 간 신청/가입 관계. 상태(`대기`, `승인`, `거절`), `(team_id, user_id)` 유니크 제약 |
| `MessageThread` / `MessageItem` | 모임 리더 문의를 위한 1:1 대화방과 메시지(말풍선) |
| `Board` | 게시판 글(작성자/제목/본문/생성·수정일) |
| `TokenBlacklist` | 로그아웃된 JWT 토큰 저장소 |

---

## 테스트

`src/test/java` 하위에 서비스 레이어 단위 테스트가 구성되어 있습니다 (Mockito 기반).

- `AuthServiceTest` — 회원가입/로그인/로그아웃
- `TeamServiceTest` — 모임 개설/조회/신청/승인·거절/탈퇴
- `MessageServiceTest` — 문의 전송/답장/쪽지함 조회/읽음 처리
- `BoardServiceTest` — 게시글 CRUD
- `UserServiceTest` — 프로필/온보딩/비밀번호/탈퇴

```bash
./gradlew test
```

---

## DB 마이그레이션

`db/schema.sql`을 베이스 스키마로 하고, 이후 변경 사항은 `db/migration/` 아래에 순차적으로 추가합니다.

| 파일 | 내용 |
|---|---|
| `schema.sql` | 초기 테이블 생성 |
| `V2__add_user_onboarding.sql` | 온보딩 관련 컬럼 추가 |
| `V3__replace_onboarding_profile_fields.sql` | 온보딩 프로필 필드 재정의 |
| `V4__add_board_table.sql` | 게시판(`board`) 테이블 추가 |
| `seed/board_test_data.sql` | 게시판 테스트용 시드 데이터 |

`spring.jpa.hibernate.ddl-auto=validate`로 설정되어 있으므로, 애플리케이션 실행 전 위 스크립트를 순서대로 DB에 반영해야 합니다.
