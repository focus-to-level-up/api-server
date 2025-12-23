package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklyStat;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklySubjectStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpdateWeeklyStatStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final DailyGoalRepository dailyGoalRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final MemberRepository memberRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;
    private final WeeklyStatRepository weeklyStatRepository;
    private final WeeklySubjectStatRepository weeklySubjectStatRepository;

    private final Clock clock;
    private record WeeklyStatContainer(
            WeeklyStat weeklyStat,
            List<WeeklySubjectStat> subjectStats
    ) {}

    @Bean
    public Step updateWeeklyStat() {
        return new StepBuilder("updateWeeklyStat", jobRepository)
                .<Member, WeeklyStatContainer> chunk(100, platformTransactionManager)
                .reader(updateWeeklyStatReader())
                .processor(updateWeeklyStatProcessor())
                .writer(updateWeeklyStatWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> updateWeeklyStatReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("updateWeeklyStatReader")
                .pageSize(100)
                .methodName("findAllByStatus")
                .repository(memberRepository)
                .arguments(MemberStatus.ACTIVE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope // Step 실행 시점의 날짜를 계산하기 위해 @StepScope 추가
    public ItemProcessor<Member, WeeklyStatContainer> updateWeeklyStatProcessor() {

        LocalDate today = LocalDate.now(clock);
        LocalDate endDate = today.minusDays(1);   // 지난주 일요일
        LocalDate startDate = today.minusDays(7); // 지난주 월요일

        log.info("WeeklyStats Processor: 집계 기간 {} ~ {}", startDate, endDate);

        return member -> {
            // 1. 유저의 지난주 DailyGoal 조회 (WeeklyStat용)
            List<DailyGoal> goals = dailyGoalRepository
                    .findAllByMemberIdAndDailyGoalDateBetween(member.getId(), startDate, endDate);

            // 2. 유저의 지난주 DailySubject 조회 (WeeklySubjectStat용)
            List<DailySubject> dailySubjects = dailySubjectRepository
                    .findAllByMemberIdAndDateBetween(member.getId(), startDate, endDate);

            // 3. 둘 다 데이터가 없으면 통계 생성 skip
            if (goals.isEmpty() && dailySubjects.isEmpty()) {
                log.warn("Member ID {} : 지난주 DailyGoal/DailySubject 데이터가 없어 통계 생성을 건너뜁니다.", member.getId());
                return null;
            }

            // 4. WeeklyStat 생성 로직
            WeeklyStat weeklyStat = null;
            if (!goals.isEmpty()) {
                weeklyStat = createWeeklyStatLogic(member, goals, startDate, endDate);
            }

            // 5. WeeklySubjectStat 생성 로직
            List<WeeklySubjectStat> subjectStats = null;
            if (!dailySubjects.isEmpty()) {
                subjectStats = createWeeklySubjectStatLogic(member, dailySubjects, startDate, endDate);
            }

            // 6. 래퍼 객체로 감싸서 Writer에 전달
            return new WeeklyStatContainer(weeklyStat, subjectStats);
        };
    }

    @Bean
    public ItemWriter<WeeklyStatContainer> updateWeeklyStatWriter() {
        return chunk -> {
            List<WeeklyStat> statsToWrite = chunk.getItems().stream()
                    .map(WeeklyStatContainer::weeklyStat)
                    .filter(Objects::nonNull) // DailyGoal 데이터가 없는 경우 null일 수 있음
                    .collect(Collectors.toList());

            List<WeeklySubjectStat> subjectStatsToWrite = chunk.getItems().stream()
                    .map(WeeklyStatContainer::subjectStats)
                    .filter(Objects::nonNull) // DailySubject 데이터가 없는 경우 null일 수 있음
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(statsToWrite)) {
                weeklyStatRepository.saveAll(statsToWrite);
                log.info(">> Saved {} WeeklyStat records.", statsToWrite.size());
            }
            if (!CollectionUtils.isEmpty(subjectStatsToWrite)) {
                weeklySubjectStatRepository.saveAll(subjectStatsToWrite);
                log.info(">> Saved {} WeeklySubjectStat records.", subjectStatsToWrite.size());
            }
        };
    }

    /**
     * WeeklyStat 생성
     */
    private WeeklyStat createWeeklyStatLogic(Member member, List<DailyGoal> goals,
                                             LocalDate startDate, LocalDate endDate) {
        // 3-1. 통계 집계
        int totalFocusMinutes = goals.stream()
                .mapToInt(DailyGoal::getCurrentSeconds)
                .sum() / 60;

        // 3-2. 대표 캐릭터 이미지 조회
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                .orElseThrow(() -> new IllegalStateException("현재 맴버의 대표 캐릭터가 설정되어있지 않습니다. ID: " + member.getId()));
        CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                memberCharacter.getCharacter().getId(),
                memberCharacter.getDefaultEvolution(),
                CharacterImageType.PICTURE
        ).orElseThrow(() -> new IllegalStateException("맴버의 대표 캐릭터 이미지를 조회할 수 없습니다. CharID: " + memberCharacter.getCharacter().getId()));

        // 3-3. WeeklyStat 엔티티 생성
        return WeeklyStat.builder()
                .member(member)
                .startDate(startDate)
                .endDate(endDate)
                .totalFocusMinutes(totalFocusMinutes)
                .totalLevel(member.getCurrentLevel())
                .lastCharacterImageUrl(characterImage.getImageUrl())
                .build();
    }

    /**
     * WeeklySubjectStat 생성
     */
    private List<WeeklySubjectStat> createWeeklySubjectStatLogic(Member member, List<DailySubject> dailySubjects,
                                                                 LocalDate startDate, LocalDate endDate) {

        // 4-1. DailySubject 리스트를 <Subject, 총합시간(초)> Map으로 집계
        Map<Subject, Integer> subjectSecondsMap = dailySubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getSubject,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 4-2. Map을 순회하며 WeeklySubjectStat 리스트 생성
        return subjectSecondsMap.entrySet().stream()
                .map(entry -> WeeklySubjectStat.builder()
                        .member(member)
                        .subject(entry.getKey())
                        .startDate(startDate)
                        .endDate(endDate)
                        .totalMinutes(entry.getValue() / 60)
                        .build())
                .collect(Collectors.toList());
    }
}
