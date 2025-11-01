-- -----------------------------------------------------
-- 아이템(미션) 마스터 데이터 초기화
-- -----------------------------------------------------
--
-- 'items' 테이블: 미션의 템플릿 (이름, 타입)
-- 'item_details' 테이블: 미션의 세부 옵션 (조건, 가격, 보상)
--
-- ItemType Enum:
-- 'TWICE_AFTER_BUYING': 일일 반복 미션 (2회)
-- 'ONCE_AFTER_BUYING': 주간 달성 미션 (1회)
--
-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE item_details;
TRUNCATE TABLE items;


-- -----------------------------------------------------
-- 1. 집중력 폭발 (일일 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('집중력 폭발', 'TWICE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 60, 1000, 1),  -- 조건(N): 60분
    (@item_id, 90, 3000, 2),  -- 조건(N): 90분
    (@item_id, 120, 5000, 3); -- 조건(N): 120분

-- -----------------------------------------------------
-- 2. 시작 시간 사수 (일일 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('시작 시간 사수', 'TWICE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 6, 2000, 3),  -- 조건(N): 오전 6시
    (@item_id, 7, 2000, 2),  -- 조건(N): 오전 7시
    (@item_id, 8, 2000, 1);  -- 조건(N): 오전 8시

-- -----------------------------------------------------
-- 3. 마지막 생존자 (일일 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('마지막 생존자', 'TWICE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 22, 3000, 1), -- 조건(N): 오후 10시 (22시)
    (@item_id, 23, 3000, 2), -- 조건(N): 오후 11시 (23시)
    (@item_id, 0, 3000, 3);  -- 조건(N): 자정 (0시)

-- -----------------------------------------------------
-- 4. 휴식은 사치 (일일 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('휴식은 사치', 'TWICE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 4, 5000, 3), -- 조건(N): 4시간 미만
    (@item_id, 5, 3000, 2), -- 조건(N): 5시간 미만
    (@item_id, 6, 1000, 1); -- 조건(N): 6시간 미만

-- -----------------------------------------------------
-- 5. 약점 극복 (주간 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('약점 극복', 'ONCE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 0, 3000, 5); -- 단일 옵션 (파라미터 불필요)

-- -----------------------------------------------------
-- 6. 저지 불가 (주간 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('저지 불가', 'ONCE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 0, 4000, 7); -- 단일 옵션 (파라미터 불필요)

-- -----------------------------------------------------
-- 7. 과거 나와 대결 (주간 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('과거 나와 대결', 'ONCE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 0, 4000, 9); -- 단일 옵션 (파라미터 불필요)

-- -----------------------------------------------------
-- 8. 누적 집중의 대가 (주간 미션)
-- -----------------------------------------------------
INSERT INTO items (name, type)
VALUES ('누적 집중의 대가', 'ONCE_AFTER_BUYING');

SET @item_id = LAST_INSERT_ID();

INSERT INTO item_details (item_id, parameter, price, reward_level)
VALUES
    (@item_id, 25, 500, 1),   -- 조건(N): 25시간
    (@item_id, 30, 1000, 2),  -- 조건(N): 30시간
    (@item_id, 35, 1500, 4),  -- 조건(N): 35시간
    (@item_id, 40, 2000, 5),  -- 조건(N): 40시간
    (@item_id, 45, 3000, 7),  -- 조건(N): 45시간
    (@item_id, 50, 3500, 9),  -- 조건(N): 50시간
    (@item_id, 55, 4000, 11), -- 조건(N): 55시간
    (@item_id, 60, 5000, 14); -- 조건(N): 60시간

-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
