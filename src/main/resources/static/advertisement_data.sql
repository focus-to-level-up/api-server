-- -----------------------------------------------------
-- 광고 데이터 초기화 (advertisement_data.sql)
-- -----------------------------------------------------

USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE advertisements;

-- S3 광고 이미지 기본 경로 설정 (가정)
-- 실제 S3 버킷의 advertisement 폴더에 이미지를 업로드해야 합니다.
SET @s3_ad_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/advertisement/';

-- -----------------------------------------------------
-- 1. 김세현(sehh_nn) 선생님 광고
-- 타겟: 예비 고3
-- -----------------------------------------------------
INSERT INTO advertisements (category_sub, image_url, link, click_count, view_count, is_active)
VALUES (
           'HIGH_3',
           CONCAT(@s3_ad_base_url, 'sehh_nn.png'),
           'https://m.yes24.com/goods/detail/148743425',
           0,
           0,
           true
       );

-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
