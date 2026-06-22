# Skill: add-api-endpoint

## 목적

모바일 앱에서 호출하는 API를 추가하거나 수정할 때 따르는 절차다.

## 사용 시점

- 새로운 API endpoint 추가
- 기존 API request/response 변경
- API service 로직 변경
- 인증/인가가 필요한 사용자 기능 변경

## 절차

1. `docs/harness/PRD.md` 또는 기능별 PRD에서 요구사항을 확인한다.
2. 기존 Controller, Request, Response, Service 패턴을 찾는다.
3. 인증/인가 요구사항을 확인한다.
4. 성공 응답과 실패 응답을 정의한다.
5. 중복 요청, 동시 요청, 대상 없음 케이스를 검토한다.
6. 필요하면 `integrity-reviewer` 또는 `security-reviewer` 관점으로 검토한다.
7. 테스트 또는 수동 검증 시나리오를 작성한다.
8. 변경 사항과 검증 결과를 `docs/harness/PROGRESS.md`에 기록한다.

## 완료 기준

- API 호출자가 기대하는 응답이 명확하다.
- 실패 케이스가 정의되어 있다.
- 검증 결과가 기록되어 있다.

