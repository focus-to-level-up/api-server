# Progress

마지막 갱신: 2026-06-08

## 현재 상태

### 멀티모듈

- Gradle 기반 계층형 멀티모듈 구조가 존재한다.
  - `levelup-application`
    - 실제 실행 가능한 애플리케이션 계층이다.
    - `api`, `admin`, `batch`가 이 계층에 속한다.
  - `levelup-domain`
    - 핵심 도메인 모델과 비즈니스 규칙을 담는 계층이다.
    - 애플리케이션 모듈들이 공통으로 사용하는 도메인 중심 모듈이다.
  - `levelup-infra`
    - 외부 기술과 연결되는 인프라 계층이다.
    - `mysql`, `redis`, `client`가 이 계층에 속한다.
  - `levelup-common`
    - 공통 응답, 예외, 유틸리티처럼 특정 도메인이나 기술에 종속되지 않는 코드를 담는 계층이다.

- 애플리케이션 모듈은 독립 실행 단위로 분리하는 방향이다.
  - `levelup-application:api`
    - 모바일 앱에서 호출하는 사용자 API 서버
  - `levelup-application:admin`
    - 운영자/CS 기능을 제공하는 관리자 서버
  - `levelup-application:batch`
    - 랭킹, 보상, 통계 등 시간 기반 작업을 수행하는 배치 서버

- 공유 모듈은 애플리케이션 모듈이 필요한 만큼 의존한다.
  - `levelup-domain`
    - 엔티티와 도메인 규칙
  - `levelup-infra:mysql`
    - MySQL/JPA 기반 영속성 인프라
  - `levelup-infra:redis`
    - Redis 기반 캐시, 락, 임시 데이터 인프라
  - `levelup-infra:client`
    - 외부 API 클라이언트 인프라
  - `levelup-common`
    - 공통 예외, 응답, 유틸리티

- JWT 인증/인가 구현은 `levelup-infra:jwt` 모듈로 분리하지 않고 `levelup-application:api` 내부에 유지한다.

### 워크플로우

- 루트 `AGENTS.md`가 작성되었다.
  - 모든 AI 작업자가 먼저 읽는 입구 문서다.
  - 작업 시작 순서, 모듈 경계, 에이전트 운영 원칙, 금지 사항을 정의한다.

- `.codex` 기반 하네스 디렉터리가 생성되었다.
  - `.codex/agents`
    - 역할별 에이전트 정의를 둘 위치
  - `.codex/rules`
    - 코딩, 테스트, 안전, 정합성 규칙을 둘 위치
  - `.codex/skills`
    - 반복 작업 체크리스트를 둘 위치

- `docs/harness` 디렉터리가 생성되었다.
  - `PROGRESS.md`
    - 현재 진행 상황과 다음 작업을 기록한다.
  - `PRD.md`
    - Spring Boot 서버 리뉴얼 요구사항을 정리한다.
  - `ARCHITECTURE.md`
    - 멀티모듈 구조, 의존성 방향, 현재/목표 인프라 구조를 정리한다.
  - `DECISIONS.md`
    - 주요 의사결정과 이유를 기록한다.

### Notion

- Notion에는 집중하면 레벨업의 제품 기획 자료가 존재한다.
  - 초기 기획안
  - 화면별 기능 정리
  - 관리자 페이지 요구사항
  - BM, 랭킹, 길드, 통계, 광고 관련 요구사항

- 서버 작업 시 Notion 기획은 요구사항의 출처로 활용한다.
  - 단, 구현 전에는 Spring Boot 서버 범위로 다시 정리한다.
  - 앱 UI, 캐릭터 디자인, 마케팅 아이디어는 서버 작업 범위와 분리한다.

### 인프라와 자동화

- Docker Compose 파일이 존재한다.
  - `docker-compose.yml`
  - `docker-compose-dev.yml`
  - `docker-compose-prod.yml`

- GitHub Actions 워크플로우가 존재한다.
  - 개발/운영 CI/CD 워크플로우
  - ECS 배포 관련 워크플로우
  - 템플릿 워크플로우

- ECS 관련 워크플로우는 현재 사용하지 않는다.
  - 향후 ECS 또는 유사한 배포 전략을 다시 검토할 때 참고하기 위해 남겨둔 레거시다.
  - 현재 운영/개발 자동화 문서에서는 활성 워크플로우와 레거시 워크플로우를 구분해서 다룬다.

