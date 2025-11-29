-- -----------------------------------------------------
-- Focus Server: DailyJobBatch 통합 테스트 데이터
-- -----------------------------------------------------
USE focus_db;

SET FOREIGN_KEY_CHECKS = 0;

-- [Step 0] 기존 데이터 초기화
TRUNCATE TABLE mails;
TRUNCATE TABLE planners;
TRUNCATE TABLE daily_goals;
TRUNCATE TABLE subjects;
TRUNCATE TABLE rankings;
TRUNCATE TABLE leagues;
TRUNCATE TABLE seasons;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- [Prerequisite] 시즌 및 리그 데이터 (밴 복구 로직 필수)
-- -----------------------------------------------------
-- User 7 (복구 대상)은 'ADULT' 카테고리이므로, 들어갈 수 있는 진행 중인 리그가 있어야 함.

INSERT INTO seasons (season_id, name, start_date, end_date, created_at, updated_at)
VALUES (1, 'Test Season 2025', DATE_SUB(NOW(), INTERVAL 1 WEEK), DATE_ADD(NOW(), INTERVAL 3 WEEK), NOW(), NOW());

INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (1, 1, '성인 브론즈 리그', 'ADULT', 'BRONZE', 1, DATE_SUB(NOW(), INTERVAL 1 WEEK), DATE_ADD(NOW(), INTERVAL 3 WEEK), NOW(), NOW());


-- -----------------------------------------------------
-- [Members] 테스트 시나리오별 유저 생성 (총 8명)
-- -----------------------------------------------------
-- status: ACTIVE, RANKING_BANNED
-- is_focusing: 0(Off), 1(On)

INSERT INTO members (
    member_id, social_type, social_id, nickname, status, is_focusing,
    current_level, current_exp, highest_tier,
    created_at, updated_at
) VALUES
-- 1. [Normal] 아무 일도 없는 정상 유저
(1, 'KAKAO', 'user_1', 'User1_Clean', 'ACTIVE', 0, 1, 0, 'BRONZE', NOW(), NOW()),

-- 2. [ClearPlanner] 플래너/투두 삭제 테스트 대상
(2, 'KAKAO', 'user_2', 'User2_Planner', 'ACTIVE', 0, 1, 0, 'BRONZE', NOW(), NOW()),

-- 3. [RestoreWarning] 경고 만료 테스트 (30일 지남 -> 경고 해제되어야 함)
(3, 'KAKAO', 'user_3', 'User3_WarnExp', 'ACTIVE', 0, 5, 0, 'BRONZE', NOW(), NOW()),

-- 4. [KeepWarning] 경고 유지 테스트 (10일 지남 -> 경고 유지되어야 함)
(4, 'KAKAO', 'user_4', 'User4_WarnKeep', 'ACTIVE', 0, 3, 0, 'BRONZE', NOW(), NOW()),

-- 5. [CheckFocusing] 집중 적발 테스트 (집중 켜짐 + 경고 없음 -> 경고 부여)
(5, 'KAKAO', 'user_5', 'User5_Catch', 'ACTIVE', 1, 2, 0, 'BRONZE', NOW(), NOW()),

-- 6. [CheckFocusing -> Ban] 집중 적발 심화 (집중 켜짐 + 이미 경고 -> 랭킹 밴 & 데이터 삭제)
(6, 'KAKAO', 'user_6', 'User6_BanTarget', 'ACTIVE', 1, 4, 0, 'BRONZE', NOW(), NOW()),

-- 7. [RestoreExcludeRanking] 밴 복구 테스트 (밴 기간 만료 -> ACTIVE 복구 & 랭킹 재진입)
(7, 'KAKAO', 'user_7', 'User7_Restore', 'RANKING_BANNED', 0, 0, 0, 'BRONZE', NOW(), NOW()),

-- 8. [KeepBan] 밴 유지 테스트 (밴 기간 남음 -> 밴 유지)
(8, 'KAKAO', 'user_8', 'User8_StayBanned', 'RANKING_BANNED', 0, 0, 0, 'BRONZE', NOW(), NOW());


-- -----------------------------------------------------
-- [MemberInfos] 리그 배정을 위한 카테고리 정보
-- -----------------------------------------------------
-- User 7 복구를 위해 category_main = 'ADULT' 필수 (위의 League와 매칭)

INSERT INTO member_infos (
    member_info_id, member_id, age, gender, category_main, category_sub,
    total_level
) VALUES
      (1, 1, 20, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 1),
      (2, 2, 20, 'FEMALE', 'ADULT', 'OFFICE_WORKER', 1),
      (3, 3, 25, 'MALE', 'ADULT', 'JOB_SEEKER', 5),
      (4, 4, 25, 'FEMALE', 'ADULT', 'PUBLIC_SERVANT', 3),
      (5, 5, 22, 'MALE', 'ADULT', 'EXAM_TAKER', 2),
      (6, 6, 22, 'FEMALE', 'ADULT', 'GRADUATE_STUDENT', 4),
      (7, 7, 24, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 0), -- User 7: ADULT (복구 시 1번 리그로 감)
      (8, 8, 24, 'FEMALE', 'ADULT', 'OFFICE_WORKER', 0);


