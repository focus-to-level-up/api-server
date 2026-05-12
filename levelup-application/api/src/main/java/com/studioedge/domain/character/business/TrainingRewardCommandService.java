package com.studioedge.domain.character.business;

import com.studioedge.character.component.MemberCharacterReader;
import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.member.component.MemberInfoReader;
import com.studioedge.member.entity.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 훈련 보상 서비스
 * - 집중 시간에 비례하여 훈련 보상 적립
 * - 보유한 모든 캐릭터의 진화 단계별 시급으로 계산
 * - 분 단위로 누적, 수령 시 60분당 1다이아로 변환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingRewardCommandService {

    private final MemberCharacterReader memberCharacterReader;
    private final MemberInfoReader memberInfoReader;

    /**
     * 집중 종료 시 훈련 보상 적립
     * - (분 × 시급) 단위로 누적
     * - 예: 레어 2단계(시급 2) + 에픽 1단계(시급 1) = 총 시급 3
     *       90분 집중 → 3 × 90 = 270 적립
     */
    public void accumulateTrainingReward(Long memberId, int focusSeconds) {
        int focusMinutes = focusSeconds / 60;
        if (focusMinutes < 1) {
            return;
        }

        List<MemberCharacter> memberCharacters = memberCharacterReader.findAllByMemberId(memberId);
        if (memberCharacters.isEmpty()) {
            return;
        }

        // 모든 캐릭터의 시급 합계
        int totalRewardPerHour = 0;
        for (MemberCharacter mc : memberCharacters) {
            totalRewardPerHour += mc.calculateRewardPerHour();
        }

        // 분×시급 누적
        int reward = totalRewardPerHour * focusMinutes;

        if (reward > 0) {
            MemberInfo memberInfo = memberInfoReader.findOne(memberId);
            memberInfo.addTrainingReward(reward);
        }
    }

    /**
     * 훈련 보상 수령 (다이아로 전환)
     */
    public int claimTrainingReward(Long memberId) {
        MemberInfo memberInfo = memberInfoReader.findOne(memberId);
        return memberInfo.claimTrainingReward();
    }
}
