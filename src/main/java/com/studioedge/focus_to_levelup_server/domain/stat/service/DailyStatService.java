package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.DailyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.DailyStatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DailyStatService {

    private final DailyGoalRepository dailyGoalRepository;

    @Transactional(readOnly = true)
    public DailyStatListResponse getDailyStats(Long memberId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 1. DB에서 해당 월의 DailyGoal 데이터를 한 번에 조회
        List<DailyGoal> goals = dailyGoalRepository.
                findAllByMemberIdAndDailyGoalDateBetween(memberId, startDate, endDate);

        // 'goals' 리스트에서 총 집중 시간을 미리 계산합니다.
        int totalFocusMinutes = goals.stream()
                .mapToInt(DailyGoal::getCurrentMinutes)
                .sum();

        // DailyGoal 리스트를 (LocalDate -> DailyGoal) 맵으로 변환 (빠른 탐색용)
        Map<LocalDate, DailyGoal> goalMap = goals.stream()
                .collect(Collectors.toMap(DailyGoal::getDailyGoalDate, Function.identity()));

        // 1일부터 말일까지 순회하며 DTO 생성
        List<DailyStatResponse> responses = IntStream.rangeClosed(1, endDate.getDayOfMonth())
                .mapToObj(day -> {
                    LocalDate date = startDate.withDayOfMonth(day);
                    DailyGoal goal = goalMap.get(date);

                    if (goal != null) {
                        return DailyStatResponse.of(goal); // 데이터가 있으면 DTO 생성
                    } else {
                        return DailyStatResponse.empty(date); // 데이터가 없으면 0으로 채움
                    }
                })
                .collect(Collectors.toList());

        return DailyStatListResponse.of(responses, totalFocusMinutes);
    }
}
