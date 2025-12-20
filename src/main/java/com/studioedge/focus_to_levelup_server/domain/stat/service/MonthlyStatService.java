package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.MonthlyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class MonthlyStatService {

    private final MonthlyStatRepository monthlyStatRepository;
    private final DailyGoalRepository dailyGoalRepository;

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
     * 월간 상세 조회 (4개월 비교 + 일별 데이터)
     */
    @Transactional(readOnly = true)
    public MonthlyDetailResponse getMonthlyDetail(Long memberId, int year, int month, boolean init) {

        LocalDate serviceDate = getServiceDate();
        LocalDate targetDate = LocalDate.of(year, month, 1);

        // 1. 조회할 4개의 달(YearMonth) 리스트 구성
        List<LocalDate> queryMonths = new ArrayList<>();
        if (init) {
            for (int i = 0; i < 4; i++) {
                queryMonths.add(targetDate.plusMonths(i));
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                queryMonths.add(targetDate.minusMonths(i));
            }
        }

        // 2. 전체 조회 범위 계산 (쿼리 최적화용)
        // 리스트의 첫 번째 달의 1일 ~ 리스트의 마지막 달의 말일
        LocalDate globalStartDate = queryMonths.get(0);
        LocalDate globalEndDate = queryMonths.get(queryMonths.size() - 1).with(TemporalAdjusters.lastDayOfMonth());

        // 3. [DB 조회] 해당 기간의 모든 DailyGoal 한 번에 조회
        List<DailyGoal> allDailyGoals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                memberId, globalStartDate, globalEndDate
        );

        // 4. (Memory) 날짜별 초(Second) 데이터 매핑 (Map<LocalDate, Integer>)
        Map<LocalDate, Integer> dailyMap = allDailyGoals.stream()
                .collect(Collectors.toMap(
                        DailyGoal::getDailyGoalDate,
                        DailyGoal::getCurrentSeconds,
                        Integer::sum // 혹시 모를 중복 날짜 합산 처리
                ));

        // 5. 응답 데이터 생성
        List<MonthlyDetailResponse.MonthlyComparisonData> comparisonList = new ArrayList<>();

        for (LocalDate monthDate : queryMonths) {
            int currentYear = monthDate.getYear();
            int currentMonth = monthDate.getMonthValue();

            // 해당 월의 1일 ~ 말일 구하기
            LocalDate monthStart = monthDate;
            LocalDate monthEnd = monthDate.with(TemporalAdjusters.lastDayOfMonth());

            List<MonthlyDetailResponse.DailyFocusData> dailyList = new ArrayList<>();
            int monthlyTotalSeconds = 0;

            // 1일부터 말일까지 순회
            LocalDate iterDate = monthStart;
            while (!iterDate.isAfter(monthEnd)) {
                // 미래 날짜는 0으로 처리 (표시 안 함 or 0으로 표시 - 요구사항에 따라 조정 가능)
                // 여기서는 0으로 채워서 리스트 길이를 유지합니다 (그래프 그리기에 용이)
                int seconds = 0;

                // 서비스 날짜(오늘) 이전이거나 같은 경우만 데이터 가져옴
                if (!iterDate.isAfter(serviceDate)) {
                    seconds = dailyMap.getOrDefault(iterDate, 0);
                }

                monthlyTotalSeconds += seconds;

                dailyList.add(MonthlyDetailResponse.DailyFocusData.builder()
                        .date(iterDate)
                        .focusMinutes(seconds / 60)
                        .build());

                iterDate = iterDate.plusDays(1);
            }

            // DTO 추가
            comparisonList.add(MonthlyDetailResponse.MonthlyComparisonData.builder()
                    .year(currentYear)
                    .month(currentMonth)
                    .totalFocusMinutes(monthlyTotalSeconds / 60)
                    .dailyFocusList(dailyList)
                    .build());
        }

        return MonthlyDetailResponse.builder()
                .monthlyComparison(comparisonList)
                .build();
    }
}
