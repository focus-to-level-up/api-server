package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.SubjectStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.WeeklyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.WeeklyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklyStat;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklySubjectStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class WeeklyStatService {

    private final WeeklyStatRepository weeklyStatRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final WeeklySubjectStatRepository weeklySubjectStatRepository;
    private final DailySubjectRepository dailySubjectRepository;

    @Transactional(readOnly = true)
    public WeeklyStatListResponse getWeeklyStats(Long memberId, int year, int month) {

        LocalDate startDateOfMonth = LocalDate.of(year, month, 1);
        LocalDate today = getServiceDate();

        // 1. [집계 데이터] 조회할 달의 시작 ~ "이번 주 시작일" 전까지의 WeeklyStat 조회
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<WeeklyStat> aggregatedWeeks = weeklyStatRepository.findAllByMemberIdAndDateRange(
                memberId, startDateOfMonth, startOfThisWeek.minusDays(1)
        );

        // 2. DTO로 변환
        List<WeeklyStatResponse> responses = aggregatedWeeks.stream()
                .map(WeeklyStatResponse::of)
                .collect(Collectors.toList());

        // 3. [실시간 데이터] 조회하려는 달이 "현재 달"이고, "오늘"이 해당 월에 포함될 경우
        if (startDateOfMonth.getMonth().equals(today.getMonth()) &&
                startDateOfMonth.getYear() == today.getYear()) {

            // 3-1. "이번 주"의 DailyGoal 데이터를 실시간 조회
            List<DailyGoal> currentWeekGoals = dailyGoalRepository
                    .findAllByMemberIdAndDailyGoalDateBetween(memberId, startOfThisWeek, today);

            // 3-2. 실시간 데이터 합산
            int currentWeekMinutes = currentWeekGoals.stream()
                    .mapToInt(DailyGoal::getCurrentMinutes)
                    .sum();

            // 3-3. 현재 레벨과 이미지 URL 조회를 위해 MemberInfo 조회 (N+1 방지 쿼리 사용)
            MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                    .orElseThrow(InvalidMemberException::new);

            Member member = memberInfo.getMember();
            String imageUrl = memberInfo.getProfileImage().getAsset().getAssetUrl();

            // 3-4. 실시간 DTO 생성 및 리스트에 추가
            responses.add(WeeklyStatResponse.of(
                    startOfThisWeek,
                    today,
                    currentWeekMinutes,
                    member.getCurrentLevel(),
                    imageUrl
            ));
        }

        // 한 달동안의 총 집중 시간 합
        int totalFocusMinutes = responses.stream()
                .mapToInt(WeeklyStatResponse::totalFocusMinutes)
                .sum();

        return WeeklyStatListResponse.of(responses, totalFocusMinutes);
    }

    @Transactional(readOnly = true)
    public List<SubjectStatResponse> getWeeklySubjectStats(Member member, int year, int month) {

        LocalDate startDateOfMonth = LocalDate.of(year, month, 1);

        // "서비스 기준일" (새벽 4시 기준)
        LocalDate serviceDate = getServiceDate();
        LocalDate startOfThisWeek = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 1. [집계 데이터] 요청된 월의 "지난 주차" 통계 조회
        List<WeeklySubjectStat> pastWeeksStats = weeklySubjectStatRepository
                .findAllByMemberIdAndDateRangeWithSubject(
                        member.getId(),
                        startDateOfMonth,
                        startOfThisWeek.minusDays(1) // 이번 주 시작일 직전까지
                );

        // 2. [실시간 데이터] "이번 주" 데이터는 'DailySubject' 엔티티에서 직접 조회
        List<DailySubject> currentWeekStats;

        if (serviceDate.getYear() == year && serviceDate.getMonthValue() == month) {
            // [수정] SubjectRepository -> DailySubjectRepository
            currentWeekStats = dailySubjectRepository
                    .findAllByMemberIdAndDateRangeWithSubject(
                            member.getId(),
                            startOfThisWeek,
                            serviceDate // 오늘까지
                    );
        } else {
            currentWeekStats = List.of(); // 빈 리스트
        }

        // 3. (집계) 과목(Subject)을 기준으로 모든 시간(분)을 합산 (Map<Subject, Integer>)
        Map<Subject, Integer> totalMinutesPerSubject = pastWeeksStats.stream()
                .collect(Collectors.toMap(
                        WeeklySubjectStat::getSubject,
                        WeeklySubjectStat::getTotalMinutes,
                        Integer::sum // (같은 과목의 주차가 여러 개 있으면 합산)
                ));

        // 4. (집계) 3번 Map에 "이번 주" 실시간 데이터 덮어쓰기 (또는 추가)
        for (DailySubject stat : currentWeekStats) {
            // [수정] DailySubject의 focusSeconds를 분으로 변환하여 합산
            int minutes = stat.getFocusSeconds() / 60;
            totalMinutesPerSubject.merge(stat.getSubject(), minutes, Integer::sum);
        }


        // 5. (계산) 모든 과목의 총 합산 시간 계산
        double totalAllSubjectsMinutes = totalMinutesPerSubject.values().stream()
                .mapToDouble(Integer::doubleValue)
                .sum();

        // 6. (변환) Map을 DTO 리스트로 변환 (퍼센트 계산 포함)
        return totalMinutesPerSubject.entrySet().stream()
                .map(entry -> SubjectStatResponse.of(
                        entry.getKey(), // Subject
                        entry.getValue(), // TotalMinutes
                        totalAllSubjectsMinutes
                ))
                .collect(Collectors.toList());
    }
}
