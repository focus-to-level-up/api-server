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
import com.studioedge.focus_to_levelup_server.domain.store.service.ItemAchievementService;
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
    private final ItemAchievementService itemAchievementService;
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
     * 목표 보상 수령 (오늘의 학습 종료)
     * - 보상 수령 처리
     * - "휴식은 사치" 미션 성공 판정
     */
    @Transactional
    public void receiveDailyGoal(Member m, ReceiveDailyGoalRequest request) {
        LocalDate serviceDate = AppConstants.getServiceDate();
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(m.getId(), serviceDate)
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

        // "휴식은 사치" 미션 성공 판정 (오늘의 학습 종료 시점에 판정)
        itemAchievementService.checkRestIsLuxuryOnStudyEnd(m.getId(), serviceDate, dailyGoal);
    }
}
