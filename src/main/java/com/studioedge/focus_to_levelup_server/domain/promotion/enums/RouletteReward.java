package com.studioedge.focus_to_levelup_server.domain.promotion.enums;

import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RouletteReward {
    GOLD_1000(20, RewardType.GOLD, 1000, "1,000 골드"),
    DIA_100(20, RewardType.DIAMOND, 100, "100 다이아"),
    GOLD_2000(20, RewardType.GOLD, 2000, "2,000 골드"),
    DIA_200(20, RewardType.DIAMOND, 200, "200 다이아"),
    DIA_500(10, RewardType.DIAMOND, 500, "500 다이아"),
    DIA_1000(5, RewardType.DIAMOND, 1000, "1,000 다이아"),
    EPIC_CHARACTER(5, RewardType.CHARACTER, 0, "에픽 캐릭터 선택권");

    private final int probability; // 확률 (%)
    private final RewardType type;
    private final int amount;
    private final String description;
}
