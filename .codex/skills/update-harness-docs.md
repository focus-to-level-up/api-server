# Skill: update-harness-docs

## 목적

코드 또는 구조 변경 후 하네스 문서를 최신 상태로 유지하는 절차다.

## 사용 시점

- 기능 구현 완료
- 구조 변경
- 의사결정 변경
- 검증 결과 발생
- 열린 질문이 결정으로 바뀜

## 절차

1. `docs/harness/PROGRESS.md`에 변경 사항과 검증 결과를 기록한다.
2. 구조나 실행 단위가 바뀌면 `docs/harness/ARCHITECTURE.md`를 갱신한다.
3. 중요한 결정이 내려졌으면 `docs/harness/DECISIONS.md`에 날짜와 함께 기록한다.
4. 요구사항 범위가 바뀌면 `docs/harness/PRD.md`를 갱신한다.
5. 에이전트 역할이나 작업 규칙이 바뀌면 `.codex/agents` 또는 `.codex/rules`를 갱신한다.

## 완료 기준

- 문서가 현재 코드/운영 상태와 충돌하지 않는다.
- 실행하지 못한 검증이 명확히 기록되어 있다.
- 새로 생긴 열린 질문이 누락되지 않았다.

