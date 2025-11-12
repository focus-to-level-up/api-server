-- -----------------------------------------------------
-- 몬스터 마스터 데이터 초기화 (v1)
-- -----------------------------------------------------
--
-- 'monsters' 테이블: 몬스터 마스터 (이름, 타입, 이미지 URL)
--
-- MonsterImageType Enum: MOVE, DIE
--
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE monsters;

-- S3 몬스터 이미지 기본 경로 설정
SET @s3_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/monster/';

-- -----------------------------------------------------
-- 1. 슬라임 (slime)
-- -----------------------------------------------------
SET @folder = 'slime/';
SET @file = 'slime';

INSERT INTO monsters (name, type, image_url)
VALUES
    ('slime', 'MOVE', CONCAT(@s3_base_url, @folder, @file, '_move.gif')),
    ('slime', 'DIE',  CONCAT(@s3_base_url, @folder, @file, '_die.gif'));

-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;