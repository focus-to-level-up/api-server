-- -----------------------------------------------------
-- 구독권 및 인앱 결제 상품 마스터 데이터 초기화
-- -----------------------------------------------------
--
-- 'products' 테이블: 인앱 결제 상품 (구독권, 다이아 팩)
--
-- ProductType Enum:
-- 'BASIC_SUBSCRIPTION': 기본 구독권
-- 'PREMIUM_SUBSCRIPTION': 프리미엄 구독권
-- 'DIAMOND_PACK': 다이아 구매
--
-- -----------------------------------------------------

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터를 비웁니다 (멱등성 보장)
TRUNCATE TABLE products;

-- -----------------------------------------------------
-- 1. 기본 구독권 (2,200원)
-- -----------------------------------------------------
INSERT INTO products (
    name,
    description,
    price,
    type,
    diamond_reward,
    bonus_ticket_count,
    gift_ticket_count,
    is_active
)
VALUES (
    '기본 구독권',
    '• 황금색 닉네임
• 광고 제거
• 획득 다이아 50% 추가
• 다이아 1,000개 즉시 지급 (결제 당일만)
• 다이아 10% 보너스 선물 티켓 5장',
    2200,
    'BASIC_SUBSCRIPTION',
    1000,  -- 다이아 1000개 즉시 지급
    5,     -- 보너스 티켓 5개
    NULL,  -- 선물 티켓 없음
    TRUE
);

-- -----------------------------------------------------
-- 2. 프리미엄 구독권 (4,400원)
-- -----------------------------------------------------
INSERT INTO products (
    name,
    description,
    price,
    type,
    diamond_reward,
    bonus_ticket_count,
    gift_ticket_count,
    is_active
)
VALUES (
    '프리미엄 구독권',
    '• 황금색 닉네임
• 광고 제거
• 획득 다이아 100% 추가
• 다이아 2,000개 즉시 지급 (결제 당일만)
• 다이아 10% 보너스 선물 티켓 8장
• 프리미엄 구독권(1주일) 선물권 2회 (매달)
• 길드 내 부스트 가능
• 길드 주간 보상 더 많이 수령',
    4400,
    'PREMIUM_SUBSCRIPTION',
    2000,  -- 다이아 2000개 즉시 지급
    8,     -- 보너스 티켓 8개
    2,     -- 선물 티켓 2개
    TRUE
);

-- -----------------------------------------------------
-- 3. 다이아 2500개 (3,300원, 월 1회 한정)
-- -----------------------------------------------------
INSERT INTO products (
    name,
    description,
    price,
    type,
    diamond_reward,
    bonus_ticket_count,
    gift_ticket_count,
    is_active
)
VALUES (
    '다이아 2500개',
    '• 다이아 2,500개 즉시 지급
• 월 1회 한정 특가',
    3300,
    'DIAMOND_PACK',
    2500,  -- 다이아 2500개 즉시 지급
    NULL,  -- 보너스 티켓 없음
    NULL,  -- 선물 티켓 없음
    TRUE
);

-- -----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
