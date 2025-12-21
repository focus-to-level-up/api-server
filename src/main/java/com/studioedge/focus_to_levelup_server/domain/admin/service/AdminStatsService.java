package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.CategoryDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.FocusTimeDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.GenderDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final DailyGoalRepository dailyGoalRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 일간 집중시간 분포 (2시간 단위)
     * 0~2시간, 2~4시간, 4~6시간, 6~8시간, 8~10시간, 10시간 이상
     */
    public FocusTimeDistributionResponse getDailyFocusTimeDistribution(LocalDate date) {
        List<Integer> dailySeconds = dailyGoalRepository.findAllDailySecondsByDate(date);

        // 2시간 = 7200초, 시간대 경계값 (분 단위로 변환)
        int[] boundaries = {0, 120, 240, 360, 480, 600, Integer.MAX_VALUE}; // 분 단위
        String[] labels = {"0~2시간", "2~4시간", "4~6시간", "6~8시간", "8~10시간", "10시간 이상"};

        return calculateDistribution(dailySeconds.stream().map(s -> (long) s).toList(), boundaries, labels);
    }

    /**
     * 주간 집중시간 분포 (5시간 단위)
     * 0~5시간, 5~10시간, ..., 45~50시간, 50시간 이상
     */
    public FocusTimeDistributionResponse getWeeklyFocusTimeDistribution(LocalDate date) {
        // 해당 날짜가 속한 주의 월~일 구하기
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(DayOfWeek.SUNDAY);

        List<Long> weeklySeconds = dailyGoalRepository.findAllWeeklySecondsBetween(startOfWeek, endOfWeek);

        // 5시간 = 18000초, 시간대 경계값 (분 단위)
        int[] boundaries = {0, 300, 600, 900, 1200, 1500, 1800, 2100, 2400, 2700, 3000, Integer.MAX_VALUE}; // 분 단위
        String[] labels = {"0~5시간", "5~10시간", "10~15시간", "15~20시간", "20~25시간",
                "25~30시간", "30~35시간", "35~40시간", "40~45시간", "45~50시간", "50시간 이상"};

        return calculateDistribution(weeklySeconds, boundaries, labels);
    }

    private FocusTimeDistributionResponse calculateDistribution(List<Long> secondsList, int[] boundaries, String[] labels) {
        int totalUsers = secondsList.size();
        int[] counts = new int[labels.length];

        for (Long seconds : secondsList) {
            int minutes = (int) (seconds / 60);
            for (int i = 0; i < boundaries.length - 1; i++) {
                if (minutes >= boundaries[i] && minutes < boundaries[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
        }

        List<FocusTimeDistributionResponse.TimeRangeStats> distribution = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            double percentage = totalUsers > 0 ? Math.round(counts[i] * 1000.0 / totalUsers) / 10.0 : 0;
            int maxMinutes = boundaries[i + 1] == Integer.MAX_VALUE ? -1 : boundaries[i + 1];
            distribution.add(new FocusTimeDistributionResponse.TimeRangeStats(
                    labels[i],
                    boundaries[i],
                    maxMinutes,
                    counts[i],
                    percentage
            ));
        }

        return new FocusTimeDistributionResponse(totalUsers, distribution);
    }

    /**
     * 카테고리 분포
     */
    public CategoryDistributionResponse getCategoryDistribution() {
        List<Object[]> results = memberInfoRepository.countByCategorySub();

        long totalUsers = results.stream().mapToLong(r -> (Long) r[1]).sum();

        List<CategoryDistributionResponse.CategoryStats> distribution = results.stream()
                .map(r -> {
                    CategorySubType category = (CategorySubType) r[0];
                    long count = (Long) r[1];
                    double percentage = totalUsers > 0 ? Math.round(count * 1000.0 / totalUsers) / 10.0 : 0;
                    return new CategoryDistributionResponse.CategoryStats(
                            category,
                            getCategoryName(category),
                            count,
                            percentage
                    );
                })
                .toList();

        return new CategoryDistributionResponse(totalUsers, distribution);
    }

    /**
     * 성별 분포
     */
    public GenderDistributionResponse getGenderDistribution() {
        List<Object[]> results = memberInfoRepository.countByGender();

        long totalUsers = results.stream().mapToLong(r -> (Long) r[1]).sum();

        List<GenderDistributionResponse.GenderStats> distribution = results.stream()
                .map(r -> {
                    Gender gender = (Gender) r[0];
                    long count = (Long) r[1];
                    double percentage = totalUsers > 0 ? Math.round(count * 1000.0 / totalUsers) / 10.0 : 0;
                    return new GenderDistributionResponse.GenderStats(
                            gender,
                            gender == Gender.MALE ? "남성" : "여성",
                            count,
                            percentage
                    );
                })
                .toList();

        return new GenderDistributionResponse(totalUsers, distribution);
    }

    private String getCategoryName(CategorySubType category) {
        if (category == null) return "미설정";
        return switch (category) {
            case ELEMENTARY -> "초등학생";
            case MIDDLE_1 -> "중1";
            case MIDDLE_2 -> "중2";
            case MIDDLE_3 -> "중3";
            case HIGH_1 -> "고1";
            case HIGH_2 -> "고2";
            case HIGH_3 -> "고3";
            case N_SU -> "N수생";
            case UNIVERSITY_STUDENT -> "대학생";
            case GRADUATE_STUDENT -> "대학원생";
            case EXAM_TAKER -> "고시생";
            case PUBLIC_SERVANT -> "공무원";
            case JOB_SEEKER -> "취준생";
            case OFFICE_WORKER -> "직장인";
        };
    }
}