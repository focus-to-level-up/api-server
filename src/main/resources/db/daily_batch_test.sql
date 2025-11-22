-- -----------------------------------------------------
-- DailyJobBatch 테스트 데이터 초기화
-- -----------------------------------------------------
-- 주의: 카멜케이스 컬럼명 정책 반영 (socialId, isFocusing 등)
-- ID 컬럼은 @Column(name="...")에 따라 스네이크 케이스 사용

USE focus_db; -- (실제 사용하는 DB 이름으로 변경 확인)
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터 정리
TRUNCATE TABLE planners;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- 1. Members 생성 (총 10명)
-- -----------------------------------------------------
-- status: ACTIVE, RANKING_BANNED
-- isFocusing: true/false (1/0)

INSERT INTO members (
    member_id, socialType, socialId, nickname,
    status, isFocusing, currentLevel, currentExp,
    isPreRegistrationRewarded, isSubscriptionRewarded, isReceivedWeeklyReward,
    created_at, updated_at
) VALUES
-- [Scenario 1] 일반 유저 (아무 일도 안 일어남)
(1, 'KAKAO', 'social_1', 'User1_Normal', 'ACTIVE', 0, 1, 0, 0, 0, 0, NOW(), NOW()),
(2, 'KAKAO', 'social_2', 'User2_Normal', 'ACTIVE', 0, 1, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 2] 플래너 삭제 테스트 (Planner 데이터 보유)
(3, 'KAKAO', 'social_3', 'User3_Planner', 'ACTIVE', 0, 5, 100, 0, 0, 0, NOW(), NOW()),
(4, 'KAKAO', 'social_4', 'User4_Planner', 'ACTIVE', 0, 3, 50, 0, 0, 0, NOW(), NOW()),

-- [Scenario 3] 경고 만료 테스트 (4주 지남 -> 해제되어야 함)
(5, 'KAKAO', 'social_5', 'User5_WarnExp', 'ACTIVE', 0, 10, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 4] 경고 유지 테스트 (4주 안 지남 -> 유지되어야 함)
(6, 'KAKAO', 'social_6', 'User6_WarnKeep', 'ACTIVE', 0, 8, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 5] 집중 적발 테스트 (집중 켜짐 + 경고 없음 -> 경고 부여)
(7, 'KAKAO', 'social_7', 'User7_FocusOn', 'ACTIVE', 1, 2, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 6] 집중 적발 + 밴 테스트 (집중 켜짐 + 이미 경고 -> 랭킹 밴)
(8, 'KAKAO', 'social_8', 'User8_FocusBan', 'ACTIVE', 1, 4, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 7] 밴 복구 테스트 (밴 상태 + 2주 지남 -> ACTIVE 복구)
(9, 'KAKAO', 'social_9', 'User9_Restore', 'RANKING_BANNED', 0, 0, 0, 0, 0, 0, NOW(), NOW()),

-- [Scenario 8] 밴 유지 테스트 (밴 상태 + 2주 안 지남 -> 밴 유지)
(10,'KAKAO', 'social_10','User10_Banned', 'RANKING_BANNED', 0, 0, 0, 0, 0, 0, NOW(), NOW());


-- -----------------------------------------------------
-- 2. MemberInfos 생성 (필수 1:1 매핑)
-- -----------------------------------------------------
-- Entity 필드: age, gender, categoryMain, categorySub, belonging, totalLevel...
-- CamelCase 적용

INSERT INTO member_infos (
    member_info_id, member_id, age, gender, categoryMain, categorySub,
    totalLevel, totalExp, belonging, gold, diamond, trainingReward
) VALUES
      (1, 1, 20, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 1, 0, 'none', 0, 0, 0),
      (2, 2, 20, 'FEMALE', 'ADULT', 'OFFICE_WORKER', 1, 0, 'none', 0, 0, 0),
      (3, 3, 18, 'MALE', 'HIGH_SCHOOL', 'HIGH_3', 5, 0, 'none', 0, 0, 0),
      (4, 4, 18, 'FEMALE', 'HIGH_SCHOOL', 'HIGH_1', 3, 0, 'none', 0, 0, 0),
      (5, 5, 15, 'MALE', 'MIDDLE_SCHOOL', 'MIDDLE_3', 10, 0, 'none', 0, 0, 0),
      (6, 6, 15, 'FEMALE', 'MIDDLE_SCHOOL', 'MIDDLE_1', 8, 0, 'none', 0, 0, 0),
      (7, 7, 20, 'MALE', 'ADULT', 'JOB_SEEKER', 2, 0, 'none', 0, 0, 0),
      (8, 8, 20, 'FEMALE', 'ADULT', 'PUBLIC_SERVANT', 4, 0, 'none', 0, 0, 0),
      (9, 9, 20, 'MALE', 'ADULT', 'EXAM_TAKER', 0, 0, 'none', 0, 0, 0),
      (10, 10, 20, 'FEMALE', 'ADULT', 'GRADUATE_STUDENT', 0, 0, 'none', 0, 0, 0);



