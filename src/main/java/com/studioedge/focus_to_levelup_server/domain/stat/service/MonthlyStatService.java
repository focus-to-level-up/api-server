package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.SubjectStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlySubjectStat;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyStatService {

    private final MonthlyStatRepository monthlyStatRepository;
    private final MonthlySubjectStatRepository monthlySubjectStatRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final DailySubjectRepository dailySubjectRepository;

    @Transactional(readOnly = true)
    public MonthlyStatListResponse getMonthlyStats(Long memberId, int year) {

        // 1. [집계 데이터] 해당 연도의 집계된 MonthlyStat 조회
        List<MonthlyStat> aggregatedMonths = monthlyStatRepository.findAllByMemberIdAndYear(memberId, year);

        // 2. Map으로 변환 (빠른 탐색용)
        Map<Integer, Integer> aggregatedMap = aggregatedMonths.stream()
                .collect(Collectors.toMap(MonthlyStat::getMonth, MonthlyStat::getTotalFocusMinutes));

        // 3. [실시간 데이터] "현재 달"의 데이터만 실시간 계산
        LocalDate today = LocalDate.now();
        Integer currentMonthLiveMinutes = 0;

        if (today.getYear() == year) {
            LocalDate startOfMonth = today.withDayOfMonth(1);
            List<DailyGoal> currentMonthGoals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                    memberId, startOfMonth, today
            );
            currentMonthLiveMinutes = currentMonthGoals.stream()
                    .mapToInt(DailyGoal::getCurrentMinutes)
                    .sum();
        }

        // 4. 1월~12월 DTO 생성
        List<MonthlyStatResponse> responses = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            if (year == today.getYear() && month == today.getMonthValue()) {
                // "현재 달"은 실시간 데이터로 덮어쓰기 (또는 추가)
                responses.add(MonthlyStatResponse.of(month, currentMonthLiveMinutes));
            } else {
                // "과거 달"은 집계 데이터 사용 (없으면 0)
                Integer totalMinutes = aggregatedMap.getOrDefault(month, 0);
                responses.add(MonthlyStatResponse.of(month, totalMinutes));
            }
        }

        // 한 달동안의 총 집중 시간 합
        int totalFocusMinutes = responses.stream()
                .mapToInt(MonthlyStatResponse::totalFocusMinutes)
                .sum();

        return MonthlyStatListResponse.of(responses, totalFocusMinutes);
    }

    @Transactional(readOnly = true)
    public List<SubjectStatResponse> getMonthlySubjectStats(Member member, int year) {

        // "서비스 기준일" (새벽 4시 기준)
        LocalDate serviceDate = AppConstants.getServiceDate();
        int currentMonth = serviceDate.getMonthValue();

        // 1. [집계 데이터] 요청된 연도의 "지난 달"까지의 통계 조회
        List<MonthlySubjectStat> pastMonthsStats = monthlySubjectStatRepository
                .findAllByMemberIdAndYearAndMonthBeforeWithSubject(
                        member.getId(),
                        year,
                        currentMonth
                );

        // 2. (집계) 과목(Subject)을 기준으로 합산 (Map<Subject, Integer>)
        Map<Subject, Integer> totalMinutesPerSubject = pastMonthsStats.stream()
                .collect(Collectors.toMap(
                        MonthlySubjectStat::getSubject,
                        MonthlySubjectStat::getTotalMinutes,
                        Integer::sum
                ));

        // 3. [실시간 데이터] 요청된 연도가 "현재 연도"일 경우, "이번 달" 데이터 실시간 집계
        if (serviceDate.getYear() == year) {

            // 3-1. "현재 월"의 시작일 ~ 오늘까지의 DailySubject 데이터 조회
            LocalDate startOfThisMonth = serviceDate.withDayOfMonth(1);

            List<DailySubject> currentMonthStats = dailySubjectRepository
                    .findAllByMemberIdAndDateRangeWithSubject(
                            member.getId(),
                            startOfThisMonth,
                            serviceDate // 오늘까지
                    );

            // 3-2. (집계) "현재 월"의 실시간 데이터를 2번 Map에 합산
            for (DailySubject stat : currentMonthStats) {
                int minutes = stat.getFocusSeconds() / 60; // 초 -> 분
                totalMinutesPerSubject.merge(stat.getSubject(), minutes, Integer::sum);
            }
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
