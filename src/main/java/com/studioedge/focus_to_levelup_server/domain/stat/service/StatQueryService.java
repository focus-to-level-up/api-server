package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Query 서비스는 기본적으로 readOnly
public class StatQueryService {

    private final DailyStatService dailyStatService;
    private final WeeklyStatService weeklyStatService;
    private final MonthlyStatService monthlyStatService;
    private final TotalStatService totalStatService;

    public DailyStatListResponse getDailyStats(Long memberId, int year, int month) {
        return dailyStatService.getDailyStats(memberId, year, month);
    }

    public WeeklyStatListResponse getWeeklyStats(Long memberId, int year, int month) {
        return weeklyStatService.getWeeklyStats(memberId, year, month);
    }

    public MonthlyStatListResponse getMonthlyStats(Long memberId, int year) {
        return monthlyStatService.getMonthlyStats(memberId, year);
    }

    public TotalStatResponse getTotalStats(Member member, Integer period) {
        return totalStatService.getTotalStats(member, period);
    }

    public List<SubjectStatResponse> getWeeklySubjectStats(Member member, int year, int month) {
        return weeklyStatService.getWeeklySubjectStats(member, year, month);
    }

    // [추가] 월간 과목 통계
    public List<SubjectStatResponse> getMonthlySubjectStats(Member member, int year) {
        return monthlyStatService.getMonthlySubjectStats(member, year);
    }
}
