# Architecture

마지막 갱신: 2026-05-28

## 목적

이 문서는 집중하면 레벨업 Spring Boot 서버의 멀티모듈 구조, 모듈별 책임, 의존성 방향, 실행 단위, 현재/목표 인프라 구조를 정리한다.

서버 리뉴얼의 핵심 목표는 API, Admin, Batch를 독립적으로 실행 가능한 애플리케이션 모듈로 분리하고, 공통 도메인/인프라/유틸리티 코드를 명확한 계층으로 관리하는 것이다.

## 전체 구조

### 계층형 멀티모듈

```text
focus-to-levelup-server
├── levelup-application
│   ├── api
│   ├── admin
│   └── batch
├── levelup-domain
├── levelup-infra
│   ├── mysql
│   ├── redis
│   └── client
└── levelup-common
```

### 계층 설명

- `levelup-application`
  - 실제 실행 가능한 애플리케이션 계층이다.
  - API, Admin, Batch 서버가 이 계층에 속한다.
  - 각 모듈은 독립 실행 가능한 Spring Boot 애플리케이션을 목표로 한다.

- `levelup-domain`
  - 핵심 도메인 모델과 비즈니스 규칙을 담는다.
  - 집중, 보상, 랭킹, 길드, 결제 등 서비스의 핵심 개념이 위치한다.
  - 특정 애플리케이션 모듈에 종속되지 않아야 한다.

- `levelup-infra`
  - 외부 기술과 연결되는 인프라 계층이다.
  - MySQL, Redis, 외부 API 클라이언트처럼 기술 구현을 담당한다.

- `levelup-common`
  - 공통 예외, 응답, 유틸리티처럼 특정 도메인이나 기술에 종속되지 않는 코드를 담는다.
  - 다른 프로젝트 모듈에 의존하지 않는 가장 낮은 공통 계층이다.

## 애플리케이션 모듈

### API

`levelup-application:api`는 모바일 앱에서 호출하는 사용자 API 서버다.

주요 책임:

- 로그인/회원가입
- 집중 시작, 저장, 종료
- 목표, 과목, Todo, 플래너
- 캐릭터, 아이템, 보상
- 랭킹, 길드
- 결제, 구독, 우편함
- 출석 체크, 쿠폰, 친구 초대 등 프로모션
- API 인증과 JWT 발급/검증

### Admin

`levelup-application:admin`은 운영자/CS 기능을 제공하는 관리자 서버다.

주요 책임:

- 유저 조회
- 유저 닉네임, 학교, 상태 메시지 수정
- 재화 및 패키지 지급
- 신고 확인
- 랭킹 경고 부여/취소
- 길드 정보 수정
- 운영 통계 조회

### Batch

`levelup-application:batch`는 시간 기반 작업을 수행하는 배치 서버다.

주요 책임:

- 주간 랭킹 정산
- 주간 보상 지급
- 통계 집계
- 만료 처리
- 반복성 데이터 보정 작업

## 공유 모듈

### Domain

`levelup-domain`은 핵심 모델과 규칙을 담는다.

포함할 수 있는 것:

- JPA Entity
- 도메인 Enum
- 도메인 상태 전이 규칙
- 도메인 예외
- 도메인 계산 로직

포함하지 않는 것:

- API/Admin/Batch 전용 DTO
- Controller 흐름에 종속된 로직
- 외부 기술 구현 세부사항

### Infra

`levelup-infra`는 기술 구현을 담당한다.

- `levelup-infra:mysql`
  - MySQL/JPA repository
  - DB 설정
  - 영속성 관련 구현

- `levelup-infra:redis`
  - Redis 설정
  - 캐시
  - 분산락
  - 임시 데이터 저장

- `levelup-infra:client`
  - 외부 API 클라이언트
  - 소셜 로그인, 결제, 기타 외부 연동

JWT 발급/검증은 현재 `levelup-application:api` 내부에 둔다. Spring Security 필터, 사용자 인증 객체, 토큰 발급 흐름이 API 요청 처리와 강하게 결합되어 있으므로 별도 infra 모듈로 분리하지 않는다.

### Common

`levelup-common`은 가장 낮은 수준의 공통 코드만 둔다.

포함할 수 있는 것:

