package com.studioedge.focus_to_levelup_server.domain.focus.service;


import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.service.TrainingRewardService;
import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.store.service.ItemAchievementService;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class FocusServiceV4 {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubjectRepository subjectRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final SchoolRepository schoolRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final ItemAchievementService itemAchievementService;
    private final TrainingRewardService trainingRewardService;
    private final PlannerRepository plannerRepository;

    @Transactional
    public void saveFocus(Member m, Long subjectId) {
        /*
         * 4시 경계 체크 및 시간 보정 로직 (추가된 부분)
         * - 시작 시간을 기준으로 '다음 4시'를 구합니다.
         * - 종료 시간이 4시를 넘어가면, '시작~4시'까지의 시간만 저장하도록 focusSeconds를 조정합니다.
         */
        LocalDate serviceDate = getServiceDate();
        // 1. DailyGoal 조회 (가장 최근 것)
        DailyGoal dailyGoal = dailyGoalRepository.findFirstByMemberIdOrderByDailyGoalDateDesc(m.getId())
                        .orElseThrow(DailyGoalNotFoundException::new);

        // 2. 시작 시간 검증
        if (dailyGoal.getStartTime() == null) {
            // 이미 저장이 되었거나, 시작 요청이 없었던 경우 예외 처리 또는 무시
            throw new IllegalArgumentException("진행 중인 집중 세션이 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime subjectStartTime = dailyGoal.getStartTime();
        LocalDateTime screenStartTime = dailyGoal.getScreenStartTime() == null ? subjectStartTime : dailyGoal.getScreenStartTime();
        int seconds = (int) Duration.between(subjectStartTime, now).getSeconds();

        if (seconds < 0) seconds = 0;
        LocalDateTime endTime = subjectStartTime.plusSeconds(seconds);

        LocalDateTime limitTime;
        if (subjectStartTime.getHour() < 4) {
            limitTime = subjectStartTime.toLocalDate().atTime(4, 0);
        } else {
            limitTime = subjectStartTime.toLocalDate().plusDays(1).atTime(4, 0);
        }

        // 5. 저장할 시간 보정 (4시 넘어가면 자름)
        int savedFocusSeconds = seconds;
        if (endTime.isAfter(limitTime)) {
            long durationUntilLimit = Duration.between(subjectStartTime, limitTime).getSeconds();
            savedFocusSeconds = (int) Math.max(0, durationUntilLimit);
            // 종료 시간도 4시로 고정 (플래너 저장용)
            endTime = limitTime;
        }

        int focusMinutes = savedFocusSeconds / 60;
        int remainSeconds = savedFocusSeconds % 60;

        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(m.getId())
                .orElseThrow(InvalidMemberException::new);
        Subject subject = subjectRepository.findByIdAndDeleteAtIsNull(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(m.getId(), true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        DailySubject dailySubject = dailySubjectRepository.findByMemberAndSubjectAndDate(member, subject, dailyGoal.getDailyGoalDate())
                .orElseGet(() -> {
                    // 오늘 해당 과목으로 공부한 기록이 없으면, 새로 생성
                    return DailySubject.builder()
                            .member(member)
                            .subject(subject)
                            .date(dailyGoal.getDailyGoalDate())
                            .build();
                });
        if (!subject.getMember().getId().equals(m.getId())) {
            throw new SubjectUnAuthorizedException();
        }

        // 초단위의 남은 시간이 60분보다 클 경우
        remainSeconds += dailySubject.getRemainSeconds();
        if (remainSeconds >= 60) {
            focusMinutes += 1;
            dailySubject.setRemainSeconds(remainSeconds - 60);
        } else {
            dailySubject.setRemainSeconds(remainSeconds);
        }

        int focusExp = focusMinutes * 10;
        // 레벨 업
        member.expUp(focusExp);
        // 총 레벨 업
        memberInfo.totalExpUp(focusExp);
        // 골드 획득
        memberInfo.addGold(focusExp);
        // 일일 목표 공부 시간 더하기
        dailyGoal.addCurrentSeconds(savedFocusSeconds);
        // 과목 공부 시간 더하기
        dailySubject.addSeconds(savedFocusSeconds);
        // 캐릭터 친밀도 상승
        memberCharacter.expUp(focusExp);
        // 집중 상태 해제
        member.focusOff();

        // 만약 dailySubject가 생성되어있지 않다면 저장해야함.
        dailySubjectRepository.save(dailySubject);
        dailySubjectRepository.flush();

        if (AppConstants.SCHOOL_CATEGORIES.contains(memberInfo.getCategoryMain()) &&
                !memberInfo.getCategorySub().equals(CategorySubType.N_SU) &&
                memberInfo.getSchool() != null && !memberInfo.getSchool().isBlank()) {
            schoolRepository.findByName(memberInfo.getSchool())
                    .ifPresent(school -> school.plusTotalLevel(focusExp));
        }

        // 길드 주간 집중 시간 업데이트 (가입한 모든 길드)
        List<GuildMember> guildMembers = guildMemberRepository.findAllByMemberIdWithGuild(m.getId());
        for (GuildMember gm : guildMembers) {
            gm.addWeeklyFocusTime(savedFocusSeconds);
            gm.getGuild().updateAverageFocusTime(savedFocusSeconds);
        }

        // 하루 최대 집중시간 확인하기
        long consecutiveSecondsLong = Duration.between(screenStartTime, now).getSeconds();
        int maxConsecutiveSeconds = (int) consecutiveSecondsLong;

        if (maxConsecutiveSeconds > dailyGoal.getMaxConsecutiveSeconds()) {
            dailyGoal.renewMaxConsecutiveSeconds(maxConsecutiveSeconds);
        }

        // 오늘 가장 빠른 시작 시각, 가장 늦은 종료 시각 업데이트
        dailyGoal.updateEarliestStartTime(screenStartTime.toLocalTime());
        dailyGoal.updateLatestEndTime(endTime.toLocalTime());

        // 아이템 달성 조건 체크 (DailySubject 저장 이후)
        itemAchievementService.checkAchievements(m.getId(), savedFocusSeconds, screenStartTime, dailyGoal);

        // 훈련 보상 적립
        trainingRewardService.accumulateTrainingReward(m.getId(), savedFocusSeconds);

        // 플래너 저장
        if (savedFocusSeconds > 0) {
            plannerRepository.save(
                    Planner.builder()
                            .member(member)
                            .subject(subject)
                            .date(dailyGoal.getDailyGoalDate())
                            .startTime(subjectStartTime.toLocalTime())
                            .endTime(endTime.toLocalTime())
                            .build()
            );
        }
    }
}
