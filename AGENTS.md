# 집중하면 레벨업 서버 작업 지침

## 프로젝트 요약

- Spring Boot 기반 집중하면 레벨업 서버
- Java 17, Spring Boot 3.3.5, Gradle 멀티모듈, MySQL, Redis, Spring Batch
- API/Admin/Batch를 독립 실행 가능한 애플리케이션 모듈로 분리
- Domain/Infra/Common은 공유 라이브러리 모듈로 사용
- 챗봇/RAG는 현재 레포가 아니라 별도 서비스 후보로 둠

## 문서 구조

- `AGENTS.md`: 모든 AI 작업자가 먼저 읽는 입구 문서
- `.codex/agents`: 역할별 에이전트 정의
- `.codex/rules`: 코딩, 테스트, 안전, 정합성 규칙
- `.codex/skills`: 반복 작업 체크리스트
- `docs/harness/PRD.md`: 제품 요구사항
- `docs/harness/PROGRESS.md`: 현재 진행 상황
- `docs/harness/ARCHITECTURE.md`: 서버 구조
- `docs/harness/DECISIONS.md`: 의사결정 기록

## 모듈 경계

- `levelup-application:api`: 모바일 앱 API, API 인증/JWT
- `levelup-application:admin`: 운영자/CS 기능
- `levelup-application:batch`: 정산/랭킹/통계 배치
- `levelup-domain`: 핵심 도메인 모델과 규칙
- `levelup-infra:mysql`: MySQL/JPA 인프라
- `levelup-infra:redis`: Redis, 캐시, 락
- `levelup-infra:client`: 외부 API 클라이언트
- `levelup-common`: 공통 예외, 응답, 유틸리티

## 작업 시작 순서

1. `AGENTS.md`를 읽는다.
2. `docs/harness/PROGRESS.md`에서 현재 상태를 확인한다.
3. `docs/harness/PRD.md`에서 제품 요구사항을 확인한다.
4. 구조 변경이면 `docs/harness/ARCHITECTURE.md`와 `docs/harness/DECISIONS.md`를 확인한다.
5. 작업 성격에 맞는 `.codex/agents` 문서를 선택한다.
6. 완료 후 `PROGRESS.md`에 변경 사항과 검증 결과를 기록한다.

## 에이전트 운영 원칙

- 한 작업의 주 구현자는 한 명만 둔다.
- 같은 파일을 여러 에이전트가 동시에 수정하지 않는다.
- API/Admin/Batch 구현자는 직접 코드를 수정할 수 있다.
- Integrity/Architecture/QA/DevOps reviewer는 기본적으로 리뷰와 체크를 담당한다.
- 공유 모듈 수정 시 영향 범위를 먼저 확인한다.
- 분산락, 멱등성, 결제, 보상, 랭킹, 배치 정산은 integrity-reviewer 관점 검토를 거친다.

## 자주 쓰는 명령

```bash
./gradlew clean build
./gradlew test
./gradlew :levelup-application:api:test
./gradlew :levelup-application:admin:test
./gradlew :levelup-application:batch:test
./gradlew :levelup-application:api:bootRun
./gradlew :levelup-application:admin:bootRun
./gradlew :levelup-application:batch:bootRun
```

## 금지 사항

- 비밀 값 커밋 금지
- 운영 설정 임의 변경 금지
- 실패 테스트 무단 삭제 금지
- 사용자 요청 없는 대량 리팩터링 금지
- 결제/보상/랭킹 산식 추측 변경 금지
