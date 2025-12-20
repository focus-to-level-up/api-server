package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.ReceiveDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetDailyGoalResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.AlreadyReceivedDailyGoalException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalDuplicatedException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyGoalService {
    private final MemberRepository memberRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    /**
     * 목표 시간 설정
     * */
    @Transactional
    public void createDailyGoal(Member member, CreateDailyGoalRequest request) {
        LocalDate serviceDate = AppConstants.getServiceDate();
        if (dailyGoalRepository.findByMemberIdAndDailyGoalDate(member.getId(), serviceDate).isPresent()) {
            throw new DailyGoalDuplicatedException();
        }
        dailyGoalRepository.save(CreateDailyGoalRequest.from(member, request, serviceDate));
    }

    /**
     * 목표 시간 조회
     * */
    @Transactional(readOnly = true)
    public GetDailyGoalResponse getTodayDailyGoal(Member member, LocalDate date) {
        LocalDate serviceDate = date == null ? AppConstants.getServiceDate() : date;
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(member.getId(), serviceDate)
                .orElseThrow(DailyGoalNotFoundException::new);
        return GetDailyGoalResponse.of(dailyGoal);
    }

    /**
     * 목표 보상 수령
     * */
    @Transactional
    public void receiveDailyGoal(Member m, Long dailyGoalId, ReceiveDailyGoalRequest request) {
        DailyGoal dailyGoal = dailyGoalRepository.findById(dailyGoalId)
                .orElseThrow(DailyGoalNotFoundException::new);
        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                .orElseThrow(CharacterDefaultNotFoundException::new);

        if (!dailyGoal.receiveReward()) {
            throw new AlreadyReceivedDailyGoalException();
        }

        member.expUp(request.rewardExp());
        member.getMemberInfo().totalExpUp(request.rewardExp());
        memberCharacter.expUp(request.rewardExp());
    }
}
