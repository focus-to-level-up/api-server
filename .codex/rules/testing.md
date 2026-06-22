# Testing Rules

## 목적

변경 사항을 검증하는 기준과 기록 방식을 정의한다.

## 기본 원칙

- 변경 위험도에 맞는 테스트를 작성하거나 수동 검증 시나리오를 남긴다.
- 실패하는 테스트를 이유 없이 삭제하지 않는다.
- 테스트를 실행하지 못하면 이유를 `PROGRESS.md`에 기록한다.
- 결제, 보상, 랭킹, 배치, 관리자 수동 지급은 해피 패스만으로 완료하지 않는다.

## 권장 검증 명령

```bash
./gradlew test
./gradlew clean build
./gradlew :levelup-application:api:test
./gradlew :levelup-application:admin:test
./gradlew :levelup-application:batch:test
```

## 검증 관점

- 성공 케이스
- 인증 실패
- 권한 실패
- 대상 없음
- 중복 요청
- 동시 요청
- KST 기준 날짜 경계
- Batch 재실행
- webhook 중복 전달

