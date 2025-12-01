USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- [Step 0] 기존 데이터 정리 (순서 중요)
TRUNCATE TABLE weekly_stats;
TRUNCATE TABLE weekly_subject_stats;
TRUNCATE TABLE mails;
TRUNCATE TABLE planners;
TRUNCATE TABLE daily_goals;
TRUNCATE TABLE daily_subjects;
TRUNCATE TABLE subjects;
TRUNCATE TABLE rankings;
TRUNCATE TABLE leagues;
TRUNCATE TABLE seasons;
TRUNCATE TABLE member_characters;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- [Step 2] Seasons & Leagues (지난주/이번주)
-- -----------------------------------------------------
INSERT INTO seasons (season_id, name, start_date, end_date, created_at, updated_at)
VALUES (1, 'Season 2025-Nov', DATE_SUB(NOW(), INTERVAL 2 WEEK), DATE_ADD(NOW(), INTERVAL 2 WEEK), NOW(), NOW());

-- 지난주 (Ended)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES
    (10, 1, '지난주 브론즈', 'ADULT', 'BRONZE', 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
    (11, 1, '지난주 실버', 'ADULT', 'SILVER', 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW());

-- 이번주 (Started)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES
    (20, 1, '이번주 브론즈', 'ADULT', 'BRONZE', 2, NOW(), DATE_ADD(NOW(), INTERVAL 6 DAY), NOW(), NOW()),
    (21, 1, '이번주 실버', 'ADULT', 'SILVER', 2, NOW(), DATE_ADD(NOW(), INTERVAL 6 DAY), NOW(), NOW());


-- -----------------------------------------------------
-- [Step 3] Members (5 Users)
-- -----------------------------------------------------
INSERT INTO members (
    member_id, social_type, social_id, nickname, status,
    current_level, current_exp, highest_tier,
    is_received_weekly_reward, created_at, updated_at
) VALUES
      (1, 'KAKAO', 'u1', 'User1_Promote', 'ACTIVE', 5, 0, 'BRONZE', 0, NOW(), NOW()),
      (2, 'KAKAO', 'u2', 'User2_Stay', 'ACTIVE', 3, 0, 'BRONZE', 0, NOW(), NOW()),
      (3, 'KAKAO', 'u3', 'User3_Demote', 'ACTIVE', 4, 0, 'SILVER', 0, NOW(), NOW()),
      (4, 'KAKAO', 'u4', 'User4_Newbie', 'ACTIVE', 1, 0, NULL, 0, NOW(), NOW()),
      (5, 'KAKAO', 'u5', 'User5_Reset', 'ACTIVE', 10, 999, 'BRONZE', 0, NOW(), NOW());

-- -----------------------------------------------------
-- [Step 3] MemberAssets (핵심 수정)
-- 각 유저마다 '기본 프로필 이미지(Asset ID=1)'와 '기본 테두리(Asset ID=4)' 소유권 생성
-- Asset ID는 data_assets.sql 기준: 1=양동동1이미지, 4=양동동테두리 라고 가정
-- -----------------------------------------------------

-- User 1의 에셋 (ID: 10, 11)
INSERT INTO member_assets (member_asset_id, member_id, asset_id, created_at, updated_at)
VALUES (10, 1, 1, NOW(), NOW()), (11, 1, 4, NOW(), NOW());

-- User 2의 에셋 (ID: 20, 21)
INSERT INTO member_assets (member_asset_id, member_id, asset_id, created_at, updated_at)
VALUES (20, 2, 1, NOW(), NOW()), (21, 2, 4, NOW(), NOW());

-- User 3의 에셋 (ID: 30, 31)
INSERT INTO member_assets (member_asset_id, member_id, asset_id, created_at, updated_at)
VALUES (30, 3, 1, NOW(), NOW()), (31, 3, 4, NOW(), NOW());

-- User 4의 에셋 (ID: 40, 41)
INSERT INTO member_assets (member_asset_id, member_id, asset_id, created_at, updated_at)
VALUES (40, 4, 1, NOW(), NOW()), (41, 4, 4, NOW(), NOW());

-- User 5의 에셋 (ID: 50, 51)
INSERT INTO member_assets (member_asset_id, member_id, asset_id, created_at, updated_at)
VALUES (50, 5, 1, NOW(), NOW()), (51, 5, 4, NOW(), NOW());

-- -----------------------------------------------------
-- [Step 4] MemberInfos & Settings
-- -----------------------------------------------------
-- -----------------------------------------------------
-- [Step 4] MemberInfos (수정됨)
-- profile_image_id, profile_border_id를 위에서 만든 MemberAsset ID로 연결
-- -----------------------------------------------------
INSERT INTO member_infos (
    member_info_id, member_id, age, gender, category_main, category_sub,
    total_level, total_exp, highest_tier,
    profile_image_id, profile_border_id, -- MemberAsset ID 참조
    gold, diamond, belonging
) VALUES
      (1, 1, 25, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 5, 0, 'BRONZE', 10, 11, 1000, 500, '서울대'),
      (2, 2, 24, 'FEMALE', 'ADULT', 'JOB_SEEKER', 3, 0, 'BRONZE', 20, 21, 500, 100, '취준'),
      (3, 3, 26, 'MALE', 'ADULT', 'OFFICE_WORKER', 4, 0, 'SILVER', 30, 31, 0, 0, '삼성전자'),
      (4, 4, 20, 'FEMALE', 'ADULT', 'UNIVERSITY_STUDENT', 1, 0, 'BRONZE', 40, 41, 0, 0, '연세대'),
      (5, 5, 29, 'MALE', 'ADULT', 'PUBLIC_SERVANT', 10, 500, 'BRONZE', 50, 51, 2000, 1000, '시청');

-- -----------------------------------------------------
-- [Step 5] MemberSettings
-- -----------------------------------------------------
INSERT INTO member_settings (member_setting_id, member_id, alarm_on, is_ranking_active)
VALUES
    (1, 1, 1, 1),
    (2, 2, 1, 1),
    (3, 3, 1, 1),
    (4, 4, 1, 1),
    (5, 5, 1, 1);

-- -----------------------------------------------------
-- [Step 5] MemberCharacters (CRITICAL FIX)
-- -----------------------------------------------------
-- 모든 유저에게 '양동동(ID=1)' 캐릭터 지급 및 대표 설정(is_default=1)
-- default_evolution=1 (이미지가 존재하는 진화 단계)
INSERT INTO member_characters (
    member_id, character_id,
    current_level, current_exp, evolution,
    floor, remain_reward,
    is_default, default_evolution,
    created_at, updated_at
) VALUES
-- User 1: 누적 720 EXP -> (Lv 1 + 1) = Lv 2, Remainder 120
(1, 1, 2, 120, 1, 0, 0, 1, 1, NOW(), NOW()),

-- User 2: 누적 2000 EXP -> (Lv 1 + 3) = Lv 4, Remainder 200
(2, 1, 4, 200, 1, 0, 0, 1, 1, NOW(), NOW()),

-- User 3: 누적 100 EXP -> Lv 1, Remainder 100
(3, 1, 1, 100, 1, 0, 0, 1, 1, NOW(), NOW()),

-- User 4: 신규 유저 (0 EXP) -> Lv 1, Remainder 0
(4, 1, 1, 0, 1, 0, 0, 1, 1, NOW(), NOW()),

-- User 5: 누적 5900 EXP -> (Lv 1 + 9) = Lv 10, Remainder 500
(5, 1, 10, 500, 1, 0, 0, 1, 1, NOW(), NOW());

-- -----------------------------------------------------
-- [Step 6] Rankings (Last Week)
-- -----------------------------------------------------
INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at)
VALUES
    (1, 10, 1, 'BRONZE', NOW(), NOW()),
    (2, 10, 2, 'BRONZE', NOW(), NOW()),
    (3, 11, 3, 'SILVER', NOW(), NOW()),
    (5, 10, 5, 'BRONZE', NOW(), NOW()); -- User 4는 신규라 없음


-- -----------------------------------------------------
-- [Step 7] Stats Data (User 1 - 3일전 데이터)
-- -----------------------------------------------------
INSERT INTO subjects (subject_id, member_id, name, color, created_at, updated_at)
VALUES (1, 1, '영어 공부', '#FF0000', NOW(), NOW());

-- 1. Daily Goal (주간 총 시간 계산용)
INSERT INTO daily_goals (daily_goal_id, member_id, daily_goal_date, target_minutes, current_seconds, is_received, reward_multiplier, created_at, updated_at)
VALUES (1, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 120, 7200, 1, 1.0, NOW(), NOW());

-- 2. Daily Subject (과목별 통계 계산용 - 필수 추가!)
INSERT INTO daily_subjects (daily_subject_id, member_id, subject_id, date, focus_seconds, created_at, updated_at)
VALUES (1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 7200, NOW(), NOW());

-- 3. Planner
INSERT INTO planners (planner_id, daily_goal_id, subject_id, start_time, end_time, created_at, updated_at)
VALUES (1, 1, 1, '10:00:00', '12:00:00', NOW(), NOW());


SET FOREIGN_KEY_CHECKS = 1;
