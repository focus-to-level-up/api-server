package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
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
        Integer currentMonthLiveSeconds = 0;

        if (today.getYear() == year) {
            LocalDate startOfMonth = today.withDayOfMonth(1);
            List<DailyGoal> currentMonthGoals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                    memberId, startOfMonth, today
            );
            currentMonthLiveSeconds = currentMonthGoals.stream()
                    .mapToInt(DailyGoal::getCurrentSeconds)
                    .sum();
        }

        // 4. 1월~12월 DTO 생성
        List<MonthlyStatResponse> responses = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            if (year == today.getYear() && month == today.getMonthValue()) {
                // "현재 달"은 실시간 데이터로 덮어쓰기 (또는 추가)
                responses.add(MonthlyStatResponse.ofSeconds(month, currentMonthLiveSeconds));
            } else {
                // "과거 달"은 집계 데이터 사용 (없으면 0)
                Integer totalMinutes = aggregatedMap.getOrDefault(month, 0);
                responses.add(MonthlyStatResponse.ofMinutes(month, totalMinutes));
            }
        }

        // 한 달동안의 총 집중 시간 합
        int totalFocusMinutes = responses.stream()
                .mapToInt(MonthlyStatResponse::totalFocusMinutes)
                .sum();

        return MonthlyStatListResponse.of(responses, totalFocusMinutes);
    }

    /**
     * [신규] 월간 상세 조회 (4개월 비교 + 일별 데이터)
     */
    @Transactional(readOnly = true)
    public MonthlyDetailResponse getMonthlyDetail(Long memberId, int year, int month, boolean initial) {

        LocalDate serviceDate = AppConstants.getServiceDate(); // 오늘 (새벽 4시 기준)
        LocalDate targetDate = LocalDate.of(year, month, 1); // 선택한 달의 1일

        // --- 1. 4개월 비교 데이터 생성 (선택한 달 포함 과거 4개월) ---
        List<MonthlyDetailResponse.MonthlyComparisonData> comparisonList = new ArrayList<>();

        // 조회할 4개의 달을 시간순(과거 -> 미래)으로 리스트에 담습니다.
        List<LocalDate> queryMonths = new ArrayList<>();
        if (initial) {
            // init=true (클릭 시): 선택한 달부터 미래로 4개월 [Target, +1, +2, +3]
            for (int i = 0; i < 4; i++) {
                queryMonths.add(targetDate.plusMonths(i));
            }
        } else {
            // init=false (초기 진입): 선택한 달이 마지막이 되도록 과거 4개월 [Target-3, -2, -1, Target]
            for (int i = 3; i >= 0; i--) {
                queryMonths.add(targetDate.minusMonths(i));
            }
        }

        // i=3 (3달전) -> i=0 (이번달) 순서로 반복 (e.g., 8월, 9월, 10월, 11월)
        for (LocalDate queryDate : queryMonths) {
            int queryYear = queryDate.getYear();
            int queryMonth = queryDate.getMonthValue();

            int totalSeconds = 0;

            // 현재 달(진행 중)인 경우 -> DailyGoal 실시간 집계
            if (queryYear == serviceDate.getYear() && queryMonth == serviceDate.getMonthValue()) {
                List<DailyGoal> goals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                        memberId, queryDate, serviceDate
                );
                totalSeconds = goals.stream().mapToInt(DailyGoal::getCurrentSeconds).sum();
            }
            // 미래인 경우 -> 0
            else if (queryDate.isAfter(serviceDate)) {
                totalSeconds = 0;
            }
            // 과거 달(완료됨)인 경우 -> MonthlyStat 집계 데이터 조회
            else {
                totalSeconds = monthlyStatRepository.findByMemberIdAndYearAndMonth(memberId, queryYear, queryMonth)
                        .map(MonthlyStat::getTotalFocusMinutes)
                        .orElse(0);
            }

            comparisonList.add(MonthlyDetailResponse.MonthlyComparisonData.builder()
                    .year(queryYear)
                    .month(queryMonth)
                    .totalFocusMinutes(totalSeconds / 60)
                    .build());
        }


        // --- 2. 선택한 달의 일별 데이터 생성 (1일 ~ 말일/오늘) ---
        List<MonthlyDetailResponse.DailyFocusData> dailyFocusList;

        // 미래의 달을 조회한 경우 -> 빈 리스트
        if (targetDate.isAfter(serviceDate)) {
            dailyFocusList = Collections.emptyList();
        }
        else {
            LocalDate startDate = targetDate; // 1일
            LocalDate endDate;

            // 선택한 달이 '이번 달'이면 -> '오늘'까지만 조회 (e.g., 1일 ~ 16일)
            if (targetDate.getYear() == serviceDate.getYear() && targetDate.getMonthValue() == serviceDate.getMonthValue()) {
                endDate = serviceDate;
            }
            // 선택한 달이 '과거'이면 -> '말일'까지 조회 (e.g., 1일 ~ 30일)
            else {
                endDate = targetDate.with(TemporalAdjusters.lastDayOfMonth());
            }

            // DB에서 해당 기간의 DailyGoal 조회
            List<DailyGoal> dailyGoals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                    memberId, startDate, endDate
            );

            // Map으로 변환 (날짜 -> 시간)
            Map<LocalDate, Integer> goalMap = dailyGoals.stream()
                    .collect(Collectors.toMap(DailyGoal::getDailyGoalDate, DailyGoal::getCurrentSeconds));

            // 1일부터 endDate까지 리스트 생성 (빈 날짜는 0으로 채움)
            dailyFocusList = new ArrayList<>();
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                int seconds = goalMap.getOrDefault(current, 0);
                dailyFocusList.add(MonthlyDetailResponse.DailyFocusData.builder()
                        .date(current)
                        .focusMinutes(seconds / 60)
                        .build());
                current = current.plusDays(1);
            }
        }

        return MonthlyDetailResponse.builder()
                .monthlyComparison(comparisonList)
                .dailyFocusList(dailyFocusList)
                .build();
    }
}
