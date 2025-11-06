package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.ReceiveDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetDailyGoalResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.AlreadyReceivedDailyGoalException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyGoalService {
    private final MemberRepository memberRepository;
    private final DailyGoalRepository dailyGoalRepository;
    /**
     * 목표 시간 설정
     * */
    public void createDailyGoal(Member member, CreateDailyGoalRequest request) {
        dailyGoalRepository.save(CreateDailyGoalRequest.from(member, request));
    }

    /**
     * 목표 시간 조회
     * */
    @Transactional(readOnly = true)
    public GetDailyGoalResponse getTodayDailyGoal(Long memberId) {
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        return GetDailyGoalResponse.of(dailyGoal);
    }

    /**
     * 목표 보상 수령
     * */
    @Transactional
    public void receiveDailyGoal(Long memberId, ReceiveDailyGoalRequest request) {
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (dailyGoal.receiveReward()) {
            throw new AlreadyReceivedDailyGoalException();
        }
        member.levelUp(request.rewardExp());
        member.receiveDailyGoal(request);
    }
}
