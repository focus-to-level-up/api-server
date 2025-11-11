-- -----------------------------------------------------
-- 에셋 마스터 데이터 초기화 (Asset v1)
-- -----------------------------------------------------
--
-- 'assets' 테이블: 모든 프로필 이미지, 테두리 원본 데이터
--
-- AssetType Enum:
--   CHARACTER_PROFILE_IMAGE
--   CHARACTER_PROFILE_BORDER
--   TIER_BORDER
--
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE assets;

-- S3 기본 경로 설정
SET @s3_image_base = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/asset/image/';
SET @s3_border_base = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/asset/border/';


-- -----------------------------------------------------
-- 1. 양동동 (yangdongdong)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '양동동 1단계 프로필 이미지', CONCAT(@s3_image_base, 'yangdongdong1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '양동동 2단계 프로필 이미지', CONCAT(@s3_image_base, 'yangdongdong2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '양동동 3단계 프로필 이미지', CONCAT(@s3_image_base, 'yangdongdong3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '양동동 프로필 테두리', CONCAT(@s3_border_base, 'yangdongdong_profile_border.png')); -- (테두리 파일명은 S3 목록에 없어 추정)

-- -----------------------------------------------------
-- 2. 김투구 (kimtoogoo)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '김투구 1단계 프로필 이미지', CONCAT(@s3_image_base, 'kimtoogoo1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '김투구 2단계 프로필 이미지', CONCAT(@s3_image_base, 'kimtoogoo2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '김투구 3단계 프로필 이미지', CONCAT(@s3_image_base, 'kimtoogoo3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '김투구 프로필 테두리', CONCAT(@s3_border_base, 'kimtoogoo_profile_border.png'));

-- -----------------------------------------------------
-- 3. 에르핀 (elfin)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '에르핀 1단계 프로필 이미지', CONCAT(@s3_image_base, 'elfin1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '에르핀 2단계 프로필 이미지', CONCAT(@s3_image_base, 'elfin2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '에르핀 3단계 프로필 이미지', CONCAT(@s3_image_base, 'elfin3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '에르핀 프로필 테두리', CONCAT(@s3_border_base, 'elfin_profile_border.png'));

-- -----------------------------------------------------
-- 4. 쿠마 (kuma)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '쿠마 1단계 프로필 이미지', CONCAT(@s3_image_base, 'kuma1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '쿠마 2단계 프로필 이미지', CONCAT(@s3_image_base, 'kuma2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '쿠마 3단계 프로필 이미지', CONCAT(@s3_image_base, 'kuma3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '쿠마 프로필 테두리', CONCAT(@s3_border_base, 'kuma_profile_border.png'));

-- -----------------------------------------------------
-- 5. 하이든 (hiden)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '하이든 1단계 프로필 이미지', CONCAT(@s3_image_base, 'hiden1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '하이든 2단계 프로필 이미지', CONCAT(@s3_image_base, 'hiden2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '하이든 3단계 프로필 이미지', CONCAT(@s3_image_base, 'hiden3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '하이든 프로필 테두리', CONCAT(@s3_border_base, 'hiden_profile_border.png'));

-- -----------------------------------------------------
-- 6. 빙뇽 (bingyoung)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '빙뇽 1단계 프로필 이미지', CONCAT(@s3_image_base, 'bingyoung1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '빙뇽 2단계 프로필 이미지', CONCAT(@s3_image_base, 'bingyoung2_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '빙뇽 3단계 프로필 이미지', CONCAT(@s3_image_base, 'bingyoung3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '빙뇽 프로필 테두리', CONCAT(@s3_border_base, 'bingyoung_profile_border.png'));

-- -----------------------------------------------------
-- 7. 밤톨냥 (bamtolnyang)
-- -----------------------------------------------------
INSERT INTO assets (type, name, asset_url)
VALUES
    ('CHARACTER_PROFILE_IMAGE', '밤톨냥 1단계 프로필 이미지', CONCAT(@s3_image_base, 'bamtolnyang1_profile_image.png')),
    ('CHARACTER_PROFILE_IMAGE', '밤톨냥 2단계 프로필 이미지', CONCAT(@s3_image_base, 'bamtolnyang2_profile_image.png')), --
    ('CHARACTER_PROFILE_IMAGE', '밤톨냥 3단계 프로필 이미지', CONCAT(@s3_image_base, 'bamtolnyang3_profile_image.png')),
    ('CHARACTER_PROFILE_BORDER', '밤톨냥 프로필 테두리', CONCAT(@s3_border_base, 'bamtolnyang_profile_border.png'));

-- -----------------------------------------------------
SET FOREIGN_KEY_CHECKS = 1;
