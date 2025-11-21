-- -----------------------------------------------------
-- DailyJobBatch 테스트 데이터 초기화 (v2 - Snake Case)
-- -----------------------------------------------------
-- DB 선택
USE focus_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 기존 데이터 정리
TRUNCATE TABLE planners;
TRUNCATE TABLE daily_goals;
TRUNCATE TABLE subjects;
TRUNCATE TABLE rankings;
TRUNCATE TABLE leagues;
TRUNCATE TABLE seasons;
TRUNCATE TABLE member_assets;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- 2. 기초 데이터 (시즌, 리그) 생성
-- -----------------------------------------------------
-- User 7(복구 대상)이 들어갈 '성인 브론즈 리그'가 필요함

INSERT INTO seasons (season_id, name, start_date, end_date, created_at, updated_at)
VALUES (1, 'Test Season 1', NOW(), DATE_ADD(NOW(), INTERVAL 6 WEEK), NOW(), NOW());

INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (1, 1, '성인 브론즈 1리그', 'ADULT', 'BRONZE', 1, NOW(), DATE_ADD(NOW(), INTERVAL 6 WEEK), NOW(), NOW());


-- -----------------------------------------------------
-- 3. Members 생성 (7명)
-- -----------------------------------------------------
-- 컬럼: member_id, social_type, social_id, nickname, status, is_focusing, ...

INSERT INTO members (
    member_id, social_type, social_id, nickname, status, is_focusing,
    current_level, current_exp, highest_tier,
    is_pre_registration_rewarded, is_subscription_rewarded, is_received_weekly_reward,
    created_at, updated_at
) VALUES
-- [1] 정상
(1, 'KAKAO', 'social_1', 'User1_Clean', 'ACTIVE', 0, 1, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [2] 플래너 삭제 테스트
(2, 'KAKAO', 'social_2', 'User2_Planner', 'ACTIVE', 0, 1, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [3] 경고 만료 (해제 대상)
(3, 'KAKAO', 'social_3', 'User3_WarnExp', 'ACTIVE', 0, 5, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [4] 경고 유지
(4, 'KAKAO', 'social_4', 'User4_WarnKeep', 'ACTIVE', 0, 3, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [5] 집중 적발 (경고 부여 대상)
(5, 'KAKAO', 'social_5', 'User5_Focus', 'ACTIVE', 1, 2, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [6] 집중 적발 + 밴 (밴 대상)
(6, 'KAKAO', 'social_6', 'User6_FocusBan', 'ACTIVE', 1, 4, 0, 'BRONZE', 0, 0, 0, NOW(), NOW()),
-- [7] 밴 복구 (복구 대상)
(7, 'KAKAO', 'social_7', 'User7_Restore', 'RANKING_BANNED', 0, 0, 0, 'BRONZE', 0, 0, 0, NOW(), NOW());


-- -----------------------------------------------------
-- 4. MemberInfos 생성 (1:1)
-- -----------------------------------------------------
-- 컬럼: member_info_id, member_id, category_main, category_sub ...

INSERT INTO member_infos (
    member_info_id, member_id, age, gender, category_main, category_sub,
    total_level, total_exp, belonging, gold, diamond, training_reward,
    created_at, updated_at
) VALUES
(1, 1, 20, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 1, 0, 'none', 0, 0, 0, NOW(), NOW()),
(2, 2, 20, 'FEMALE', 'ADULT', 'OFFICE_WORKER', 1, 0, 'none', 0, 0, 0, NOW(), NOW()),
(3, 3, 20, 'MALE', 'ADULT', 'JOB_SEEKER', 5, 0, 'none', 0, 0, 0, NOW(), NOW()),
(4, 4, 20, 'FEMALE', 'ADULT', 'PUBLIC_SERVANT', 3, 0, 'none', 0, 0, 0, NOW(), NOW()),
(5, 5, 20, 'MALE', 'ADULT', 'EXAM_TAKER', 2, 0, 'none', 0, 0, 0, NOW(), NOW()),
(6, 6, 20, 'FEMALE', 'ADULT', 'GRADUATE_STUDENT', 4, 0, 'none', 0, 0, 0, NOW(), NOW()),
(7, 7, 20, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', 0, 0, 'none', 0, 0, 0, NOW(), NOW());


-- -----------------------------------------------------
-- 5. MemberSettings 생성
-- -----------------------------------------------------
-- 컬럼: member_setting_id, is_ranking_caution, ranking_warning_at ...

INSERT INTO member_settings (
    member_setting_id, member_id,
    alarm_on, is_pomodoro, is_ai_planner, is_subscription_message_blocked, total_stat_color,
    is_ranking_active, is_ranking_caution, ranking_warning_at, ranking_deactivated_count
) VALUES
-- [1, 2] 깨끗함
(1, 1, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),
(2, 2, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),

-- [3] 경고 만료 (30일 전 -> 28일 지났으므로 해제되어야 함)
(3, 3, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), 0),

-- [4] 경고 유지 (10일 전 -> 유지)
(4, 4, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),

-- [5] 집중 적발 (경고 없음 -> 경고 true로 변경되어야 함)
(5, 5, 1, 0, 0, 0, 'FFFF00', 1, 0, NULL, 0),

-- [6] 집중 밴 (이미 경고 5일 전 -> 밴 처리되어야 함)
(6, 6, 1, 0, 0, 0, 'FFFF00', 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 0),

-- [7] 밴 복구 (15일 전 경고/밴 -> 복구되어야 함)
-- is_ranking_active=0 (현재 랭킹 비활성)
(7, 7, 1, 0, 0, 0, 'FFFF00', 0, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 1);


-- -----------------------------------------------------
-- 6. Rankings 생성 (현재 리그 참여 상태)
-- -----------------------------------------------------
-- User 7은 밴 상태이므로 랭킹 테이블에 없다고 가정 (배치가 복구시키면서 추가할 것임)

INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at)
VALUES
(1, 1, 1, 'BRONZE', NOW(), NOW()),
(2, 1, 2, 'BRONZE', NOW(), NOW()),
(3, 1, 3, 'BRONZE', NOW(), NOW()),
(4, 1, 4, 'BRONZE', NOW(), NOW()),
(5, 1, 5, 'BRONZE', NOW(), NOW()),
(6, 1, 6, 'BRONZE', NOW(), NOW()); -- 밴 당하면 삭제될 수도 있음


-- -----------------------------------------------------
-- 7. DailyGoals & Subjects & Planners (User 2 플래너 삭제 테스트용)
-- -----------------------------------------------------
INSERT INTO subjects (subject_id, member_id, name, color, created_at, updated_at)
VALUES (1, 2, 'Math', '#FF0000', NOW(), NOW());

INSERT INTO daily_goals (daily_goal_id, member_id, daily_goal_date, target_minutes, current_seconds, is_received, reward_multiplier, created_at, updated_at)
VALUES (1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 60, 0, 0, 1.0, NOW(), NOW());

-- User 2의 플래너 (어제 날짜)
INSERT INTO planners (planner_id, member_id, daily_goal_id, subject_id, start_time, end_time, created_at, updated_at)
VALUES (1, 2, 1, 1, '10:00:00', '12:00:00', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;