package com.studioedge.focus_to_levelup_server.batch.season_end;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
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
import java.util.*;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantSeasonRewardStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MailRepository mailRepository;
    private final LeagueRepository leagueRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public Step grantSeasonReward() {
        return new StepBuilder("grantSeasonRewardStep", jobRepository)
                .<League, List<Mail>>chunk(20, platformTransactionManager)
                .reader(grantSeasonRewardReader())
                .processor(grantSeasonRewardProcessor()) // @StepScope 주입
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

                // 최종 티어에 맞는 메일 생성
                mails.add(createSeasonEndMail(member, finalTier));
                mails.add(createProfileBorderMail(member, finalTier));
                if (finalTier.equals(Tier.MASTER)) {
                    mails.add(createMasterSubscriptionMail(member));
                }
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

        // 마스터인 경우 구독권 텍스트 추가
        boolean isMaster = (finalTier == Tier.MASTER);

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
                .description("시즌 종료 보상입니다.\n" + (isMaster ? "(구독권 포함)" : ""))
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
        try {
            // SQL에 정의된 한글 에셋 이름 매핑
            String assetName = Tier.getBorderAssetName(finalTier);

            String description = objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("rewardType", "TIER_BORDER");
                put("tier", finalTier.name());
                put("assetName", assetName); // 예: "골드 프로필 테두리"
            }});

            return Mail.builder()
                    .receiver(member)
                    .senderName("Focus to Level Up")
                    .type(MailType.PROFILE_BORDER) // 혹은 ITEM_REWARD
                    .title(finalTier.name() + " 테두리 보상")
                    .description(description)
                    .popupTitle("시즌 종료 특별 보상")
                    .popupContent(finalTier.name() + " 티어 달성을 축하하며 특별한 테두리를 드립니다!")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(7))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to create border mail JSON for member {}", member.getId(), e);
            return null;
        }
    }

    /**
     * 3. 마스터 티어 구독권 보상 메일 생성
     */
    private Mail createMasterSubscriptionMail(Member member) {
        try {
            // 구독권 정보 JSON 생성
            // 마스터 보상은 'PREMIUM' 등급 30일 지급으로 설정
            String description = objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("subscriptionType", SubscriptionType.PREMIUM.name());
                put("durationDays", 30);
            }});

            return Mail.builder()
                    .receiver(member)
                    .senderName("Focus to Level Up")
                    // 클라이언트가 구독권 로직을 처리하도록 GIFT_SUBSCRIPTION 타입 사용 권장
                    .type(MailType.GIFT_SUBSCRIPTION)
                    .title("마스터 달성 보상")
                    .description(description) // JSON 데이터 저장
                    .popupTitle("🎁 마스터 티어 특별 보상")
                    .popupContent("상위 10% 달성을 축하합니다! 프리미엄 구독권 30일을 드립니다.")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(7))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to create subscription mail JSON for member {}", member.getId(), e);
            return null;
        }
    }
}
