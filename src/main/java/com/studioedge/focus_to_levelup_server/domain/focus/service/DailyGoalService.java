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

        // "휴식은 사치" 미션 성공 판정 (오늘의 학습 종료 시점에 판정)
        itemAchievementService.checkRestIsLuxuryOnStudyEnd(m.getId(), dailyGoal.getDailyGoalDate(), dailyGoal);
    }

    @Transactional
    public void receiveDailyGoalV2(Member m, Long dailyGoalId) {
        DailyGoal dailyGoal = dailyGoalRepository.findById(dailyGoalId)
                .orElseThrow(DailyGoalNotFoundException::new);
        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                .orElseThrow(CharacterDefaultNotFoundException::new);

        if (!dailyGoal.receiveReward()) {
            throw new AlreadyReceivedDailyGoalException();
        }

        int rewardExp = calculateBonusExp(dailyGoal);
        if (rewardExp > 0) {
            member.expUp(rewardExp);
            member.getMemberInfo().totalExpUp(rewardExp);
            memberCharacter.expUp(rewardExp);
        }

        // 5. "휴식은 사치" 미션 성공 판정
        itemAchievementService.checkRestIsLuxuryOnStudyEnd(m.getId(), dailyGoal.getDailyGoalDate(), dailyGoal);
    }

    /**
     * 보상(보너스) 경험치 계산 로직
     * (GetDailyGoalResponse에 있던 로직을 서버 내부로 가져옴)
     */
    private int calculateBonusExp(DailyGoal dailyGoal) {
        int currentMinutes = dailyGoal.getCurrentSeconds() / 60;
        int targetMinutes = dailyGoal.getTargetMinutes();

        // 1. 계산 기준 시간(x) 산정
        int x;
        if (currentMinutes >= targetMinutes) {
            // 목표 달성 시: x = 목표 시간(시간 단위)
            x = targetMinutes / 60;
        } else {
            // 목표 미달 시: x = (현재 시간 / 60) - 1
            x = (currentMinutes / 60) - 1;
        }

        // 2. 보상 배율 계산 (f(x) = 1.1^(x-2))
        float rewardMultiplier = 1.0f;
        if (x >= 2) {
            double rawMultiplier = Math.pow(1.1, x - 2);
            rewardMultiplier = (float) (Math.round(rawMultiplier * 100.0) / 100.0);
        }

        // 3. 보너스 경험치 계산
        int baseExp = currentMinutes * 10;

        // 추가로 줄 보너스 경험치 = 기본 * (배율 - 1)
        return (int) (baseExp * (rewardMultiplier - 1.0f));
    }
}
