# Multi Module Rules

## 목적

Gradle 멀티모듈 구조에서 모듈 책임과 의존성 방향을 유지하기 위한 규칙을 정의한다.

## 계층

- `levelup-application`
  - 실행 가능한 애플리케이션 계층
  - `api`, `admin`, `batch`

- `levelup-domain`
  - 핵심 도메인 모델과 규칙

- `levelup-infra`
  - MySQL, Redis, 외부 API client 등 기술 구현

- `levelup-common`
  - 특정 도메인이나 기술에 종속되지 않는 공통 코드

## 의존성 원칙

- API/Admin/Batch는 서로 직접 의존하지 않는다.
- `common`은 다른 프로젝트 모듈에 의존하지 않는다.
- `domain`은 application 모듈에 의존하지 않는다.
- application 모듈은 필요한 공유 모듈만 선택해 의존한다.
- 의존성 순환이 생기면 구현을 멈추고 구조를 재검토한다.

## 빌드 설정 원칙

실행 애플리케이션 모듈:

- `bootJar = true`
- `jar = false`

라이브러리 모듈:

- `bootJar = false`
- `jar = true`

## 공유 모듈 사용 기준

- 두 개 이상의 애플리케이션 모듈에서 실제로 필요하면 공유 모듈 이동을 검토한다.
- 특정 API 흐름에만 필요한 코드는 API 모듈에 둔다.
- JWT 인증/인가는 현재 API 요청 흐름에 결합되어 있으므로 `levelup-application:api`에 둔다.
- 특정 Admin 흐름에만 필요한 코드는 Admin 모듈에 둔다.
- 특정 Batch 흐름에만 필요한 코드는 Batch 모듈에 둔다.
