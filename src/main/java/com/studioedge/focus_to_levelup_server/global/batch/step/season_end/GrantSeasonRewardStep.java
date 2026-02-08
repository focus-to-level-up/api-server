package com.studioedge.focus_to_levelup_server.global.batch.step.season_end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.dao.AssetRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantSeasonRewardStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MailRepository mailRepository;
    private final LeagueRepository leagueRepository;
    private final ObjectMapper objectMapper;
    private final AssetRepository assetRepository;

    @Bean
    public Step grantSeasonReward() {
        return new StepBuilder("grantSeasonReward", jobRepository)
                .<League, List<Mail>>chunk(20, platformTransactionManager)
                .reader(grantSeasonRewardReader())
                .processor(grantSeasonRewardProcessor())
                .writer(grantSeasonRewardWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<League> grantSeasonRewardReader() {
        // 종료된 시즌의 모든 리그를 조회
        LocalDate yesterday = LocalDate.now().minusDays(1);

        return new RepositoryItemReaderBuilder<League>()
                .name("grantSeasonRewardReader")
                .pageSize(20)
                .repository(leagueRepository)
                .methodName("findAllBySeasonEndDateWithRankings")
                .arguments(Collections.singletonList(yesterday))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<League, List<Mail>> grantSeasonRewardProcessor() {
        return league -> {
            List<Mail> mails = new ArrayList<>();

            // 리그 내 랭킹 가져오기
            List<Ranking> rankings = league.getRankings();
            rankings.sort((r1, r2) -> Integer.compare(r2.getMember().getCurrentLevel(), r1.getMember().getCurrentLevel())); // (점수 로직에 따라 변경)

            int totalMembers = rankings.size();
            Tier currentLeagueTier = league.getTier();

            for (int i = 0; i < totalMembers; i++) {
                Ranking ranking = rankings.get(i);
                Member member = ranking.getMember();

                Tier finalTier = Tier.determineNextTier(currentLeagueTier, (double) (i + 1) / totalMembers, true);

                // 1. 다이아 보상 메일 (기본)
                mails.add(createSeasonEndMail(member, finalTier));

                // 2. 프로필 테두리 보상 메일 (기본)
                mails.add(createProfileBorderMail(member, finalTier));
            }

            return mails;
        };
    }
    @Bean
    public ItemWriter<List<Mail>> grantSeasonRewardWriter() {
        return chunk -> {
            List<Mail> allMails = new ArrayList<>();
            for (List<Mail> leagueMails : chunk.getItems()) {
                allMails.addAll(leagueMails);
            }

            if (!allMails.isEmpty()) {
                mailRepository.saveAll(allMails);
                log.info(">> 시즌 종료 보상 메일 발송: {}건", allMails.size());
            }
        };
    }

    private Mail createSeasonEndMail(Member member, Tier finalTier) {
        // 보상 계산
        int diamonds = Tier.getSeasonRewardDiamonds(finalTier);

        String title = "시즌이 종료되었습니다. 최종 보상을 확인하세요";
        String popupTitle = finalTier.name() + " 시즌 종료 보상";
        String popupContent = String.format(
                "%s 님은 최종적으로 \"%s\"에 위치하였습니다.\n보상을 받아가세요.",
                member.getNickname(), finalTier.name()
        );

        return Mail.builder()
                .receiver(member)
                .senderName("Focus to Level Up")
                .type(MailType.SEASON_END)
                .title(title)
                .description("시즌 종료 보상입니다!\n")
                .popupTitle(popupTitle)
                .popupContent(popupContent)
                .reward(diamonds) // 다이아 보상
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
    }

    /**
     * 2. 프로필 테두리 보상 메일 생성
     */
    private Mail createProfileBorderMail(Member member, Tier finalTier) {
        // SQL이나 Enum에 정의된 한글 에셋 이름 (예: "골드 프로필 테두리")
        String assetName = Tier.getBorderAssetName(finalTier);
        Asset asset = assetRepository.findByName(assetName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 에셋입니다: " + assetName));

        String title = finalTier.name() + " 테두리 보상";
        String description = String.format("%s 티어 달성 기념으로\n[%s]를 드립니다.", finalTier.name(), assetName);

        return Mail.builder()
                .receiver(member)
                .senderName("Focus to Level Up")
                .type(MailType.PROFILE_BORDER)
                .title(title)
                .description(description) // JSON 대신 평문 사용
                .popupTitle("시즌 종료 특별 보상")
                .popupContent(finalTier.name() + " 티어 달성을 축하하며 특별한 테두리를 드립니다!")
                .reward(0) // 재화 보상 없음 (아이템 지급은 수령 시 처리)
                .assetName(assetName)
                .profileBorderImageUrl(asset.getAssetUrl())
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
    }

}
