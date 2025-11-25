-- -----------------------------------------------------
-- [Final] 통계 기능 테스트용 통합 데이터
-- -----------------------------------------------------
-- 1. Member 및 기초 데이터 생성
-- 2. Subject (과목) 생성
-- 3. 과거 '집계' 데이터 (WeeklyStat) - 총점용
-- 4. 과거 '상세' 데이터 (DailyGoal) - 그래프용 (핵심 추가!)
-- 5. 현재 '실시간' 데이터 (DailyGoal) - 이번 주 집계용
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- [Step 0] 기존 데이터 초기화
TRUNCATE TABLE daily_subjects;
TRUNCATE TABLE daily_goals;
TRUNCATE TABLE weekly_subject_stats;
TRUNCATE TABLE weekly_stats;
TRUNCATE TABLE monthly_stats;
TRUNCATE TABLE subjects;
TRUNCATE TABLE member_settings;
TRUNCATE TABLE member_infos;
TRUNCATE TABLE members;

-- -----------------------------------------------------
-- [Step 1] Member 생성 (ID: 1)
-- -----------------------------------------------------
INSERT INTO members (
    member_id, social_type, social_id, nickname,
    current_level, current_exp, is_focusing, status, created_at, updated_at
) VALUES (
             1, 'KAKAO', 'test_social_id', '통계테스트유저',
             10, 300, false, 'ACTIVE', '2025-01-01 00:00:00', NOW()
         );

INSERT INTO member_infos (
    member_id, age, gender, category_main, category_sub, belonging,
    total_level, total_exp, gold, diamond, training_reward, bonus_ticket_count
) VALUES (
             1, 24, 'MALE', 'HIGH_SCHOOL', 'HIGH_3', '서울고등학교',
             10, 300, 1000, 100, 0, 0
         );

INSERT INTO member_settings (
    member_id, alarm_on, is_pomodoro, isaiplanner, is_subscription_message_blocked,
    is_ranking_active, total_stat_color
) VALUES (
             1, true, false, false, false, true, 'FFFF00'
         );

-- -----------------------------------------------------
-- [Step 2] Subject 생성
-- -----------------------------------------------------
INSERT INTO subjects (subject_id, member_id, name, color, created_at, updated_at) VALUES
                                                                                      (1, 1, '국어', '#FF5733', NOW(), NOW()),
                                                                                      (2, 1, '수학', '#33FF57', NOW(), NOW());

-- -----------------------------------------------------
-- [Step 3] 과거 WeeklyStat (이미 배치로 집계된 총합)
-- -----------------------------------------------------

-- 10월 마지막 주차 (10/27 ~ 11/02): 총 600분
INSERT INTO weekly_stats (
    member_id, start_date, end_date, total_focus_minutes, total_level, last_character_image_url, created_at, updated_at
) VALUES (
             1, '2025-10-27', '2025-11-02', 600, 10, 'http://example.com/image.png', NOW(), NOW()
         );

-- 11월 1주차 (11/03 ~ 11/09): 총 800분
INSERT INTO weekly_stats (
    member_id, start_date, end_date, total_focus_minutes, total_level, last_character_image_url, created_at, updated_at
) VALUES (
             1, '2025-11-03', '2025-11-09', 800, 11, 'http://example.com/image.png', NOW(), NOW()
         );

-- -----------------------------------------------------
-- [Step 4] 과거 DailyGoal (그래프를 그리기 위한 상세 데이터) ★중요★
-- 이 데이터가 있어야 'focusSecondsPerDayList'가 0으로 안 나옵니다.
-- -----------------------------------------------------

-- 1. 10월 마지막 주차 상세 (총 600분 맞추기)
-- 10/27(월): 300분 (18000초)
INSERT INTO daily_goals (
    member_id, daily_goal_date, target_minutes, current_seconds, max_consecutive_seconds, is_received, reward_multiplier, created_at, updated_at
) VALUES (1, '2025-10-27', 120, 18000, 3600, true, 1.0, NOW(), NOW());

-- 10/28(화): 300분 (18000초)
INSERT INTO daily_goals (
    member_id, daily_goal_date, target_minutes, current_seconds, max_consecutive_seconds, is_received, reward_multiplier, created_at, updated_at
) VALUES (1, '2025-10-28', 120, 18000, 3600, true, 1.0, NOW(), NOW());


-- 2. 11월 1주차 상세 (총 800분 맞추기)
-- 11/03(월) ~ 11/06(목): 매일 200분 (12000초)
INSERT INTO daily_goals (
    member_id, daily_goal_date, target_minutes, current_seconds, max_consecutive_seconds, is_received, reward_multiplier, created_at, updated_at
) VALUES
      (1, '2025-11-03', 120, 12000, 3600, true, 1.0, NOW(), NOW()),
      (1, '2025-11-04', 120, 12000, 3600, true, 1.0, NOW(), NOW()),
      (1, '2025-11-05', 120, 12000, 3600, true, 1.0, NOW(), NOW()),
      (1, '2025-11-06', 120, 12000, 3600, true, 1.0, NOW(), NOW());

-- -----------------------------------------------------
-- [Step 5] 이번 주 DailyGoal (실시간 집계 테스트용)
-- 가정: 오늘이 11/12(수)라고 칠 때, 이번 주 데이터
-- -----------------------------------------------------

-- 11/10(월): 100분 (6000초)
INSERT INTO daily_goals (
    member_id, daily_goal_date, target_minutes, current_seconds, max_consecutive_seconds, is_received, reward_multiplier, created_at, updated_at
) VALUES (1, '2025-11-10', 120, 6000, 3600, false, 1.1, NOW(), NOW());

-- 11/11(화): 120분 (7200초)
INSERT INTO daily_goals (
    member_id, daily_goal_date, target_minutes, current_seconds, max_consecutive_seconds, is_received, reward_multiplier, created_at, updated_at
) VALUES (1, '2025-11-11', 120, 7200, 4800, true, 1.2, NOW(), NOW());


-- -----------------------------------------------------
-- [Step 6] DailySubject (과목별 통계 정합성용 - 선택사항)
-- -----------------------------------------------------
INSERT INTO daily_subjects (member_id, subject_id, date, focus_seconds, created_at, updated_at) VALUES
                                                                                                    (1, 1, '2025-11-10', 3000, NOW(), NOW()), -- 월요일 국어 50분
                                                                                                    (1, 2, '2025-11-10', 3000, NOW(), NOW()), -- 월요일 수학 50분
                                                                                                    (1, 1, '2025-11-11', 3600, NOW(), NOW()), -- 화요일 국어 60분
                                                                                                    (1, 2, '2025-11-11', 3600, NOW(), NOW()); -- 화요일 수학 60분

SET FOREIGN_KEY_CHECKS = 1;
