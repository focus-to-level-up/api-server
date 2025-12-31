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
VALUES ('양동동', 'RARE', 0, '실수로 양동이가 머리에 끼는 바람에 그냥 이렇게 살기로 결심했다. 아무도 진짜 얼굴은 모른다…', '');

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
VALUES ('김투구', 'RARE', 6000, '처음에는 견습기사로 시작했지만 성장속도가 누구보다 빨라서 명성을 떨치고 있다. 과연 김투구의 한계는 어디일까…? ', '');

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
VALUES ('에르핀', 'RARE', 6000, '바람을 자유롭게 다룰수있는 엘프, 바람을 다룰수 있어서 멀리있는 목표도 단번에 명중시킬 수 있다. ', '');

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
VALUES ('쿠마', 'EPIC', 18000, '너무 착한 성격 때문에 지옥에서 쫓겨났다. 악마 종족 답게 성장할 수록 뿔의 크기가 커지는 특징이 있다. ', '');

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
VALUES ('하이든', 'EPIC', 18000, '수명과 강함을 바꾼 마법사. 강해질수록 그의 수염은 하얗게 변해버렸다.', '');

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
VALUES ('빙뇽', 'UNIQUE', 40000, '알을 깨고 나오지 못해서 발만 나온 상태로 몇십년을 살았다고 전해진다. 성장하게 되면 무시무시한 용이 될꺼라고…?!', '');

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
VALUES ('밤톨냥', 'UNIQUE', 40000, '도둑 고양이로 살다가 우연히 표창을 길거리에서 주운 뒤로 뛰어난 암살자가 되었다… 목격자가 없으면 암살이라고 주장하는 편….', '');

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
    (@character_id, 2, 'WEAPON',     CONCAT(@s3_base_url, @folder, @file, '2_weapon.gif')),
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
