package com.studioedge.focus_to_levelup_server.domain.ranking.enums;

public enum Tier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND,
    MASTER;

    public static int getSeasonRewardDiamonds(Tier tier) {
        switch (tier) {
            case BRONZE: return 500;
            case SILVER: return 1000;
            case GOLD: return 1500;
            case PLATINUM: return 2000;
            case DIAMOND: return 3000;
            case MASTER: return 3000;
            default: return 0;
        }
    }

    // 승급 보상 (주간 승급 시)
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

    /**
     * [MODIFY] 다음 티어 결정 로직 수정
     */
    public static Tier determineNextTier(Tier current, double percentile, boolean isEnteringFinalWeek) {
        switch (current) {
            case BRONZE:
                if (percentile <= 0.7) return Tier.SILVER; // 상위 70% 승급
                return Tier.BRONZE; // 30% 유지
            case SILVER:
                if (percentile <= 0.4) return Tier.GOLD; // 상위 40% 승급
                if (percentile <= 0.8) return Tier.SILVER; // 40~80% 유지
                return Tier.BRONZE; // 하위 20% 강등
            case GOLD:
                if (percentile <= 0.3) return Tier.PLATINUM; // 상위 30% 승급
                if (percentile <= 0.8) return Tier.GOLD; // 30~80% 유지
                return Tier.SILVER; // 하위 20% 강등
            case PLATINUM:
                if (percentile <= 0.3) return Tier.DIAMOND; // 상위 30% 승급
                if (percentile <= 0.8) return Tier.PLATINUM; // 30~80% 유지
                return Tier.GOLD; // 하위 20% 강등
            case DIAMOND:
                // [MODIFY] 마지막 주차 진입 시, 다이아 상위 10%는 마스터 승급
                if (isEnteringFinalWeek && percentile <= 0.1) {
                    return Tier.MASTER;
                }
                // 일반적인 경우 (마지막 주차가 아니거나 상위 10% 미만)
                if (percentile <= 0.8) return Tier.DIAMOND; // 80% 유지 (마스터 승급 실패 포함)
                return Tier.PLATINUM; // 하위 20% 강등
            case MASTER:
                // 마지막 주차에만 존재하므로, 사실상 6주 차가 끝나면 시즌 종료 로직으로 넘어감.
                // 로직상으로는 다이아로 강등되지 않으려면 상위권을 유지해야 함을 명시.
                if (percentile <= 0.8) return Tier.MASTER;
                return Tier.DIAMOND; // 강등 시 다이아로
            default:
                return Tier.BRONZE;
        }
    }
}
