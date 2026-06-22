package com.studioedge.step.daily;

import com.studioedge.common.policy.ServiceTimePolicy;
import com.studioedge.member.repository.MemberSettingRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberSetting;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import com.studioedge.ranking.domain.RankingBanPolicy;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Ranking;
import com.studioedge.common.enums.CategoryMainType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestoreExcludeRankingStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberSettingRepository memberSettingRepository;
    private final RankingRepository rankingRepository;
    private final LeagueRepository leagueRepository;

    @Bean
    public Step restoreExcludeRanking() {
        return new StepBuilder("restoreExcludeRanking", jobRepository)
                .<MemberSetting, Member> chunk(25, platformTransactionManager)
                .reader(restoreExcludeRankingReader())
                .processor(restoreExcludeRankingProcessor())
                .writer(restoreExcludeRankingWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<MemberSetting> restoreExcludeRankingReader() {
        return new RepositoryItemReaderBuilder<MemberSetting>()
                .name("checkFocusingIsOnReader")
                .pageSize(25)
                .methodName("findBannedMembersWithExpiredWarning")
                .repository(memberSettingRepository)
                .arguments(RankingBanPolicy.restoreCutoff(ServiceTimePolicy.getServiceDate()))
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
            // 1. 유저를 카테고리별로 그룹화 (In-Memory)
            Map<CategoryMainType, List<Member>> membersByCategory;
            membersByCategory = chunk.getItems().stream()
                    .collect(Collectors.groupingBy(member -> member.getMemberInfo().getCategoryMain()));

            List<Ranking> newRankings = new ArrayList<>();

            // 2. 카테고리 그룹(최대 4개)별로 반복
            for (Map.Entry<CategoryMainType, List<Member>> entry : membersByCategory.entrySet()) {
                CategoryMainType category = entry.getKey();
                List<Member> membersInGroup = entry.getValue();

                // 3. (DB 쿼리) 카테고리별 "가장 인원이 적은" 브론즈 리그 조회
                League targetLeague = leagueRepository.findSmallestBronzeLeagueForCategory(category)
                        .orElseThrow(() -> new IllegalStateException("배치 가능한 브론즈 리그가 없습니다 (Category: " + category + ")"));

                // 4. Ranking 객체 생성 (In-Memory)
                for (Member member : membersInGroup) {
                    targetLeague.increaseCurrentMembers();
                    newRankings.add(Ranking.builder()
                            .league(targetLeague)
                            .member(member)
                            .tier(RankingBanPolicy.restoreTier())
                            .build());
                }

                log.info(">> (Category: {}) {}명의 유저를 League ID: {}에 배치합니다.", category, membersInGroup.size(), targetLeague.getId());
            }

            // 5. (DB 쿼리) 신규 랭킹 데이터 일괄 저장
            rankingRepository.saveAll(newRankings);
        };
    }
}
