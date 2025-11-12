package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.TotalStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.UpdateTotalStatColorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TotalStatService {

    private final DailyGoalRepository dailyGoalRepository;
    private final MemberSettingRepository memberSettingRepository;

    @Transactional(readOnly = true)
    public TotalStatResponse getTotalStats(Member member, Integer period) {

        LocalDate today = LocalDate.now();
        LocalDate startDate;

        if (period == null) {
            startDate = member.getCreatedAt().toLocalDate();
        } else if (period == 1 || period == 3 || period == 6 || period == 12) {
            startDate = today.minusMonths(period);
        } else {
            throw new IllegalArgumentException("유효하지 않은 기간(period)입니다.");
        }

        // DB에서 해당 기간의 DailyGoal 데이터를 한 번에 조회
        List<DailyGoal> goals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                member.getId(), startDate, today
        );

        // 총합 및 평균 계산
        long totalMinutes = goals.stream()
                .mapToLong(DailyGoal::getCurrentMinutes)
                .sum();

        double averageMinutes = (goals.isEmpty()) ? 0 : (double) totalMinutes / goals.size();

        // 히트맵 데이터(DTO) 생성
        List<TotalStatResponse.HeatmapData> heatmapData = goals.stream()
                .map(TotalStatResponse.HeatmapData::from)
                .collect(Collectors.toList());

        // 최종 응답 DTO 반환
        return TotalStatResponse.builder()
                .totalMinutes(totalMinutes)
                .averageMinutes(averageMinutes)
                .heatmapData(heatmapData)
                .build();
    }

    @Transactional
    public void changeColor(Member member, UpdateTotalStatColorRequest request) {
        MemberSetting memberSetting = memberSettingRepository.findByMemberId(member.getId())
                .orElseThrow(InvalidMemberException::new);
        memberSetting.updateTotalStatColor(request.color());
    }
}