## 완료

- 하네스 문서 구조 방향 합의
- 구현 담당과 리뷰어 담당 분리 방향 합의
- `AGENTS.md` 작성
- `docs/harness/PRD.md` 작성
- `docs/harness/ARCHITECTURE.md` 작성
- `docs/harness/DECISIONS.md` 작성
- `.codex/agents` 역할별 에이전트 문서 작성
- `.codex/rules` 작업 규칙 문서 작성
- `.codex/skills` 반복 작업 체크리스트 문서 작성
- `.codex/agents`, `.codex/rules`, `.codex/skills`, `docs/harness` 디렉터리 생성
- `levelup-infra:jwt` 제거 방향을 문서와 Gradle 의존성에 반영
- API 모듈 기준 멀티모듈 컴파일 오류 정리
- API 리소스 구조를 `src/main/resources` 기준으로 복구
- 마스터 데이터 SQL을 `src/main/resources/db/data/*_data.sql` 구조로 정리
- 로컬 실행용 `MASTER_DATA_PATH`를 `classpath:db/data/*_data.sql` 기준으로 변경
- API 테스트 기본 디렉터리와 컨텍스트 로딩 테스트 골격 추가
- 멀티모듈 `bootRun`에서 루트 `.env`를 찾을 수 있도록 API 시작 로직 보완
- Redis 캐시 클라이언트 인터페이스와 구현체를 `levelup-infra:redis` 내부로 정리
- API 내 FCM 스케줄러는 제거하고 Batch 모듈에서 이후 구체화할 대상으로 이동
- API 모듈 내 와일드카드 import 제거
- API 모듈에서 Redis/ShedLock 기술 의존성을 직접 사용하지 않는지 점검

## 다음 작업 후보

### 에이전트 문서 보완

- `.codex/agents/api-engineer.md`
- `.codex/agents/admin-engineer.md`
- `.codex/agents/batch-engineer.md`
- `.codex/agents/integrity-reviewer.md`
- `.codex/agents/security-reviewer.md`
- `.codex/agents/qa-reviewer.md`
- `.codex/agents/devops-reviewer.md`
- `.codex/agents/product-strategist.md`

### 규칙 문서 보완

- `.codex/rules/coding.md`
- `.codex/rules/testing.md`
- `.codex/rules/safety.md`
- `.codex/rules/integrity.md`
- `.codex/rules/multi-module.md`

### 스킬 문서 보완

- `.codex/skills/add-api-endpoint.md`
- `.codex/skills/add-admin-feature.md`
- `.codex/skills/add-batch-job.md`
- `.codex/skills/review-transaction.md`
- `.codex/skills/update-harness-docs.md`

### 제품/아키텍처 문서 보완

- `docs/harness/PRD.md`
- `docs/harness/ARCHITECTURE.md`
- `docs/harness/DECISIONS.md`

### 멀티모듈 의존성 점검

- `settings.gradle`
- 루트 `build.gradle`
- 각 모듈 `build.gradle`
- `bootJar`/`jar` 설정
- 모듈 간 의존성 방향
- 의존성 순환 가능성
- 현재 우선순위는 API 모듈의 실행 안정화다.
- Admin 모듈은 Thymeleaf 기반 관리자 기능 구체화 전까지 추가 수정하지 않는다.
- Batch 모듈은 API 안정화 이후 독립 실행 구조와 스케줄러 책임을 다시 정리한다.

### API 모듈 안정화

- `ExceptionMapper` 기준 예외 응답 상태코드/메시지 정렬
- `ExceptionResponse.of(status, message)` 생성 방식 적용 범위 추가 점검
- `HttpResponseUtil`과 API-local 응답 구조 유지 여부 점검
- `MASTER_DATA_PATH`와 `db/data/*_data.sql` 기반 초기 데이터 로딩 순서 점검
- Spring Data Redis repository 스캔 경고의 원인과 영향 범위 확인
- macOS Netty DNS native dependency 경고의 처리 필요성 판단
- API 모듈 완료 후 커밋 전 변경 파일 범위를 점검한다.

### Batch/Admin 복구

