USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 데이터 초기화
TRUNCATE TABLE weekly_stats;
TRUNCATE TABLE weekly_subject_stats;
TRUNCATE TABLE mails;
TRUNCATE TABLE guild_weekly_rewards;
TRUNCATE TABLE rankings;
TRUNCATE TABLE guild_members;
TRUNCATE TABLE guilds;
TRUNCATE TABLE leagues;
TRUNCATE TABLE seasons;
TRUNCATE TABLE daily_goals;
TRUNCATE TABLE member_characters;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- [Step 2] Seasons & Leagues (지난주/이번주)
-- -----------------------------------------------------
INSERT INTO seasons (season_id, name, start_date, end_date, created_at, updated_at)
VALUES (1, 'Season 2025-Nov', DATE_SUB(NOW(), INTERVAL 2 WEEK), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW());

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

-- 4. 길드 생성 (5개)
INSERT INTO guilds (guild_id, name, description, target_focus_time, is_public, category, max_members, current_members, created_at, updated_at) VALUES
(1, '성실길드', '다이아 300개 목표', 3600, 1, 'COLLEGE', 20, 20, NOW(), NOW()), -- 고득점 길드
(2, '보통길드', '적당히 합시다', 3600, 1, 'COLLEGE', 20, 20, NOW(), NOW()),
(3, '유령길드', '접속 안함', 3600, 1, 'COLLEGE', 20, 20, NOW(), NOW()),
(4, '소수정예', '2명만', 3600, 1, 'COLLEGE', 20, 2, NOW(), NOW()),
(5, '솔로부대', '나혼자', 3600, 1, 'COLLEGE', 20, 1, NOW(), NOW()); -- 보상 제외 대상 (1명)

DROP PROCEDURE IF EXISTS GenerateTestData;
DELIMITER $$

CREATE PROCEDURE GenerateTestData()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE new_profile_image_id BIGINT;
    DECLARE new_profile_border_id BIGINT;
    DECLARE new_subject_id BIGINT;

    WHILE i <= 100 DO
            -- 5-1. Member 생성
            INSERT INTO members (member_id, social_type, social_id, nickname, status, current_level, current_exp, highest_tier, is_received_weekly_reward, created_at, updated_at)
            VALUES (i, 'KAKAO', CONCAT('user_', i), CONCAT('User', i), 'ACTIVE', 10, FLOOR(RAND() * 1000), 'BRONZE', false, NOW(), NOW());

            -- 5-2. MemberAssets 생성 (AUTO_INCREMENT 사용 & ID 저장)
            -- (1) 프로필 이미지 에셋 추가
            INSERT IGNORE INTO member_assets (member_id, asset_id, created_at, updated_at)
            VALUES (i, 1, NOW(), NOW());
            SET new_profile_image_id = LAST_INSERT_ID(); -- 방금 생성된 ID 저장

            -- (2) 테두리 에셋 추가
            INSERT IGNORE INTO member_assets (member_id, asset_id, created_at, updated_at)
            VALUES (i, 4, NOW(), NOW());
            SET new_profile_border_id = LAST_INSERT_ID(); -- 방금 생성된 ID 저장

            -- 5-3. MemberInfo 생성 (저장한 ID 사용)
            INSERT INTO member_infos (
                member_info_id, member_id, age, gender, category_main, category_sub,
                total_level, total_exp, highest_tier,
                profile_image_id, profile_border_id, -- 여기서 저장된 변수 사용
                gold, diamond, school, school_address
            ) VALUES (
                         i, i, 25, 'MALE', 'ADULT', 'UNIVERSITY_STUDENT',
                         FLOOR(RAND() * 10000), 0, 'BRONZE',
                         new_profile_image_id,  -- 변수값 매핑
                         new_profile_border_id, -- 변수값 매핑
                         1000, 500, '서울대', '서울시 관악구'
                     );
    INSERT INTO member_settings (member_setting_id, member_id, alarm_on, is_ranking_active) VALUES (i, i, 1, 1);

    INSERT INTO member_characters (member_id, character_id, current_level, current_exp, evolution, is_default, default_evolution, floor, remain_reward, created_at, updated_at)
    VALUES (i, 1, 1, 0, 1, 1, 1, 0, 0, NOW(), NOW());

    -- 5-3. 길드 가입 (20명씩 분배)
    -- Guild 1(1~20), Guild 2(21~40), Guild 3(41~60), Guild 4(61~62), Guild 5(63)
    IF i <= 20 THEN
                INSERT INTO guild_members (guild_id, member_id, role, weekly_focus_time, is_boosted, joined_at, created_at, updated_at)
                VALUES (1, i, 'MEMBER', 180000, 0, NOW(), NOW(), NOW()); -- 50시간 (300다이아 확정)
            ELSEIF i <= 40 THEN
                INSERT INTO guild_members (guild_id, member_id, role, weekly_focus_time, is_boosted, joined_at, created_at, updated_at)
                VALUES (2, i, 'MEMBER', 90000, 0, NOW(), NOW(), NOW()); -- 25시간 (100다이아)
            ELSEIF i <= 60 THEN
                INSERT INTO guild_members (guild_id, member_id, role, weekly_focus_time, is_boosted, joined_at, created_at, updated_at)
                VALUES (3, i, 'MEMBER', 0, 0, NOW(), NOW(), NOW()); -- 0시간 (보상 없음)
    END IF;

            -- 5-4. 랭킹 데이터 (지난주 브론즈 리그 배정)
            -- User 1~50: 점수 높음 (승격 대상 후보)
            -- User 51~100: 점수 낮음
            IF i <= 50 THEN
                INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at)
                VALUES (i, 10, i, 'BRONZE', NOW(), NOW()); -- 지난주 브론즈
    ELSE
                INSERT INTO rankings (ranking_id, league_id, member_id, tier, created_at, updated_at)
                VALUES (i, 10, i, 'BRONZE', NOW(), NOW());
    END IF;

            -- 5-5. 통계용 DailyGoal (User 1~10만 생성)
            IF i <= 10 THEN
                -- [추가] Subject 생성 (과목 ID: 1로 고정하여 테스트하거나 i값 활용 가능)
                -- 여기서는 각 유저마다 '테스트 과목'을 하나씩 생성한다고 가정
                INSERT INTO subjects (member_id, name, color, created_at, updated_at)
                VALUES (i, '테스트 과목', '#FF0000', NOW(), NOW());

                SET new_subject_id = LAST_INSERT_ID(); -- 생성된 과목 ID 저장

                INSERT INTO daily_goals (member_id, daily_goal_date, target_minutes, current_seconds, is_received, reward_multiplier, using_allowed_app_seconds, created_at, updated_at)
                VALUES (i, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 60, 3600, 1, 1.0, 0, NOW(), NOW());

                INSERT INTO daily_subjects (member_id, subject_id, date, focus_seconds, created_at, updated_at)
                VALUES (i, new_subject_id, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3600, NOW(), NOW()); -- 생성된 subject_id 사용
            END IF;

            SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL GenerateTestData();

SET FOREIGN_KEY_CHECKS = 1;
