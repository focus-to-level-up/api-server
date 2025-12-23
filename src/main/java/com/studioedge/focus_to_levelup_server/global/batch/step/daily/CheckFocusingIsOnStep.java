package com.studioedge.focus_to_levelup_server.global.batch.step.daily;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckFocusingIsOnStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final RankingRepository rankingRepository;


    @Bean
    public Step checkFocusingIsOn() {
        log.info("Step: ");
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
            boolean isJustWarned = setting.warning();

            Optional<DailyGoal> dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(
                    member.getId(), getServiceDate().minusDays(1)
            );

            boolean isStartedBeforeMidnight = false;
            if (dailyGoal.isPresent()) {
                LocalDateTime startTime = dailyGoal.get().getStartTime();
                if (startTime != null) {
                    LocalDateTime midnight = getServiceDate().atStartOfDay();
                    if (startTime.isBefore(midnight)) {
                        isStartedBeforeMidnight = true;
                    }
                }
            }
            if (!isStartedBeforeMidnight) {
                return member;
            }

//            if (!isJustWarned) {
//                log.info(">> 사용자 밴 처리 및 랭킹 삭제: {}", member.getNickname());
//                member.banRanking();
//                rankingRepository.deleteByMemberId(member.getId());
//            } else {
//                log.info(">> 사용자 경고 부여: {}", member.getNickname());
//            }

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
}
