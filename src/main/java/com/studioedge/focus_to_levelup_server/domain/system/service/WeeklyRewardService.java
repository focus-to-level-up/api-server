package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.BonusTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.dao.WeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.request.ReceiveWeeklyRewardRequest;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.WeeklyRewardInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.system.exception.WeeklyRewardAlreadyReceivedException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.WeeklyRewardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyRewardService {

    private final MemberRepository memberRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BonusTicketRepository bonusTicketRepository;

    @Transactional(readOnly = true)
    public WeeklyRewardInfoResponse getWeeklyRewardInfo(Member member) {
        if (!member.getIsReceivedWeeklyReward()) {
            throw new WeeklyRewardAlreadyReceivedException();
        }
        WeeklyReward weeklyReward = weeklyRewardRepository.findFirstByMemberIdOrderByCreatedAtDesc(member.getId())
                .orElseThrow(WeeklyRewardNotFoundException::new);
        SubscriptionType type = subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId())
                .map(Subscription::getType)
                .orElse(SubscriptionType.NONE);

        // @TODO: memberInfo -> 보너스티켓수 확인. 티켓 > 0 ? true : false
        boolean hasTicket = bonusTicketRepository.findByMemberId(member.getId()).isPresent();
//
//        boolean hasTicket;
//        Optional<BonusTicket> bonusTicket = bonusTicketRepository.findByMemberId(member.getId());
//        if (bonusTicket.isEmpty() || bonusTicket.get()) {
//            hasTicket = false;
//        } else {
//            hasTicket = true;
//        }
        return WeeklyRewardInfoResponse.of(weeklyReward, type, hasTicket);
    }

    @Transactional
    public void receiveWeeklyReward(Long memberId, ReceiveWeeklyRewardRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        member.receiveWeeklyReward();
        member.getMemberInfo().addDiamond(request.rewardDiamond());
        weeklyRewardRepository.deleteById(request.weeklyRewardId());
        // @TODO: memberInfo 보너스 티켓 차감
    }
}
