package com.studioedge.domain.system.service;

import com.studioedge.domain.character.response.CharacterSpecResponse;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.payment.repository.SubscriptionRepository;
import com.studioedge.payment.entity.Subscription;
import com.studioedge.payment.enums.SubscriptionType;
import com.studioedge.system.repository.WeeklyRewardRepository;
import com.studioedge.domain.system.dto.response.WeeklyRewardInfoResponseV2;
import com.studioedge.system.entity.WeeklyReward;
import com.studioedge.system.exception.WeeklyRewardAlreadyReceivedException;
import com.studioedge.system.exception.WeeklyRewardNotFoundException;
import com.studioedge.character.enums.Rarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyRewardServiceV2 {

    private final MemberRepository memberRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public WeeklyRewardInfoResponseV2 getWeeklyRewardInfo(Member member) {
        // WeeklyReward 조회
        WeeklyReward weeklyReward = weeklyRewardRepository.findFirstByMemberIdOrderByCreatedAtDesc(member.getId())
                .orElseThrow(WeeklyRewardNotFoundException::new); // or return null handling based on requirement

        boolean alreadyReceived = weeklyReward.getIsReceived();

        // [수정] 모든 계산을 Service 내부 로직에서 처리
        RewardCalculationResult calculation = calculateRewardDetails(member, weeklyReward);

        return WeeklyRewardInfoResponseV2.of(
                weeklyReward,
                alreadyReceived,
                calculation.subscriptionType(),
                calculation.bonusTicketCount(),
                calculation.levelBonus(),
                calculation.characterBonus(),
                calculation.subscriptionBonusDisplay(), // 화면 표시용 (미구독이어도 값 존재)
                calculation.ticketBonusDisplay(),       // 화면 표시용 (티켓없어도 값 존재)
                calculation.totalExpectedDiamond()      // 실제 총합
        );
    }

    @Transactional
    public void receiveWeeklyReward(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = member.getMemberInfo();

        WeeklyReward weeklyReward = weeklyRewardRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(WeeklyRewardNotFoundException::new);

        if (!weeklyReward.getMember().getId().equals(memberId)) {
            throw new WeeklyRewardNotFoundException();
        }
        if (member.getIsReceivedWeeklyReward() || weeklyReward.getIsReceived()) {
            throw new WeeklyRewardAlreadyReceivedException();
        }

        // [동일한 계산 로직 사용
        RewardCalculationResult calculation = calculateRewardDetails(member, weeklyReward);
        int finalRewardDiamond = calculation.totalExpectedDiamond();

        memberInfo.addDiamond(finalRewardDiamond);
        member.receiveWeeklyReward(true);

        // 실제 합계에 티켓 보너스가 포함된 경우에만 소모
        if (memberInfo.getBonusTicketCount() > 0) {
            memberInfo.useBonusTicket();
        }

        weeklyReward.receive();
    }

    // ================== 내부 계산 로직 ==================

    private record RewardCalculationResult(
            SubscriptionType subscriptionType,
            int bonusTicketCount,
            int levelBonus,
            int characterBonus,
            int subscriptionBonusDisplay, // 화면 표시용
            int ticketBonusDisplay,       // 화면 표시용
            int totalExpectedDiamond      // 실제 지급 총액
    ) {}

    private RewardCalculationResult calculateRewardDetails(Member member, WeeklyReward weeklyReward) {
        // 1. 상태 조회
        SubscriptionType subscriptionType = subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId())
                .map(Subscription::getType)
                .orElse(SubscriptionType.NONE);

        int bonusTicketCount = member.getMemberInfo().getBonusTicketCount();

        // 2. 기본 보상 계산
        int levelBonus = calculateLevelBonus(weeklyReward.getLastLevel());

        // [수정] 캐릭터 보상 계산 (올림 처리 적용)
        int characterBonus = calculateCharacterBonus(
                weeklyReward.getLastCharacter().getRarity(),
                weeklyReward.getEvolution(),
                levelBonus
        );

        int baseReward = levelBonus + characterBonus;

        // 3. 구독 보너스 계산
        int subscriptionBonusDisplay;
        int subscriptionBonusForTotal = 0;

        if (subscriptionType == SubscriptionType.PREMIUM) {
            // 프리미엄: 100%
            subscriptionBonusDisplay = baseReward; // 100%니까 그대로
            subscriptionBonusForTotal = subscriptionBonusDisplay;
        } else {
            // NONE(미구독) 또는 BASIC(베이직)
            // [요구사항] 미구독이어도 베이직(50%) 기준으로 표시값 반환 (올림 처리)
            subscriptionBonusDisplay = calculateRoundedUp(baseReward * 0.5);

            if (subscriptionType == SubscriptionType.BASIC) {
                subscriptionBonusForTotal = subscriptionBonusDisplay;
            }
            // NONE이면 subscriptionBonusForTotal은 0
        }

        // 4. 티켓 보너스 계산
        // [요구사항] 티켓이 없어도 10% 기준으로 표시값 반환 (올림 처리)
        int ticketBonusDisplay = calculateRoundedUp(baseReward * 0.1);
        int ticketBonusForTotal = 0;

        if (bonusTicketCount > 0) {
            ticketBonusForTotal = ticketBonusDisplay;
        }

        // 5. 총합 계산
        int totalExpectedDiamond = baseReward + subscriptionBonusForTotal + ticketBonusForTotal;

        return new RewardCalculationResult(
                subscriptionType,
                bonusTicketCount,
                levelBonus,
                characterBonus,
                subscriptionBonusDisplay,
                ticketBonusDisplay,
                totalExpectedDiamond
        );
    }

    private int calculateLevelBonus(int level) {
        return level * 10;
    }

    private int calculateCharacterBonus(Rarity rarity, int evolution, int levelBonus) {
        CharacterSpecResponse spec = CharacterSpecResponse.from(rarity);
        List<Integer> bonusPercents = spec.weeklyBonusPercents();
        int evolutionIndex = Math.max(0, Math.min(evolution - 1, bonusPercents.size() - 1));
        int percent = bonusPercents.get(evolutionIndex);

        // [수정] 올림 처리 적용
        return calculateRoundedUp(levelBonus * (percent / 100.0));
    }

    // [추가] 소수점 올림 유틸 메서드
    private int calculateRoundedUp(double value) {
        return (int) Math.ceil(value);
    }
}
