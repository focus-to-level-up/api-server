-- -----------------------------------------------------
-- 광고 데이터 초기화 (advertisement_data.sql)
-- 구조 변경 반영: advertisements(부모) + advertisement_categories(자식)
-- -----------------------------------------------------

USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (참조 관계가 있으므로 자식 테이블부터 비움)
TRUNCATE TABLE advertisement_categories;
TRUNCATE TABLE advertisements;

-- S3 광고 이미지 기본 경로 설정
SET @s3_ad_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/advertisement/';

-- -----------------------------------------------------
-- 1. 김세현(sehh_nn) 선생님 광고
-- 타겟: 예비 고3 (HIGH_3)
-- -----------------------------------------------------

-- [Step 1] 부모 테이블(advertisements)에 기본 정보 저장
-- 주의: category_sub 컬럼은 더 이상 이 테이블에 존재하지 않습니다.
INSERT INTO advertisements (image_url, link, click_count, view_count, is_active)
VALUES (
           CONCAT(@s3_ad_base_url, 'sehh_nn.png'),
           'https://m.yes24.com/goods/detail/148743425',
           0,
           0,
           true
       );

-- 방금 들어간 광고의 ID를 변수에 저장
SET @ad_id_1 = LAST_INSERT_ID();

-- [Step 2] 자식 테이블(advertisement_categories)에 카테고리 매핑 정보 저장
-- 하나의 광고에 여러 카테고리를 넣고 싶다면 VALUES에 여러 줄을 추가하면 됩니다.
INSERT INTO advertisement_categories (advertisement_id, category_sub)
VALUES
    (@ad_id_1, 'HIGH_3');
-- 만약 '예비 고2'도 포함하고 싶다면 아래 주석처럼 추가
-- , (@ad_id_1, 'HIGH_2');

-- -----------------------------------------------------
-- 2. 김서현(kim._.fx) 선생님 광고
-- 타겟: 예비 고3 (HIGH_3), N수생 (N_SU)
-- 설문조사 기반 데이터
-- -----------------------------------------------------

-- [Step 1] 부모 테이블(advertisements)에 정보 저장
-- 이미지 파일명은 인스타 ID를 참고하여 'kim_fx.png'로 가정하였습니다.
-- 실제 S3에 업로드된 파일명과 일치해야 합니다.
INSERT INTO advertisements (image_url, link, click_count, view_count, is_active)
VALUES (
           CONCAT(@s3_ad_base_url, 'kim._.fx.png'),
           'https://hacks-zone.imweb.me',
           0,
           0,
           true
       );

-- 방금 들어간 광고의 ID를 변수에 저장
SET @ad_id_2 = LAST_INSERT_ID();

-- [Step 2] 자식 테이블(advertisement_categories)에 타겟 카테고리 매핑
-- 설문조사의 '예비 고3'과 'N수'를 모두 포함합니다.
INSERT INTO advertisement_categories (advertisement_id, category_sub)
VALUES
    (@ad_id_2, 'HIGH_3'), -- 예비 고3
    (@ad_id_2, 'N_SU');   -- N수생 (기타 답변 반영)

SET FOREIGN_KEY_CHECKS = 1;