- 공통 응답 형식
- 공통 예외 기반 클래스
- 범용 유틸리티
- 도메인에 종속되지 않는 상수

포함하지 않는 것:

- 비즈니스 규칙
- 특정 인프라 기술 코드
- 특정 애플리케이션 모듈 전용 코드

## 의존성 방향

### 기본 방향

```text
application
  -> domain
  -> infra
  -> common
```

실제 Gradle 의존성은 각 애플리케이션 모듈이 필요한 공유 모듈을 선택해서 가진다.

### 금지 방향

```text
api -> admin
admin -> api
batch -> api
batch -> admin
common -> domain
common -> infra
domain -> application
```

### 원칙

- API/Admin/Batch는 서로 직접 의존하지 않는다.
- 공통 도메인 규칙은 `levelup-domain`으로 이동한다.
- 공통 기술 구현은 `levelup-infra`로 이동한다.
- 공통 유틸리티는 `levelup-common`으로 이동한다.

## 빌드와 실행

### 실행 애플리케이션 모듈

다음 모듈은 실행 가능한 Spring Boot 애플리케이션이다.

- `levelup-application:api`
- `levelup-application:admin`
- `levelup-application:batch`

원칙:

- `bootJar = true`
- `jar = false`

### 라이브러리 모듈

다음 모듈은 다른 모듈에서 참조하는 라이브러리 모듈이다.

- `levelup-domain`
- `levelup-common`
- `levelup-infra:mysql`
- `levelup-infra:redis`
- `levelup-infra:client`

원칙:

- `bootJar = false`
- `jar = true`

## 현재 인프라 구조

### 실행 환경

현재 서버는 단일 인스턴스 중심으로 운영되는 구조다.

```text
single instance
├── api-blue
├── api-green
└── redis
```

### API 서버

- API 서버는 blue-green 배포 형태로 운영한다.
- `app-blue`, `app-green` 두 컨테이너가 번갈아 배포 대상이 된다.
- 외부 트래픽은 활성 컨테이너로 라우팅된다.
- API 서버는 사용자 요청을 직접 처리하므로 배포 중 다운타임을 최소화한다.

### Redis

- Redis는 현재 단일 인스턴스 내부에서 실행된다.
- 캐시, 임시 데이터, 분산락 후보 인프라로 사용한다.
- 향후 API/Admin/Batch가 독립 실행되더라도 동일 Redis를 공유할 수 있다.


### GitHub Actions

- 현재 개발/운영 배포 워크플로우가 존재한다.
- ECS 관련 워크플로우는 현재 사용하지 않는다.
- ECS 워크플로우는 향후 ECS 또는 유사 배포 전략 검토용 레거시로 남겨둔다.

## 목표 인프라 구조

### 목표 실행 단위

향후 목표는 API, Admin, Batch를 각각 독립 실행 단위로 운영하는 것이다.
이후 redis 또한 ElasticCache 등 외부 관리형 인프라로 분리할 수 있다.

```text
server environment
├── api-blue
├── api-green
├── admin
├── batch
└── redis
```

### API

- API 서버는 blue-green 배포를 유지한다.
- 사용자-facing 서버이므로 배포 중 다운타임을 최소화한다.
- 장애 대응과 롤백 우선순위가 가장 높다.

### Admin

- Admin 서버는 API 서버와 독립적으로 실행한다.
- 운영자/CS 기능이므로 API보다 낮은 가용성 요구사항을 가진다.
- 배포 시 짧은 다운타임을 감수할 수 있다.

### Batch

- Batch 서버는 API 서버와 독립적으로 실행한다.
- 정산, 랭킹, 통계 등 시간 기반 작업을 담당한다.
- 배포 시 짧은 다운타임을 감수할 수 있다.
- 단, 실행 중인 정산 작업이 중복되거나 중간에 깨지지 않도록 별도 제어가 필요하다.

## 향후 정리할 내용

- 실제 Gradle 의존성 점검 결과
- 모듈별 `bootJar`/`jar` 설정 검증
- API/Admin/Batch 실행 명령 검증
- Docker Compose 서비스 분리 방식
- 활성 GitHub Actions와 레거시 GitHub Actions 구분
- 현재 Nginx 또는 라우팅 방식
- Redis을 단일 인스턴스 내부에 둘지 외부 관리형 인프라로 분리할지
