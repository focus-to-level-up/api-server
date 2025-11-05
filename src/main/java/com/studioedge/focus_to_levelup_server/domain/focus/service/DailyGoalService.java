package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetDailyGoalResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.ReceiveDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyGoalService {
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
    public GetDailyGoalResponse getTodayDailyGoal(Member member) {
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberAndDailyGoalDate(member, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        return GetDailyGoalResponse.of(dailyGoal);
    }

    /**
     * 목표 보상 수령
     * */
    @Transactional
    public void receiveDailyGoal(Member member, Long dailyGoalId,
                                 ReceiveDailyGoalRequest request) {
        DailyGoal dailyGoal = dailyGoalRepository.findById(dailyGoalId)
                .orElseThrow(DailyGoalNotFoundException::new);
        dailyGoal.receiveReward();
        member.receiveDailyGoal(request);
    }
}
