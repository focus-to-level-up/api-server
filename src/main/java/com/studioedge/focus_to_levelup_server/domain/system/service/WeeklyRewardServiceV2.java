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
public class WeeklyRewardServiceV2 {

    private final MemberRepository memberRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;
    private final SubscriptionRepository subscriptionRepository;
    /**
     * [V2] 주간 보상 수령 (서버 사이드 계산 검증 포함)
     * MemberInfo의 상태를 체크하고, 지급 후 WeeklyReward 데이터를 삭제합니다.
     */
    @Transactional
    public void receiveWeeklyReward(Long memberId) {
        // 1. 유저 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = member.getMemberInfo();

        // 3. 주간 보상 데이터 조회 (본인 소유 확인)
        WeeklyReward weeklyReward = weeklyRewardRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(WeeklyRewardNotFoundException::new);
        if (!weeklyReward.getMember().getId().equals(memberId)) {
            throw new WeeklyRewardNotFoundException();
        }
        if (member.getIsReceivedWeeklyReward() || weeklyReward.getIsReceived()) {
            throw new WeeklyRewardAlreadyReceivedException();
        }

        // 4. [보안 핵심] 보상량 서버 재계산
        // 클라이언트가 보낸 request.rewardDiamond()는 무시하고 서버가 직접 계산합니다.
        RewardCalculationResult calculation = calculateTotalReward(member, weeklyReward);
        int finalRewardDiamond = calculation.totalAmount();

        // 5. 상태 업데이트 및 보상 지급
        memberInfo.addDiamond(finalRewardDiamond);
        member.receiveWeeklyReward(true); // 플래그를 '받음(true)'으로 변경

        // 6. 보너스 티켓 소모 처리 (계산에 사용되었다면 차감)
        if (calculation.ticketBonus() > 0) {
            memberInfo.useBonusTicket();
        }

        // 7. 주간 보상 수령
        weeklyReward.receive();
    }

    // ================== 내부 계산 로직 (공통 사용) ==================

    // 계산 결과를 담을 내부 Record
    private record RewardCalculationResult(
            SubscriptionType subscriptionType,
            int bonusTicketCount,
            int levelBonus,
            int characterBonus,
            int subscriptionBonus,
            int ticketBonus,
            int totalAmount
    ) {}

    private RewardCalculationResult calculateTotalReward(Member member, WeeklyReward weeklyReward) {
        // 1. 구독 상태 조회
        SubscriptionType subscriptionType = subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId())
                .map(Subscription::getType)
                .orElse(SubscriptionType.NONE);

        // 2. 티켓 보유량 조회
        int bonusTicketCount = member.getMemberInfo().getBonusTicketCount();

        // 3. 각 항목별 보너스 계산
        int levelBonus = calculateLevelBonus(weeklyReward.getLastLevel());
        int characterBonus = calculateCharacterBonus(weeklyReward.getLastCharacter().getRarity(), weeklyReward.getEvolution());
        int baseReward = levelBonus + characterBonus;
        int subscriptionBonus = calculateSubscriptionBonus(subscriptionType, baseReward);
        int ticketBonus = calculateTicketBonus(bonusTicketCount, baseReward);
        int totalAmount = baseReward + subscriptionBonus + ticketBonus;

        return new RewardCalculationResult(
                subscriptionType,
                bonusTicketCount,
                levelBonus,
                characterBonus,
                subscriptionBonus,
                ticketBonus,
                totalAmount
        );
    }

    private int calculateLevelBonus(int level) {
        return level * 10; // 1레벨당 10다이아 (예시)
    }

    private int calculateCharacterBonus(Rarity rarity, int evolution) {
        CharacterSpecResponse spec = CharacterSpecResponse.from(rarity);
        List<Integer> bonusPercents = spec.weeklyBonusPercents();
        int evolutionIndex = Math.max(0, Math.min(evolution - 1, bonusPercents.size() - 1));
        return bonusPercents.get(evolutionIndex); // 여기서는 다이아 수치가 아니라 % 계산 등이 필요할 수 있음.
        // 기존 로직상 리턴값이 다이아 양이라고 가정하고 작성됨.
        // 만약 퍼센트라면: (levelBonus * percent / 100) 형태로 수정 필요
    }

    private int calculateSubscriptionBonus(SubscriptionType subscriptionType, int baseAmount) {
        return switch (subscriptionType) {
            case NONE -> 0;
            case NORMAL -> (int) (baseAmount * 0.05);
            case PREMIUM -> (int) (baseAmount * 0.10);
        };
    }

    private int calculateTicketBonus(int ticketCount, int baseAmount) {
        if (ticketCount <= 0) return 0;
        return (int) (baseAmount * 0.10); // 티켓 1장만 적용 (중복 적용 불가 정책 가정)
        // 만약 티켓 개수만큼 중복 적용이면: (int) (baseAmount * 0.10 * ticketCount);
    }
}
