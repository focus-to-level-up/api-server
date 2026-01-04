package com.studioedge.focus_to_levelup_server.global.batch.step.monthly;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.MonthlySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlySubjectStat;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpdateMonthlySubjectStatStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MonthlySubjectStatRepository monthlySubjectStatRepository;
    private final DailySubjectRepository dailySubjectRepository;

    private final Clock clock;

    @Bean
    public Step updateMonthlySubjectStat() {
        return new StepBuilder("updateMonthlySubjectStat", jobRepository)
                .<Member, Member> chunk(100, platformTransactionManager)
                .reader(updateMonthlySubjectStatReader())
                .processor(passThroughMemberProcessor())
                .writer(updateMonthlySubjectStatWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> updateMonthlySubjectStatReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("updateMonthlySubjectStatReader")
                .pageSize(100)
                .methodName("findAll")
                .repository(memberRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Member, Member> passThroughMemberProcessor() {
        return member -> member;
    }

    @Bean
    public ItemWriter<Member> updateMonthlySubjectStatWriter() {
        return chunk -> {

            LocalDate today = LocalDate.now(clock);
            LocalDate firstDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            Integer lastYearValue = firstDayOfLastMonth.getYear(); // MonthlySubjectStat은 year 필드가 있음
            Integer lastMonthValue = firstDayOfLastMonth.getMonthValue();

            log.info("MonthlySubjectStat Writer: 집계 대상 월 = {}-{} ({} ~ {})", lastYearValue, lastMonthValue, firstDayOfLastMonth, lastDayOfLastMonth);

            List<Member> members = (List<Member>) chunk.getItems();
            List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());

            // [REFACTOR] 멤버 ID로 Member 객체를 빠르게 찾기 위한 Map 생성
            Map<Long, Member> memberMap = members.stream()
                    .collect(Collectors.toMap(Member::getId, m -> m));

            // 1. [DB 쿼리 1회] Chunk(100명)의 '지난달' DailySubject 통계를 DB에서 일괄 집계
            // (DailySubjectRepository에 DTO와 쿼리 추가 필요)
            List<DailySubjectRepository.MonthlySubjectFocusStat> statsList =
                    dailySubjectRepository.findMonthlyStatsByMemberIds(
                            memberIds, firstDayOfLastMonth, lastDayOfLastMonth
                    );

            List<MonthlySubjectStat> newMonthlySubjectStats = new ArrayList<>();

            // 2. (In-Memory) 집계 결과를 바탕으로 MonthlySubjectStat 엔티티 생성
            for (DailySubjectRepository.MonthlySubjectFocusStat stat : statsList) {
                int totalSeconds = stat.getTotalSeconds();
                if (totalSeconds == 0) continue;

                // [REFACTOR] Map에서 Member 객체를 찾아 사용 (N+1 방지)
                Member member = memberMap.get(stat.getMemberId());
                Subject subject = stat.getSubject(); // 쿼리에서 Subject 엔티티를 이미 가져옴

                if (member == null || subject == null) {
                    log.warn("Member or Subject is null during aggregation. Skipping record.");
                    continue;
                }

                newMonthlySubjectStats.add(MonthlySubjectStat.builder()
                        .member(member)
                        .subject(subject)
                        .year(lastYearValue)
                        .month(lastMonthValue)
                        .totalMinutes(totalSeconds / 60) // 초 -> 분 변환
                        .build());
            }

            // 3. [DB 쿼리 1회] 생성된 MonthlySubjectStat 리스트 일괄 저장
            if (!CollectionUtils.isEmpty(newMonthlySubjectStats)) {
                monthlySubjectStatRepository.saveAll(newMonthlySubjectStats);
                log.info(">> Saved {} MonthlySubjectStat records.", newMonthlySubjectStats.size());
            }
        };
    }
}
