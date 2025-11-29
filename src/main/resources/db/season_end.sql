-- -----------------------------------------------------
-- Focus Server: SeasonEndJob (시즌 종료 및 교체) 통합 테스트 데이터
-- -----------------------------------------------------
-- 시나리오:
-- 1. 'Season 1'은 어제(일요일) 종료됨.
-- 2. 유저 구성:
--    - User 1: 다이아몬드 (상위 10% 분석 대상)
--    - User 2: 골드
--    - User 3: 실버
--    - User 4: 브론즈
-- 3. 기대 결과:
--    - 보상 메일 발송 (다이아/골드/실버/브론즈)
--    - 기존 랭킹/리그 삭제
--    - 새 시즌 생성
--    - 모든 유저가 새 시즌의 '브론즈' 리그로 재배치 (랜덤 셔플)
-- -----------------------------------------------------

USE focus_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- [Step 0] 기존 데이터 정리
TRUNCATE TABLE mails;
TRUNCATE TABLE rankings;
TRUNCATE TABLE leagues;
TRUNCATE TABLE seasons;
TRUNCATE TABLE member_characters;
TRUNCATE TABLE character_images;
TRUNCATE TABLE characters;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- [Step 1] 시즌 & 리그 데이터 (종료된 시즌)
-- -----------------------------------------------------
-- Season 1: 6주 전 시작 ~ 어제(1일 전) 종료
INSERT INTO seasons (season_id, name, start_date, end_date, created_at, updated_at)
VALUES (1, 'Season 2025-Oct/Nov', DATE_SUB(CURDATE(), INTERVAL 43 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- 리그 생성 (지난 시즌)
-- 1. 다이아몬드 리그 (User 1)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (10, 1, '지난 시즌 다이아', 'ADULT', 'DIAMOND', 6, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- 2. 골드 리그 (User 2)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (11, 1, '지난 시즌 골드', 'ADULT', 'GOLD', 6, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- 3. 실버 리그 (User 3)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (12, 1, '지난 시즌 실버', 'ADULT', 'SILVER', 6, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- 4. 브론즈 리그 (User 4)
INSERT INTO leagues (league_id, season_id, name, category_type, tier, current_week, start_date, end_date, created_at, updated_at)
VALUES (13, 1, '지난 시즌 브론즈', 'ADULT', 'BRONZE', 6, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());


-- -----------------------------------------------------
-- [Step 2] Members 생성 (Entity 필드 완벽 대응)
-- -----------------------------------------------------
-- status: ACTIVE (StartNewSeasonStep에서 조회 조건임)
-- highest_tier: 현재 달성한 티어 기록
INSERT INTO members (
    member_id, social_type, social_id, nickname, status,
    current_level, current_exp, highest_tier,
    is_pre_registration_rewarded, is_subscription_rewarded, is_received_weekly_reward, is_focusing,
    last_login_date_time, created_at, updated_at
) VALUES
      (1, 'KAKAO', 'u1', 'User1_Diamond', 'ACTIVE', 30, 0, 'DIAMOND', 0, 0, 0, 0, NOW(), NOW(), NOW()),
      (2, 'KAKAO', 'u2', 'User2_Gold', 'ACTIVE', 20, 0, 'GOLD', 0, 0, 0, 0, NOW(), NOW(), NOW()),
      (3, 'KAKAO', 'u3', 'User3_Silver', 'ACTIVE', 10, 0, 'SILVER', 0, 0, 0, 0, NOW(), NOW(), NOW()),
      (4, 'KAKAO', 'u4', 'User4_Bronze', 'ACTIVE', 5, 0, 'BRONZE', 0, 0, 0, 0, NOW(), NOW(), NOW());


-- -----------------------------------------------------
-- [Step 3] MemberInfos 생성 (NotNull 필드 채움)
-- -----------------------------------------------------
-- gender, category_sub, belonging 등 필수 필드 추가
-- training_reward, bonus_ticket_count 등 기본값 0 설정
INSERT INTO member_infos (
    member_info_id, member_id, age, gender, category_main, category_sub, belonging,
    total_level, total_exp, gold, diamond,
    training_reward, bonus_ticket_count, profile_message
) VALUES
      (1, 1, 25, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT', '서울대학교', 30, 0, 10000, 500, 0, 0, '다이아 1등'),
      (2, 2, 25, 'FEMALE', 'ADULT', 'OFFICE_WORKER', '삼성전자', 20, 0, 5000, 100, 0, 0, '골드 유저'),
      (3, 3, 22, 'MALE', 'ADULT', 'JOB_SEEKER', '취준생', 10, 0, 1000, 0, 0, 0, '실버 유저'),
      (4, 4, 20, 'FEMALE', 'ADULT', 'UNIVERSITY_STUDENT', '연세대학교', 5, 0, 0, 0, 0, 0, '브론즈 유저');


-- -----------------------------------------------------
-- [Step 4] MemberSettings 생성 (필드 보충)
-- -----------------------------------------------------
-- is_ranking_active: 1 (StartNewSeasonStep에서 재배치 대상이 되려면 필수!)
-- total_stat_color: 필수값 'FFFF00'
INSERT INTO member_settings (
    member_setting_id, member_id,
    alarm_on, is_ranking_active, is_ranking_caution,
    is_pomodoro, isaiplanner, is_subscription_message_blocked,
    ranking_deactivated_count, total_stat_color
) VALUES
      (1, 1, 1, 1, 0, 0, 0, 0, 0, 'FFFF00'),
      (2, 2, 1, 1, 0, 0, 0, 0, 0, 'FFFF00'),
      (3, 3, 1, 1, 0, 0, 0, 0, 0, 'FFFF00'),
      (4, 4, 1, 1, 0, 0, 0, 0, 0, 'FFFF00');


-- -----------------------------------------------------
-- [Step 5] Rankings 데이터 (지난 시즌 최종 성적)
-- -----------------------------------------------------
-- AnalyzeSeasonStep: Diamond 티어를 조회하므로 User 1은 반드시 DIAMOND여야 함.
-- GrantSeasonRewardStep: 이 데이터를 기반으로 보상 메일을 생성함.
INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at) VALUES
                                                                                          (1, 10, 1, 'DIAMOND', NOW(), NOW()), -- User 1: 다이아몬드
                                                                                          (2, 11, 2, 'GOLD', NOW(), NOW()),    -- User 2: 골드
                                                                                          (3, 12, 3, 'SILVER', NOW(), NOW()),  -- User 3: 실버
                                                                                          (4, 13, 4, 'BRONZE', NOW(), NOW());  -- User 4: 브론즈

INSERT INTO member_characters (
    member_id, character_id, current_level, current_exp, evolution,
    floor, remain_reward, is_default, default_evolution, created_at, updated_at
) VALUES
      (1, 1, 10, 0, 1, 0, 0, 1, 1, NOW(), NOW()),
      (2, 1, 10, 0, 1, 0, 0, 1, 1, NOW(), NOW()),
      (3, 1, 10, 0, 1, 0, 0, 1, 1, NOW(), NOW()),
      (4, 1, 10, 0, 1, 0, 0, 1, 1, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;