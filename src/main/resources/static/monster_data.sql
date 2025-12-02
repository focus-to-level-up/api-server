-- -----------------------------------------------------
-- 몬스터 마스터 데이터 초기화 (v2 - 분리된 엔티티)
-- -----------------------------------------------------
--
-- 'monsters' 테이블: 몬스터 마스터 (이름)
-- 'monster_images' 테이블: 몬스터 이미지 (타입, URL)
--
-- MonsterImageType Enum: MOVE, DIE
--
-- -----------------------------------------------------
USE focus_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (순서 중요: FK를 가진 images 먼저)
TRUNCATE TABLE monster_images;
TRUNCATE TABLE monsters;

-- S3 몬스터 이미지 기본 경로 설정
SET @s3_base_url = 'https://focus-to-levelup-s3.s3.ap-northeast-2.amazonaws.com/monster/';

-- -----------------------------------------------------
-- 1. 슬라임 (slime)
-- -----------------------------------------------------

-- 1-1. 마스터 몬스터 생성
INSERT INTO monsters (name) VALUES ('slime');

-- 1-2. 방금 생성된 monster_id 가져오기
SET @monster_id = LAST_INSERT_ID();
SET @folder = 'slime/';
SET @file = 'slime';

-- 1-3. 이미지 테이블에 타입별 URL 저장
INSERT IGNORE INTO monster_images (monster_id, type, image_url)
VALUES
    (@monster_id, 'MOVE', CONCAT(@s3_base_url, @folder, @file, '_move.gif')),
    (@monster_id, 'DIE',  CONCAT(@s3_base_url, @folder, @file, '_die.gif'));

-- -----------------------------------------------------
-- (향후 몬스터 추가 시 이 블록을 복사)
--
-- INSERT INTO monsters (name) VALUES ('new_monster');
-- SET @monster_id = LAST_INSERT_ID();
-- SET @folder = 'new_monster/';
-- SET @file = 'new_monster';
--
-- INSERT INTO monster_images (monster_id, type, image_url)
-- VALUES
--    (@monster_id, 'MOVE', CONCAT(@s3_base_url, @folder, @file, '_move.gif')),
--    (@monster_id, 'DIE',  CONCAT(@s3_base_url, @folder, @file, '_die.gif'));
-- -----------------------------------------------------


SET FOREIGN_KEY_CHECKS = 1;