-- -----------------------------------------------------
-- [MemberSettings] 경고/밴 날짜 상태 설정
-- -----------------------------------------------------
-- is_ranking_caution: 경고 상태 (1=True)
-- ranking_warning_at: 경고 또는 밴을 당한 시점
-- is_ranking_active: 랭킹 참여 여부 (0이면 밴 상태거나 비활성)

INSERT INTO member_settings (
    member_setting_id, member_id,
    is_ranking_active, is_ranking_caution, ranking_warning_at, ranking_deactivated_count
) VALUES
-- [1, 2] 정상
(1, 1, 1, 0, NULL, 0),
(2, 2, 1, 0, NULL, 0),

-- [3] 경고 만료 대상 (30일 전 경고 -> 해제)
(3, 3, 1, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), 0),

-- [4] 경고 유지 대상 (10일 전 경고 -> 유지)
(4, 4, 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),

-- [5] 집중 적발 대상 (현재 정상, 곧 경고 받음)
(5, 5, 1, 0, NULL, 0),

-- [6] 밴 처리 대상 (이미 경고 상태(5일전) + 현재 집중 중 -> 밴 처리)
(6, 6, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 0),

-- [7] 밴 복구 대상 (15일 전 밴 당함 -> 2주 지났으므로 복구)
-- is_ranking_active = 0 (밴 상태)
(7, 7, 0, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 1),

-- [8] 밴 유지 대상 (5일 전 밴 당함 -> 아직 복구 안됨)
(8, 8, 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 1);


-- -----------------------------------------------------
-- [Rankings] 현재 랭킹 테이블 상태
-- -----------------------------------------------------
-- User 7, 8 (밴 당한 유저)은 랭킹 테이블에 없어야 함.
-- User 6 (곧 밴 당할 유저)은 현재는 존재함.

INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at)
VALUES
    (1, 1, 1, 'BRONZE', NOW(), NOW()),
    (2, 1, 2, 'BRONZE', NOW(), NOW()),
    (3, 1, 3, 'BRONZE', NOW(), NOW()),
    (4, 1, 4, 'BRONZE', NOW(), NOW()),
    (5, 1, 5, 'BRONZE', NOW(), NOW()),
    (6, 1, 6, 'BRONZE', NOW(), NOW()); -- Step 5 실행 후 삭제될 예정


-- -----------------------------------------------------
-- [Planner & DailyGoals] Step 1: 플래너 삭제 테스트
-- -----------------------------------------------------
-- User 2에게 '어제' 날짜의 플래너와 목표를 생성
-- Batch가 돌면 삭제되어야 함.

INSERT INTO subjects (subject_id, member_id, name, color, created_at, updated_at)
VALUES (1, 2, '코딩 테스트', '#FF0000', NOW(), NOW());

INSERT INTO daily_goals (daily_goal_id, member_id, daily_goal_date, target_minutes, is_received, reward_multiplier, created_at, updated_at)
VALUES (1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 60, 0, 1.0, NOW(), NOW());

INSERT INTO planners (planner_id, daily_goal_id, subject_id, start_time, end_time, created_at, updated_at)
VALUES (1, 1, 1, '10:00:00', '12:00:00', NOW(), NOW());


-- -----------------------------------------------------
-- [Mails] Step 2: 만료된 우편 삭제 테스트
-- -----------------------------------------------------
INSERT INTO mails (
    mail_id, member_id, sender_name, type, title, description,
    is_received, expired_at, created_at, updated_at
) VALUES
-- [삭제 대상] 만료일이 어제인 우편
(1, 1, 'Admin', 'EVENT', '만료된 보상', '기간 만료', 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW()),
-- [삭제 대상] 만료일이 3일 전인 우편
(2, 1, 'Admin', 'CHARACTER_SELECTION_TICKET', '오래된 공지', '기간 만료', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NOW(), NOW()),

-- [유지 대상] 만료일이 내일인 우편
(3, 1, 'Admin', 'EVENT', '유효한 보상', '기간 남음', 0, DATE_ADD(CURDATE(), INTERVAL 1 DAY), NOW(), NOW()),
-- [유지 대상] 만료일이 오늘(자정까지)인 우편
(4, 1, 'Admin', 'CHARACTER_SELECTION_TICKET', '오늘 만료', '아직 유효', 0, CURDATE(), NOW(), NOW());


SET FOREIGN_KEY_CHECKS = 1;
