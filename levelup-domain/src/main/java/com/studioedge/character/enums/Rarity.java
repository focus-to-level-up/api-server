package com.studioedge.character.enums;

import lombok.Getter;
import java.util.List;

@Getter
public enum Rarity {
    RARE(List.of(2, 3, 4)),
    EPIC(List.of(1, 2, 3)),
    UNIQUE(List.of(3, 4, 5));

    private final List<Integer> trainingRewardsPerHour;

    Rarity(List<Integer> trainingRewardsPerHour) {
        this.trainingRewardsPerHour = trainingRewardsPerHour;
    }

    public int getTrainingRewardPerHour(int evolution) {
        int index = Math.min(Math.max(0, evolution - 1), trainingRewardsPerHour.size() - 1);
        return trainingRewardsPerHour.get(index);
    }
}
