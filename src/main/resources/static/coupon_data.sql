USE focus_db;

-- 쿠폰 마스터 데이터
-- RewardType enum: GOLD(0), DIAMOND(1), ETC(2)
-- SubscriptionType enum: NONE, NORMAL, PREMIUM (문자열로 저장)
INSERT IGNORE INTO coupons (coupon_code, description, reward_type, reward, expired_at, subscription_type, subscription_duration_days, created_at, updated_at) VALUES
('DIAMOND100', '다이아 100개 지급', 1, 100, '2025-12-31 23:59:59', NULL, NULL, NOW(), NOW()),
('DIAMOND500', '다이아 500개 지급', 1, 500, '2025-12-31 23:59:59', NULL, NULL, NOW(), NOW()),
('SUBSCRIPTION30', '30일 일반 구독권', 2, 0, '2025-12-31 23:59:59', 'NORMAL', 30, NOW(), NOW()),
('PREMIUM_SUBSCRIPTION', '30일 프리미엄 구독권', 2, 0, '2025-12-31 23:59:59', 'PREMIUM', 30, NOW(), NOW());