- API 모듈 완료 후 Batch 모듈 독립 실행 구조를 복구한다.
- Batch 모듈의 FCM 스케줄러 책임과 Spring Batch 설정을 다시 정리한다.
- Batch 모듈 복구 후 Admin 모듈을 Thymeleaf 기반 목표 구조에 맞게 복구한다.
- Admin 모듈은 API 안정화 전까지 추가 수정하지 않는다.

### 배포 플로우

- API, Batch, Admin 3개 모듈이 모두 독립 실행 가능해진 뒤 배포 플로우를 설계한다.
- API는 기존 blue-green 배포 방식을 유지한다.
- Batch와 Admin은 API와 독립적으로 배포 가능하도록 분리한다.
- 현재 main 브랜치가 dev로 바로 배포되는 흐름은 별도 배포 설계 전까지 유의한다.

### FCM/Firebase 구조 분리 보류

- `FcmService`, `FcmScheduler`, `FirebaseService`는 외부 클라이언트 호출과 도메인 조회/알림 정책이 섞여 있어 즉시 infra로 옮기지 않는다.
- 향후 프론트/운영 흐름과 함께 푸시 알림 구조를 다시 볼 때 다음 기준으로 분리한다.
  - FCM 단순 전송 클라이언트는 `levelup-infra:client` 후보
  - 알림 대상 조회, 문구 결정, 스케줄링은 API 또는 Batch 애플리케이션 계층 후보
  - 테스트용 FCM API는 API-local 유지 후보

### 집중 시간 저장 안정화 설계

- `FocusService`, `FocusServiceV2`, `FocusServiceV3`, `FocusServiceV4` 차이 파악
- 멱등성 키 설계
- Redis 분산락 적용 지점 검토
- 중복 경험치/골드 지급 방지
- 실패/재시도 전략 정리

## 열린 질문

- API/Admin/Batch는 단일 레포 안에서 독립 실행만 보장할지, 배포 단위까지 완전히 분리할지?
- Admin은 Thymeleaf 기반 페이지를 유지할지, API만 제공하고 별도 프론트로 분리할지?
- 배치 기준 시각은 모든 주간 시스템에서 월요일 04:00 KST로 통일할지?
- `levelup-domain`에 JPA 엔티티와 도메인 규칙을 함께 둘지, 순수 도메인과 영속성 모델 분리를 더 진행할지?
- ECS 관련 워크플로우를 언제까지 레거시로 유지하고, 어떤 조건에서 삭제 또는 재활성화할지?

## 검증 기록

- `AGENTS.md` 파일 생성 확인
- `docs/harness/PRD.md` 파일 생성 확인
- `docs/harness/ARCHITECTURE.md` 파일 생성 확인
- `docs/harness/DECISIONS.md` 파일 생성 확인
- `.codex/agents` 역할별 에이전트 문서 생성 확인
- `.codex/rules` 작업 규칙 문서 생성 확인
- `.codex/skills` 반복 작업 체크리스트 문서 생성 확인
- `.codex/*`, `docs/harness` 디렉터리 생성 확인
- `AGENTS.md` 작업 시작 순서 1~4번 확인
  - `AGENTS.md` 확인
  - `docs/harness/PROGRESS.md` 확인
  - `docs/harness/PRD.md` 확인
  - `docs/harness/ARCHITECTURE.md`, `docs/harness/DECISIONS.md` 확인
- `sh gradlew :levelup-application:api:processResources :levelup-application:api:compileJava :levelup-application:api:testClasses` 성공
- `sh gradlew :levelup-application:api:compileJava :levelup-application:api:testClasses` 성공
- `sh gradlew :levelup-application:api:test` 성공
- `sh gradlew :levelup-application:api:bootRun --args='--server.port=18080'`로 API 기동 확인
- `curl -i http://127.0.0.1:18080/actuator/health` 결과 `HTTP/1.1 200`, `{"status":"UP"}` 확인
- API 모듈 내 `import ...*;` 검색 결과 없음
- API 모듈 내 직접 Redis/ShedLock 기술 사용 검색 결과 없음
- 샌드박스 내부 TCP 연결은 `Operation not permitted`로 제한되어, 헬스 체크는 승인된 샌드박스 외부 실행으로 확인했다.
- 전체 `clean build`와 Admin/Batch 테스트는 현재 범위가 API 안정화이므로 아직 실행하지 않았다.
