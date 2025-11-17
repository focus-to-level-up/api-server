package com.studioedge.focus_to_levelup_server.batch;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyJobBatch {
    /**
     * 1. `clearPlannersStep`
     * 2. `deleteExpiredMailsStep`
     * 3. `checkSubscriptionsStep` -> ?
     * 4. `checkRankingWarningsStep`
     * 5. `checkFocusModeIsOn`
     * 6. `checkExcludeRanking`
     * */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final PlannerRepository plannerRepository;
    private final MailRepository mailRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final MemberRepository memberRepository;
    private final RankingRepository rankingRepository;
    private final LeagueRepository leagueRepository;


    @Bean
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(clearPlannerStep())
                .next(deleteExpiredMail())
                .next(restoreRankingWarning())
                .next(checkFocusingIsOn())
                .next(restoreExcludeRanking())
                .build();
    }

    // ------------------------------ CLEAR PLANNER ------------------------------

    @Bean
    public Step clearPlannerStep() {
        return new StepBuilder("clearPlannerStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    plannerRepository.deleteAllInBatch();
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    // ------------------------------ CHECK RANKING WARNING ------------------------------

    @Bean
    public Step restoreRankingWarning() {
        return new StepBuilder("restoreRankingWarning", jobRepository)
                .<MemberSetting, MemberSetting> chunk(10, platformTransactionManager)
                .reader(restoreRankingWarningReader())
                .processor(restoreRankingWarningProcessor())
                .writer(restoreRankingWarningWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<MemberSetting> restoreRankingWarningReader() {
        return new RepositoryItemReaderBuilder<MemberSetting>()
                .name("restoreRankingWarningReader")
                .pageSize(50)
                .methodName("findExpiredRankingCautions")
                .repository(memberSettingRepository)
                .arguments(List.of(LocalDate.now().minusWeeks(4)))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<MemberSetting, MemberSetting> restoreRankingWarningProcessor() {
        return new ItemProcessor<MemberSetting, MemberSetting>() {
            @Override
            public MemberSetting process(MemberSetting item) throws Exception {
                item.clearRankingWarning();
                return item;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<MemberSetting> restoreRankingWarningWriter() {
        return new RepositoryItemWriterBuilder<MemberSetting>()
                .repository(memberSettingRepository)
                .build();
    }

    // ------------------------------ DELETE EXPIRED MAIL ------------------------------

    @Bean
    public Step deleteExpiredMail() {
        return new StepBuilder("deleteExpiredMail", jobRepository)
                .<Mail, Mail> chunk(10, platformTransactionManager)
                .reader(deleteExpiredMailReader())
                .writer(deleteExpiredMailWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Mail> deleteExpiredMailReader() {
        return new RepositoryItemReaderBuilder<Mail>()
                .name("deleteExpiredMailReader")
                .pageSize(10)
                .methodName("findExpiredMails")
                .repository(mailRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<Mail> deleteExpiredMailWriter() {
        return chunk -> mailRepository.deleteAllInBatch((Iterable<Mail>) chunk.getItems());
    }

    // ------------------------------ CHECK FOCUS MODE IS ON ------------------------------

    @Bean
    public Step checkFocusingIsOn() {
        return new StepBuilder("checkFocusingIsOn", jobRepository)
                .<Member, Member> chunk(10, platformTransactionManager)
                .reader(checkFocusingIsOnReader())
                .processor(checkFocusingIsOnProcessor())
                .writer(checkFocusingIsOnWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> checkFocusingIsOnReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("checkFocusingIsOnReader")
                .pageSize(10)
                .methodName("findAllByIsFocusingIsTrue")
                .repository(memberRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Member, Member> checkFocusingIsOnProcessor() {
        return member -> {
            MemberSetting setting = member.getMemberSetting();
            boolean isBanned = setting.warning();
            if (isBanned) {
                member.banRanking();
                rankingRepository.deleteByMemberId(member.getId());
            }
            member.focusOff();

            return member;
        };
    }

    @Bean
    public RepositoryItemWriter<Member> checkFocusingIsOnWriter() {
        return new RepositoryItemWriterBuilder<Member>()
                .repository(memberRepository)
                .build();
    }


    // ------------------------------ CHECK RESTORE RANKING ------------------------------

    @Bean
    public Step restoreExcludeRanking() {
        return new StepBuilder("restoreExcludeRanking", jobRepository)
                .<MemberSetting, Member> chunk(100, platformTransactionManager)
                .reader(restoreExcludeRankingReader())
                .processor(restoreExcludeRankingProcessor())
                .writer(restoreExcludeRankingWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<MemberSetting> restoreExcludeRankingReader() {
        return new RepositoryItemReaderBuilder<MemberSetting>()
                .name("checkFocusingIsOnReader")
                .pageSize(100)
                .methodName("findBannedMembersWithExpiredWarning")
                .repository(memberSettingRepository)
                .arguments(LocalDate.now().minusWeeks(2))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<MemberSetting, Member> restoreExcludeRankingProcessor() {
        return memberSetting -> {
            Member member  = memberSetting.getMember();
            member.reactivate();
            memberSetting.clearRankingWarning();
            return member;
        };
    }

    @Bean
    public ItemWriter<Member> restoreExcludeRankingWriter() {
        return chunk -> {
            List<Member> members = (List<Member>) chunk.getItems();

            if (members.size() == 100) {

                rankingRepository.saveAll(newRankings);
            } else {

            }
            // 1. 100명 미만인 브론즈 리그를 찾습니다 (DB 조회 1회)
            // (리그가 없으면 예외 발생 -> Job 실패)
            League targetLeague = leagueRepository.findBronzeLeagueWithSpace()
                    .orElseThrow(() -> new IllegalStateException("No available bronze league found."));

            // 2. Ranking 객체 리스트 생성
            List<Ranking> newRankings = membersToRestore.stream()
                    .map(member -> Ranking.builder()
                            .league(targetLeague)
                            .member(member)
                            .build()) // Tier는 BRONZE가 기본값
                    .collect(Collectors.toList());

            // 3. 랭킹을 한 번에 저장

            log.info(">> Saved {} new rankings to League ID: {}", newRankings.size(), targetLeague.getId());

            // (트랜잭션 커밋 시, Processor에서 변경된 Member/MemberSetting도 자동 UPDATE 됨)
        };
    }
}