-- -----------------------------------------------------
-- 3. MemberSettings 생성 (시나리오별 데이터 세팅)
-- -----------------------------------------------------
-- CamelCase 적용: isRankingCaution, rankingWarningAt, isRankingActive 등

INSERT INTO member_settings (
    member_setting_id, member_id,
    alarmOn, isPomodoro, isAIPlanner, isSubscriptionMessageBlocked, totalStatColor,
    isRankingActive, isRankingCaution, rankingWarningAt, rankingDeactivatedCount
) VALUES
-- [1-4] 정상 유저들 (경고 없음)
(1, 1, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),
(2, 2, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),
(3, 3, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),
(4, 4, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),

-- [5] 경고 만료 테스트: 30일 전 경고 (28일 지남 -> 해제 대상)
(5, 5, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), 0),

-- [6] 경고 유지 테스트: 10일 전 경고 (28일 안 지남 -> 유지 대상)
(6, 6, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),

-- [7] 집중 적발 테스트: 집중ON, 경고 없음 -> (결과: 경고 ON, 날짜 갱신)
(7, 7, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),

-- [8] 집중 밴 테스트: 집중ON, 이미 경고(5일전) -> (결과: RankingBan, 밴 횟수 증가)
(8, 8, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 0),

-- [9] 밴 복구 테스트: 15일 전 경고(밴 시점) (2주 지남 -> 결과: Active 복구, 경고 해제)
(9, 9, 1, 0, 0, 0, 'FFFF00', 0, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 1),

-- [10] 밴 유지 테스트: 5일 전 경고(밴 시점) (2주 안 지남 -> 결과: 유지)
(10, 10, 1, 0, 0, 0, 'FFFF00', 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 1);


-- -----------------------------------------------------
-- 4. Planners 생성 (삭제 테스트용)
-- -----------------------------------------------------
-- 유저 3, 4번에게 플래너 데이터 생성
-- (Planner 엔티티 필드명에 맞춰 수정 필요, 여기선 CamelCase 가정)

# INSERT INTO planners (member_id, dailyGoalId, created_at, updated_at)
# VALUES
#     (3, NULL, NOW(), NOW()),
#     (3, NULL, NOW(), NOW()),
#     (4, NULL, NOW(), NOW());
#
# SET FOREIGN_KEY_CHECKS = 1;


-- --- 1. 만료된 우편 (삭제 대상) 5개 ---
-- 'expiredAt' 날짜가 '오늘' (2025-11-16)보다 이전입니다.
INSERT INTO mails (member_id, senderName, type, title, description, reward, isReceived, expiredAt, created_at, updated_at)
VALUES
    (1, '운영자', 'EVENT', '만료된 우편 1 (어제 만료)', '이 우편은 어제 만료되었습니다.', 100, 0, '2025-11-15', NOW(), NOW()),
    (1, '운영자', 'TIER_PROMOTION', '만료된 우편 2 (2일 전 만료)', '이 우편은 2일 전 만료되었습니다.', 0, 0, '2025-11-14', NOW(), NOW()),
    (1, '운영자', 'GUILD_WEEKLY', '만료된 우편 3 (3일 전 만료)', '이 우편은 3일 전 만료되었습니다.', 50, 1, '2025-11-13', NOW(), NOW()),
    (1, '운영자', 'EVENT', '만료된 우편 4 (4일 전 만료)', '이 우편은 4일 전 만료되었습니다.', 0, 0, '2025-11-12', NOW(), NOW()),
    (1, '운영자', 'COUPON', '만료된 우편 5 (5일 전 만료)', '이 우편은 5일 전 만료되었습니다.', 1000, 0, '2025-11-11', NOW(), NOW());

-- --- 2. 유효한 우편 (삭제 대상 아님) 5개 ---
-- 'expiredAt' 날짜가 '오늘' (2025-11-16)이거나 미래입니다.
INSERT INTO mails (member_id, senderName, type, title, description, reward, isReceived, expiredAt, created_at, updated_at)
VALUES
    (1, '운영자', 'TIER_PROMOTION', '유효한 우편 1 (오늘 만료)', '이 우편은 오늘까지 유효합니다.', 0, 0, '2025-11-16', NOW(), NOW()),
    (1, '운영자', 'EVENT', '유효한 우편 2 (내일 만료)', '이 우편은 내일 만료됩니다.', 0, 0, '2025-11-17', NOW(), NOW()),
    (1, '운영자', 'EVENT', '유효한 우편 3 (모레 만료)', '이 우편은 모레 만료됩니다.', 200, 0, '2025-11-18', NOW(), NOW()),
    (1, '운영자', 'COUPON', '유효한 우편 4 (3일 뒤 만료)', '이 우편은 3일 뒤 만료됩니다.', 0, 1, '2025-11-19', NOW(), NOW()),
    (1, '운영자', 'GUILD_WEEKLY', '유효한 우편 5 (4일 뒤 만료)', '이 우편은 4일 뒤 만료됩니다.', 30, 0, '2025-11-20', NOW(), NOW());
