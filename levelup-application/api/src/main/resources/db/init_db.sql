-- ---------------------------------
-- 1. focus_meta_db (배치용 DB) 생성
-- ---------------------------------
-- [수정] CHARACTER SET을 CREATE 문에 포함
CREATE DATABASE IF NOT EXISTS `focus_meta_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ---------------------------------
-- 2. 'focus' 유저에게 권한 부여
-- ---------------------------------
-- 'focus_db' (기본 DB)에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON `focus_db`.* TO 'focus'@'%';

-- 'focus_meta_db' (배치 DB)에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON `focus_meta_db`.* TO 'focus'@'%';

GRANT ALL PRIVILEGES ON *.* TO 'focus'@'%' WITH GRANT OPTION;

-- 권한 적용
FLUSH PRIVILEGES;
