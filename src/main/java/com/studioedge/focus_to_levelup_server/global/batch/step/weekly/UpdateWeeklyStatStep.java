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
import org.springframework.batch.core.SkipListener;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * [Weekly Job - Step 1] 주간 통계 집계
 *
 * 동작 흐름:
 * 1. Reader: 상태가 'ACTIVE'인 모든 멤버를 페이지 단위(100명)로 조회합니다.
 * 2. Processor:
 * - 배치 실행 시점 기준 지난주 월요일 ~ 일요일 날짜를 계산합니다.
 * - 각 멤버의 지난주 DailyGoal(일일 목표)과 DailySubject(과목별 학습) 데이터를 조회합니다.
 * - 데이터가 존재할 경우, 총 집중 시간 및 과목별 학습 시간을 집계합니다.
 * - 멤버의 현재 대표 캐릭터 및 이미지를 조회하여 통계에 스냅샷으로 저장합니다.
 * - 집계된 WeeklyStat과 WeeklySubjectStat 리스트를 Container 객체에 담아 반환합니다.
 * 3. Writer:
 * - Container에서 WeeklyStat과 WeeklySubjectStat 리스트를 추출합니다.
 * - 각 Repository를 통해 DB에 일괄 저장(Bulk Insert)합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpdateWeeklyStatStep {private final JobRepository jobRepository;
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
                .faultTolerant()
                .skip(IllegalStateException.class)
                .skip(NullPointerException.class)
                .skip(DataIntegrityViolationException.class)
                .skipLimit(200)
                .retry(DeadlockLoserDataAccessException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new WeeklyStatSkipListener())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> updateWeeklyStatReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("updateWeeklyStatReader")
                .pageSize(100)
                .methodName("findAllByStatusIn")
                .repository(memberRepository)
                .arguments(Collections.singletonList(List.of(MemberStatus.ACTIVE, MemberStatus.RANKING_BANNED)))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, WeeklyStatContainer> updateWeeklyStatProcessor() {

        LocalDate today = LocalDate.now(clock);
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = today.minusDays(7);

        log.info("WeeklyStats Processor: 집계 기간 {} ~ {}", startDate, endDate);

        return member -> {
            List<DailyGoal> goals = dailyGoalRepository
                    .findAllByMemberIdAndDailyGoalDateBetween(member.getId(), startDate, endDate);

            List<DailySubject> dailySubjects = dailySubjectRepository
                    .findAllByMemberIdAndDateBetween(member.getId(), startDate, endDate);

            if (goals.isEmpty() && dailySubjects.isEmpty()) {
                return null;
            }

            WeeklyStat weeklyStat = null;
            if (!goals.isEmpty()) {
                weeklyStat = createWeeklyStatLogic(member, goals, startDate, endDate);
            }
            List<WeeklySubjectStat> subjectStats = null;
            if (!dailySubjects.isEmpty()) {
                subjectStats = createWeeklySubjectStatLogic(member, dailySubjects, startDate, endDate);
            }

            return new WeeklyStatContainer(weeklyStat, subjectStats);
        };
    }

    @Bean
    public ItemWriter<WeeklyStatContainer> updateWeeklyStatWriter() {
        return chunk -> {
            List<WeeklyStat> statsToWrite = chunk.getItems().stream()
                    .map(WeeklyStatContainer::weeklyStat)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<WeeklySubjectStat> subjectStatsToWrite = chunk.getItems().stream()
                    .map(WeeklyStatContainer::subjectStats)
                    .filter(Objects::nonNull)
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

    public static class WeeklyStatSkipListener implements SkipListener<Member, WeeklyStatContainer> {
        @Override
        public void onSkipInRead(Throwable t) {
            log.error(">> [Skip] 읽기 중 에러 발생: {}", t.getMessage());
        }

        @Override
        public void onSkipInProcess(Member member, Throwable t) {
            log.error(">> [Skip] 처리 중 에러 발생 (MemberID: {}): {}", member.getId(), t.getMessage());
        }

        @Override
        public void onSkipInWrite(WeeklyStatContainer item, Throwable t) {
            Member member = item.weeklyStat() != null ? item.weeklyStat().getMember() : null;
            Long memberId = (member != null) ? member.getId() : -1L;
            log.error(">> [Skip] 쓰기 중 에러 발생 (MemberID: {}): {}", memberId, t.getMessage());
        }
    }

    //-------------------------------------------- PRIVATE METHOD --------------------------------------------

    private WeeklyStat createWeeklyStatLogic(Member member, List<DailyGoal> goals,
                                             LocalDate startDate, LocalDate endDate) {
        int totalFocusMinutes = goals.stream()
                .mapToInt(DailyGoal::getCurrentSeconds)
                .sum() / 60;

        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                .orElseThrow(() -> new IllegalStateException("대표 캐릭터 미설정. ID: " + member.getId()));

        CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                memberCharacter.getCharacter().getId(),
                memberCharacter.getDefaultEvolution(),
                CharacterImageType.PICTURE
        ).orElseThrow(() -> new IllegalStateException("캐릭터 이미지 조회 실패. CharID: " + memberCharacter.getCharacter().getId()));

        return WeeklyStat.builder()
                .member(member)
                .startDate(startDate)
                .endDate(endDate)
                .totalFocusMinutes(totalFocusMinutes)
                .totalLevel(member.getCurrentLevel())
                .lastCharacterImageUrl(characterImage.getImageUrl())
                .build();
    }

    private List<WeeklySubjectStat> createWeeklySubjectStatLogic(Member member, List<DailySubject> dailySubjects,
                                                                 LocalDate startDate, LocalDate endDate) {
        Map<Subject, Integer> subjectSecondsMap = dailySubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getSubject,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

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
