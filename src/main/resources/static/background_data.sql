-- -----------------------------------------------------
-- 배경 마스터 데이터 초기화 (v1)
-- -----------------------------------------------------
--
-- 'backgrounds' 테이블: 집중 모드, 훈련장 배경 원본
--
-- BackgroundImageType Enum: FOCUS
--
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE backgrounds;

-- S3 기본 경로 설정
SET @s3_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/background/';

-- -----------------------------------------------------
-- 1. 기본 집중 배경
-- -----------------------------------------------------
INSERT IGNORE INTO backgrounds (name, type, image_url)
VALUES
    ('기본 집중 배경', 'FOCUS', CONCAT(@s3_base_url, 'default_focus_background.png'));

-- (향후 새로운 배경 추가 시)
-- INSERT INTO backgrounds (name, type, image_url)
-- VALUES
--    ('훈련장 배경 1', 'TRAINING', CONCAT(@s3_base_url, 'training_background_01.png'));


-- -----------------------------------------------------
SET FOREIGN_KEY_CHECKS = 1;
