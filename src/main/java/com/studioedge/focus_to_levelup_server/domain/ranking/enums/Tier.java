package com.studioedge.focus_to_levelup_server.domain.ranking.enums;

public enum Tier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND,
    MASTER;

    public static int getRewardDiamonds(Tier tier) {
        switch (tier) {
            case SILVER: return 100;
            case GOLD: return 150;
            case PLATINUM: return 300;
            case DIAMOND: return 350;
            case MASTER: return 500;
            default: return 0;
        }
    }

    public static Tier determineNextTier(Tier current, double percentile) {
        switch (current) {
            case BRONZE:
                if (percentile <= 0.7) return Tier.SILVER; // 상위 70% 승급
                return Tier.BRONZE; // 30% 유지
            case SILVER:
                if (percentile <= 0.4) return Tier.GOLD; // 상위 40% 승급
                if (percentile <= 0.8) return Tier.SILVER; // 40~80% (40%) 유지
                return Tier.BRONZE; // 하위 20% 강등
            case GOLD:
                if (percentile <= 0.3) return Tier.PLATINUM; // 상위 30% 승급
                if (percentile <= 0.8) return Tier.GOLD; // 30~80% (50%) 유지
                return Tier.SILVER; // 하위 20% 강등
            case PLATINUM:
                if (percentile <= 0.3) return Tier.DIAMOND; // 상위 30% 승급
                if (percentile <= 0.8) return Tier.PLATINUM; // 30~80% (50%) 유지
                return Tier.GOLD; // 하위 20% 강등
            case DIAMOND:
                // 다이아는 승급 없음 (마스터는 시즌 종료 시 별도 로직)
                if (percentile <= 0.8) return Tier.DIAMOND; // 80% 유지
                return Tier.PLATINUM; // 하위 20% 강등
            case MASTER:
                // 마스터는 시즌 종료 후에만 생기거나, 다이아 유지 로직을 따름
                return Tier.DIAMOND; // (로직 정의 필요, 일단 다이아로)
            default:
                return Tier.BRONZE;
        }
    }
}
