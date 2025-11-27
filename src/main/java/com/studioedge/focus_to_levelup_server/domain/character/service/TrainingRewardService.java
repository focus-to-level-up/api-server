package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.CharacterSpecResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
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
public class TrainingRewardService {

    private final MemberCharacterRepository memberCharacterRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 집중 종료 시 훈련 보상 적립
     * - 분×시급 단위로 누적
     * - 예: 레어 2단계(시급 2) + 에픽 1단계(시급 1) = 총 시급 3
     *       90분 집중 → 3 × 90 = 270 적립
     *
     * @param memberId 회원 ID
     * @param focusSeconds 집중 시간 (초)
     */
    @Transactional
    public void accumulateTrainingReward(Long memberId, int focusSeconds) {
        int focusMinutes = focusSeconds / 60;
        if (focusMinutes < 1) {
            return;
        }

        List<MemberCharacter> memberCharacters = memberCharacterRepository.findAllByMemberIdWithCharacter(memberId);
        if (memberCharacters.isEmpty()) {
            return;
        }

        // 모든 캐릭터의 시급 합계
        int totalRewardPerHour = 0;
        for (MemberCharacter mc : memberCharacters) {
            totalRewardPerHour += calculateRewardPerHour(mc);
        }

        // 분×시급 누적
        int reward = totalRewardPerHour * focusMinutes;

        if (reward > 0) {
            MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                    .orElseThrow(InvalidMemberException::new);
            memberInfo.addTrainingReward(reward);
        }
    }

    /**
     * 훈련 보상 수령 (다이아로 전환)
     *
     * @param memberId 회원 ID
     * @return 수령한 다이아 수량
     */
    @Transactional
    public int claimTrainingReward(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        return memberInfo.claimTrainingReward();
    }

    /**
     * 수령 가능한 다이아 조회
     *
     * @param memberId 회원 ID
     * @return 수령 가능한 다이아 수량
     */
    @Transactional(readOnly = true)
    public int getClaimableDiamond(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        return memberInfo.getTrainingReward() / 60;
    }

    /**
     * 현재 적립된 보상 조회 (분×시급 단위)
     *
     * @param memberId 회원 ID
     * @return 적립된 보상
     */
    @Transactional(readOnly = true)
    public int getAccumulatedReward(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        return memberInfo.getTrainingReward();
    }

    /**
     * 캐릭터의 시간당 훈련 보상 계산
     */
    private int calculateRewardPerHour(MemberCharacter memberCharacter) {
        Rarity rarity = memberCharacter.getCharacter().getRarity();
        int evolution = memberCharacter.getEvolution();

        CharacterSpecResponse spec = CharacterSpecResponse.from(rarity);
        List<Integer> rewards = spec.trainingRewardsPerHour();

        // evolution 1, 2, 3 → index 0, 1, 2
        int index = Math.min(evolution - 1, rewards.size() - 1);
        return rewards.get(index);
    }
}
