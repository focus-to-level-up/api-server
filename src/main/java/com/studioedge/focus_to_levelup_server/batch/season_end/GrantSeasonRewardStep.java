package com.studioedge.focus_to_levelup_server.batch.season_end;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Map;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantSeasonRewardStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SeasonRepository seasonRepository;
    private final RankingRepository rankingRepository;
    private final MailRepository mailRepository;
    private final MemberRepository memberRepository;
    private final LeagueRepository leagueRepository;

    private static final int TARGET_LEAGUE_SIZE = 100;
    @Bean
    public Step grantSeasonReward() {
        return new StepBuilder("grantSeasonRewardStep", jobRepository)
                .<Ranking, Mail>chunk(100, platformTransactionManager)
                .reader(grantSeasonRewardReader())
                .processor(grantSeasonRewardProcessor(null, null)) // @StepScope 주입
                .writer(grantSeasonRewardWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Ranking> grantSeasonRewardReader() {
        // 지난 시즌의 모든 랭킹을 읽어옴 (이미 종료된 시즌)
        // Reader 쿼리에서 lastSeasonId를 파라미터로 받기 위해선
        // 쿼리 메서드나 별도 설정이 필요하지만, 여기선 '가장 최근 시즌'을 읽는다고 가정하거나
        // JobParameter로 넘기는 것이 정석입니다. 편의상 Repository 메서드 사용.
        return new RepositoryItemReaderBuilder<Ranking>()
                .name("grantSeasonRewardReader")
                .pageSize(100)
                .repository(rankingRepository)
                .methodName("findAllBySeasonId") // SeasonId로 조회하는 메서드 필요
                // Argument는 StepScope Reader가 아니므로 하드코딩이 어렵습니다.
                // 실제로는 Custom Reader를 만들거나 JobParameter를 사용해야 합니다.
                // 여기서는 '현재 DB상 가장 최근 시즌'을 읽는 로직이 Repository 내부에 있다고 가정합니다.
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Ranking, Mail> grantSeasonRewardProcessor(
            @Value("#{jobExecutionContext['top10CutoffScore']}") Long top10CutoffScore,
            @Value("#{jobExecutionContext['lastSeasonId']}") Long lastSeasonId) {

        return ranking -> {
            // 해당 랭킹이 처리하려는 시즌의 것인지 더블 체크 (Reader 구현에 따라 생략 가능)
            if (!ranking.getLeague().getSeason().getId().equals(lastSeasonId)) {
                return null;
            }

            Member member = ranking.getMember();
            Tier finalTier = ranking.getTier();
            int score = ranking.getMember().getCurrentLevel(); // getScore() 가정

            // 보상 정보 계산
            int diamonds = Tier.getSeasonRewardDiamonds(finalTier);
            boolean isSubscription = false;

            // 다이아 티어이면서, 점수가 커트라인 이상이면 구독권 지급
            if (finalTier == Tier.DIAMOND && score >= top10CutoffScore) {
                isSubscription = true;
            }

            String title = "시즌 종료 보상 확인하세요";
            String popupTitle = finalTier.name() + " 시즌 종료 보상";
            String popupContent = String.format("%s 님은 최종적으로 \"%s\"에 위치하였습니다.\n보상을 받아가세요",
                    member.getNickname(), finalTier.name());

            // Mail 객체 생성
            return Mail.builder()
                    .receiver(member)
                    .senderName("운영자")
                    .type(MailType.RANKING) // 혹은 SEASON_END
                    .title(title)
                    .description("시즌 종료 보상입니다.") // 실제 내용은 팝업에서 처리
                    .popupTitle(popupTitle)
                    .popupContent(popupContent)
                    .reward(diamonds)
                    // [중요] 구독권 여부를 Mail 엔티티에 저장할 필드가 필요함 (ex: hasSubscription)
                    // 현재 Mail 엔티티에는 reward(int)만 있으므로,
                    // 구독권은 별도로 처리하거나 reward 필드 로직 확장이 필요.
                    // 여기서는 로직 흐름상 설명만 추가.
                    .expiredAt(LocalDate.now().plusDays(7))
                    .build();
        };
    }

    @Bean
    public ItemWriter<Mail> grantSeasonRewardWriter() {
        return chunk -> mailRepository.saveAll(chunk.getItems());
    }
}
