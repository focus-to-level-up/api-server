package com.studioedge.focus_to_levelup_server.batch.monthly;


import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpdateMonthlyStatStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MonthlyStatRepository monthlyStatRepository;
    private final DailyGoalRepository dailyGoalRepository;

    @Bean
    public Step updateMonthlyStat(ItemProcessor<Member, Member> passThroughMemberProcessor) {
        return new StepBuilder("updateMonthlyStat", jobRepository)
                .<Member, Member> chunk(100, platformTransactionManager)
                .reader(updateMonthlyStatReader())
                .processor(passThroughMemberProcessor)
                .writer(updateMonthStatWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> updateMonthlyStatReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("updateMonthlyStatReader")
                .pageSize(100)
                .methodName("findAll")
                .repository(memberRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<Member> updateMonthStatWriter() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Integer lastMonthValue = firstDayOfLastMonth.getMonthValue();

        return chunk -> {
            List<Member> members = (List<Member>) chunk.getItems();
            List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());

            // 1. [DB 쿼리 1회] Chunk(100명)의 '지난달' DailyGoal 통계를 DB에서 일괄 집계
            // (DailyGoalRepository에 DTO와 쿼리 추가 필요)
            Map<Long, Integer> statsMap = dailyGoalRepository.findMonthlyStatsByMemberIds(
                    memberIds, firstDayOfLastMonth, lastDayOfLastMonth
            ).stream().collect(Collectors.toMap(
                    DailyGoalRepository.MonthlyFocusStat::getMemberId,
                    DailyGoalRepository.MonthlyFocusStat::getTotalSeconds
            ));

            List<MonthlyStat> newMonthlyStats = new ArrayList<>();

            // 2. (In-Memory) 집계 결과를 바탕으로 MonthlyStat 엔티티 생성
            for (Member member : members) {
                // 집계된 총 시간(초)을 가져옴 (데이터 없으면 0)
                int totalSeconds = statsMap.getOrDefault(member.getId(), 0);

                // [REFACTOR] 지난달에 집중 시간이 0초인 유저는 통계 생성 안 함 (선택적)
                if (totalSeconds == 0) {
                    continue;
                }

                MonthlyStat monthlyStat = MonthlyStat.builder()
                        .member(member)
                        .year(LocalDate.now().getYear())
                        .month(lastMonthValue)
                        .totalFocusMinutes(totalSeconds / 60) // 초 -> 분 변환
                        .build();

                newMonthlyStats.add(monthlyStat);
            }

            // 3. [DB 쿼리 1회] 생성된 MonthlyStat 리스트 일괄 저장
            if (!CollectionUtils.isEmpty(newMonthlyStats)) {
                monthlyStatRepository.saveAll(newMonthlyStats);
                log.info(">> Saved {} MonthlyStat records.", newMonthlyStats.size());
            }
        };
    }
}
