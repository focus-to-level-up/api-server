package com.studioedge.admin.stats;

import com.studioedge.admin.stats.dto.CategoryDistributionResponse;
import com.studioedge.admin.stats.dto.GenderDistributionResponse;
import com.studioedge.common.enums.CategorySubType;
import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.member.enums.Gender;
import com.studioedge.member.repository.MemberInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private DailyGoalRepository dailyGoalRepository;

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void sortsCategoriesByUserCountDescending() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{CategorySubType.HIGH_3, 10L});
        rows.add(new Object[]{CategorySubType.OFFICE_WORKER, 30L});
        rows.add(new Object[]{null, 5L});
        when(memberInfoRepository.countByCategorySub()).thenReturn(rows);

        CategoryDistributionResponse result = statsService.getCategoryDistribution();

        assertThat(result.distribution())
                .extracting(CategoryDistributionResponse.CategoryStats::categoryName)
                .containsExactly("직장인", "고3", "미설정");
    }

    @Test
    void keepsMaleFemaleAndUnsetGenderAsSeparateGroups() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{Gender.FEMALE, 20L});
        rows.add(new Object[]{null, 5L});
        rows.add(new Object[]{Gender.MALE, 30L});
        when(memberInfoRepository.countByGender()).thenReturn(rows);

        GenderDistributionResponse result = statsService.getGenderDistribution();

        assertThat(result.totalUsers()).isEqualTo(55);
        assertThat(result.distribution())
                .extracting(GenderDistributionResponse.GenderStats::genderName)
                .containsExactly("남성", "여성", "미설정");
        assertThat(result.distribution())
                .extracting(GenderDistributionResponse.GenderStats::userCount)
                .containsExactly(30L, 20L, 5L);
    }
}
