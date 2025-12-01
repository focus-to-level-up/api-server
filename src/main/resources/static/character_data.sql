-- -----------------------------------------------------
-- 캐릭터 마스터 데이터 초기화 (v3 - HEAD 이미지 추가)
-- -----------------------------------------------------
--
-- 'characters' 테이블: 캐릭터 마스터 (이름, 등급, 가격)
-- 'character_images' 테이블: 캐릭터 모든 이미지 (진화/타입별)
--
-- Rarity Enum: RARE, EPIC, UNIQUE
-- CharacterImageType Enum: IDLE, ATTACK, WEAPON, BACKGROUND, PICTURE, HEAD
--
-- -----------------------------------------------------
USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE character_images;
TRUNCATE TABLE characters;

-- S3 기본 경로 설정
SET @s3_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/character/';

-- -----------------------------------------------------
-- 1. 기본 캐릭터 - 양동동 (S3: yangdongdong)
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('양동동', 'RARE', 0, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'yangdongdong/';
SET @file = 'yangdongdong';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED

-- -----------------------------------------------------
-- 2. 레어 - 김투구 (S3: kimtoogoo)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('김투구', 'RARE', 6000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'kimtoogoo/';
SET @file = 'kimtoogoo';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED


-- -----------------------------------------------------
-- 3. 레어 - 에르핀 (S3: elfin)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('에르핀', 'RARE', 6000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'elfin/';
SET @file = 'elfin';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED


-- -----------------------------------------------------
-- 4. 에픽 - 쿠마 (S3: kuma)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('쿠마', 'EPIC', 18000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'kuma/';
SET @file = 'kuma';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED


-- -----------------------------------------------------
-- 5. 에픽 - 하이든 (S3: hiden)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('하이든', 'EPIC', 18000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'hiden/';
SET @file = 'hiden';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED


-- -----------------------------------------------------
-- 6. 유니크 - 빙뇽 (S3: bingyoung)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('빙뇽', 'UNIQUE', 40000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'bingyoung/';
SET @file = 'bingyoung';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED


-- -----------------------------------------------------
-- 7. 유니크 - 밤톨냥 (S3: bamtolnyang)
-- -----------------------------------------------------
INSERT IGNORE INTO characters (name, rarity, price, description, background_image_url)
VALUES ('밤톨냥', 'UNIQUE', 40000, '', '');

SET @character_id = LAST_INSERT_ID();
SET @folder = 'bamtolnyang/';
SET @file = 'bamtolnyang';

INSERT IGNORE INTO character_images (character_id, evolution, image_type, image_url)
VALUES
    (@character_id, 0, 'BACKGROUND', CONCAT(@s3_base_url, @folder, @file, '_background.png')),
    -- Evolution 1
    (@character_id, 1, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '1.png')),
    (@character_id, 1, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '1_attack.gif')),
    (@character_id, 1, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '1_idle.gif')),
    (@character_id, 1, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '1_weapon.png')),
    (@character_id, 1, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '1_head.png')), -- ADDED
    -- Evolution 2
    (@character_id, 2, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '2.png')),
    (@character_id, 2, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '2_attack.gif')),
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.png')),
    (@character_id, 2, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '2_idle.gif')),
    (@character_id, 2, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '2_head.png')), -- ADDED
    -- Evolution 3
    (@character_id, 3, 'PICTURE',    CONCAT(@s3_base_url, @folder, @file, '3.png')),
    (@character_id, 3, 'ATTACK',     CONCAT(@s3_base_url, @folder, @file, '3_attack.gif')),
    (@character_id, 3, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '3_weapon.png')),
    (@character_id, 3, 'IDLE',       CONCAT(@s3_base_url, @folder, @file, '3_idle.gif')),
    (@character_id, 3, 'HEAD',       CONCAT(@s3_base_url, @folder, @file, '3_head.png')); -- ADDED

-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
