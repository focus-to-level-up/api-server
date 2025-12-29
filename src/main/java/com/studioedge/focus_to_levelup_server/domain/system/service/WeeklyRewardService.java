package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.studioedge.focus_to_levelup_server.domain.character.dto.response.CharacterSpecResponse;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.dao.WeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.request.ReceiveWeeklyRewardRequest;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.WeeklyRewardInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.system.exception.WeeklyRewardAlreadyReceivedException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.WeeklyRewardNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyRewardService {

    private final MemberRepository memberRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public WeeklyRewardInfoResponse getWeeklyRewardInfo(Member member) {
        // WeeklyReward 조회 (없으면 예외)
        WeeklyReward weeklyReward = weeklyRewardRepository.findFirstByMemberIdOrderByCreatedAtDesc(member.getId())
                .orElseThrow(WeeklyRewardNotFoundException::new);

        // 이미 수령 여부 확인 (예외 대신 플래그로 전달)
        boolean alreadyReceived = weeklyReward.getIsReceived();

        // 현재 구독 상태 조회
        SubscriptionType subscriptionType = subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId())
                .map(Subscription::getType)
                .orElse(SubscriptionType.NONE);

        // 보너스 티켓 보유 개수 조회
        MemberInfo memberInfo = member.getMemberInfo();
        int bonusTicketCount = memberInfo.getBonusTicketCount();

        // 보상 계산
        int levelBonus = calculateLevelBonus(weeklyReward.getLastLevel());
        int characterBonus = calculateCharacterBonus(
                weeklyReward.getLastCharacter().getRarity(),
                weeklyReward.getEvolution(),
                levelBonus);
        int subscriptionBonus = calculateSubscriptionBonus(subscriptionType, levelBonus + characterBonus);
        int baseReward = levelBonus + characterBonus + subscriptionBonus;
        int ticketBonus = calculateTicketBonus(bonusTicketCount, baseReward);

        return WeeklyRewardInfoResponse.of(
                weeklyReward,
                alreadyReceived,
                subscriptionType,
                bonusTicketCount,
                levelBonus,
                characterBonus,
                subscriptionBonus,
                ticketBonus
        );
    }

    /**
     * 레벨 보너스 계산 (1레벨당 10다이아)
     */
    private int calculateLevelBonus(int level) {
        return level * 10;
    }

    /**
     * 캐릭터 보너스 계산 (등급 + 진화 단계별 퍼센트)
     * CharacterSpecResponse의 weeklyBonusPercents 참조
     */
    private int calculateCharacterBonus(Rarity rarity, int evolution, int baseLevel) {
        CharacterSpecResponse spec = CharacterSpecResponse.from(rarity);
        List<Integer> bonusPercents = spec.weeklyBonusPercents();

        // evolution은 1~3, 인덱스는 0~2
        int evolutionIndex = Math.max(0, Math.min(evolution - 1, bonusPercents.size() - 1));
        int percent = bonusPercents.get(evolutionIndex);

        return (int) (baseLevel * (percent / 100.0));
    }

    /**
     * 구독 보너스 계산
     * - NONE: 0%
     * - NORMAL: 50%
     * - PREMIUM: 100%
     */
    private int calculateSubscriptionBonus(SubscriptionType subscriptionType, int baseAmount) {
        return switch (subscriptionType) {
            case NONE -> 0;
            case NORMAL -> (int) (baseAmount * 0.5);
            case PREMIUM -> baseAmount;
        };
    }

    /**
     * 보너스 티켓 보너스 계산 (티켓 1개당 10%)
     */
    private int calculateTicketBonus(int ticketCount, int baseAmount) {
        if (ticketCount <= 0) return 0;
        return (int) (baseAmount * 0.10);
    }

    @Transactional
    public void receiveWeeklyReward(Long memberId, ReceiveWeeklyRewardRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        WeeklyReward weeklyReward = weeklyRewardRepository.findById(request.weeklyRewardId())
                .orElseThrow(WeeklyRewardNotFoundException::new);
        MemberInfo memberInfo = member.getMemberInfo();

        if (weeklyReward.getIsReceived() || member.getIsReceivedWeeklyReward()) {
            throw new WeeklyRewardAlreadyReceivedException();
        }
        member.receiveWeeklyReward(true);
        weeklyReward.receive();
        memberInfo.addDiamond(request.rewardDiamond());
        if (memberInfo.getBonusTicketCount() > 0) {
            member.getMemberInfo().useBonusTicket();
        }
    }
}
