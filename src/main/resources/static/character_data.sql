-- -----------------------------------------------------
-- 캐릭터 마스터 데이터 초기화
-- -----------------------------------------------------
--
-- 'characters' 테이블: 캐릭터 마스터 (이름, 등급, 가격)
-- 'character_images' 테이블: 캐릭터 진화 이미지 (진화 단계별)
--
-- Rarity Enum: RARE, EPIC, UNIQUE
--
-- 등급별 가격:
-- - 기본 (양동동): 0 다이아 (최초 지급)
-- - 레어: 6,000 다이아
-- - 에픽: 18,000 다이아
-- - 유니크: 40,000 다이아
--
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE character_images;
TRUNCATE TABLE characters;


-- -----------------------------------------------------
-- 1. 기본 캐릭터 - 양동동 (최초 지급)
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('양동동', 'RARE', 0, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 2. 레어 - 김투구
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('김투구', 'RARE', 6000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 3. 레어 - 에르핀
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('에르핀', 'RARE', 6000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 4. 에픽 - 쿠마
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('쿠마', 'EPIC', 18000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 5. 에픽 - 하이든
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('하이든', 'EPIC', 18000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 6. 유니크 - 빙뇽
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('빙뇽', 'UNIQUE', 40000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------
-- 7. 유니크 - 밤톨냥
-- -----------------------------------------------------
INSERT INTO characters (name, rarity, price, description, background_image_url)
VALUES ('밤톨냥', 'UNIQUE', 40000, '', '');

SET @character_id = LAST_INSERT_ID();

INSERT INTO character_images (character_id, evolution, image_url)
VALUES
    (@character_id, 1, ''),
    (@character_id, 2, ''),
    (@character_id, 3, '');


-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
