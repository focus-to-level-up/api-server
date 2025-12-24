package com.studioedge.focus_to_levelup_server.global.batch.step.daily;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckFocusingIsOnStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final MemberRepository memberRepository;
    private final DailyGoalRepository dailyGoalRepository;

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
    @StepScope
    public JpaCursorItemReader<Member> checkFocusingIsOnReader() {
        return new JpaCursorItemReaderBuilder<Member>()
                .name("checkFocusingIsOnReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT m FROM Member m WHERE m.isFocusing = true ORDER BY m.id ASC")
                .build();
    }

    @Bean
    public ItemProcessor<Member, Member> checkFocusingIsOnProcessor() {
        return member -> {
            // @TODO: 4시간이상 집중한 유저는 이탈로 경고 처리

//            Optional<DailyGoal> dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(
//                    member.getId(), getServiceDate().minusDays(1)
//            );
//            boolean isStartedBeforeMidnight = false;
//            if (dailyGoal.isPresent()) {
//                LocalDateTime startTime = dailyGoal.get().getStartTime();
//                if (startTime != null) {
//                    if (startTime.isBefore(getServiceDate().atStartOfDay())) {
//                        isStartedBeforeMidnight = true;
//                    }
//                }
//            }
//            if (!isStartedBeforeMidnight) {
//                return member;
//            }

//            if (!member.getMemberSetting().warning()) {
//                member.banRanking();
//                rankingRepository.deleteByMemberId(member.getId());
//                log.info(">> 사용자 밴 처리 및 랭킹 삭제: {}", member.getNickname());
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
