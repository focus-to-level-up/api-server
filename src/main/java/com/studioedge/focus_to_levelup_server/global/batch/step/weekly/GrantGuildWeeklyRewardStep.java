package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildWeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [Weekly Job - Step 3] 길드 주간 보상 지급
 *
 * 동작 흐름:
 * 1. Reader: 모든 길드 정보를 페이지 단위(Chunk 20)로 조회합니다.
 * 2. Processor: 별도의 가공 없이 Guild 객체를 그대로 Writer로 전달합니다.
 * 3. Writer:
 * - 전달받은 길드 목록에 속한 모든 길드원을 한 번의 쿼리(IN절)로 조회합니다.
 * - 보상 계산:
 * a. 각 길드의 평균 공부 시간 및 부스트 사용 횟수를 집계하여 보상(다이아)을 계산합니다.
 * b. 길드원이 2명 미만이거나 보상이 0인 경우 제외합니다.
 * - 메일 생성 로직 (Best Reward):
 * a. 유저가 이미 DB에 보상 메일을 가지고 있다면, 더 높은 보상일 경우에만 업데이트합니다.
 * b. 현재 처리 중인 Chunk 내에서 중복된 유저가 있다면, 가장 높은 보상으로 덮어씁니다.
 * - 히스토리 처리: 기존 히스토리가 있으면 Update, 없으면 Insert 합니다.
 * - DB 저장: 계산된 메일, 히스토리, 길드 정보 업데이트를 일괄 저장(Bulk Save)합니다.
 * 4. Fault Tolerance:
 * - DB 데드락 등 일시적 장애 시 3회 재시도(Retry)합니다.
 * - 데이터 무결성 위반 등 처리 불가능한 에러 발생 시 해당 길드 처리를 건너뛰고(Skip) 로그를 남깁니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantGuildWeeklyRewardStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final GuildWeeklyRewardRepository guildWeeklyRewardRepository;
    private final MailRepository mailRepository;

    private static final int MAX_REWARD = 500;
    private static final int MIN_MEMBER_COUNT = 2;
    private static final int BOOST_BONUS = 50;

    private final Clock clock;

    @Bean
    public Step grantGuildWeeklyReward() {
        return new StepBuilder("grantGuildWeeklyReward", jobRepository)
                .<Guild, Guild> chunk(20, platformTransactionManager)
                .reader(grantGuildWeeklyRewardReader())
                .processor(grantGuildWeeklyRewardProcessor())
                .writer(grantGuildWeeklyRewardWriter())
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skip(NullPointerException.class)
                .skip(DataIntegrityViolationException.class)
                .skipLimit(100)
                .retry(DeadlockLoserDataAccessException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new GuildRewardSkipListener())
                .build();
    }

    @Bean
    public RepositoryItemReader<Guild> grantGuildWeeklyRewardReader() {
        return new RepositoryItemReaderBuilder<Guild>()
                .name("grantGuildWeeklyRewardReader")
                .pageSize(20)
                .methodName("findAll")
                .repository(guildRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Guild, Guild> grantGuildWeeklyRewardProcessor() {
        return guild -> guild;
    }

    @Bean
    @StepScope
    public ItemWriter<Guild> grantGuildWeeklyRewardWriter() {
        return chunk -> {
            List<Guild> guilds = (List<Guild>) chunk.getItems();
            List<Long> guildIds = guilds.stream().map(Guild::getId).toList();

            Map<Long, List<GuildMember>> membersByGuild = guildMemberRepository.findAllByGuildIdIn(guildIds)
                    .stream().collect(Collectors.groupingBy(gm -> gm.getGuild().getId()));

            List<Long> allMemberIds = membersByGuild.values().stream()
                    .flatMap(List::stream)
                    .map(gm -> gm.getMember().getId())
                    .distinct()
                    .toList();

            LocalDate oneWeekAgo = LocalDate.now(clock).minusDays(6);
            List<Mail> existingMails = mailRepository.findAllByReceiverIdInAndTypeAndCreatedAtAfter(
                    allMemberIds, MailType.GUILD_WEEKLY, oneWeekAgo.atStartOfDay()
            );
            Map<Long, Mail> dbMailMap = existingMails.stream()
                    .collect(Collectors.toMap(m -> m.getReceiver().getId(), m -> m));

            List<GuildWeeklyReward> existingHistories = guildWeeklyRewardRepository
                    .findAllByGuildIdInAndCreatedAtBetween(guildIds, LocalDateTime.now(clock).minusDays(7));

            Map<Long, GuildWeeklyReward> dbHistoryMap = existingHistories.stream()
                    .collect(Collectors.toMap(h -> h.getGuild().getId(), java.util.function.Function.identity()));

            Map<Long, Mail> memberBestRewardMap = new HashMap<>();
            List<GuildWeeklyReward> historyToSave = new ArrayList<>();
            List<Guild> guildsToUpdate = new ArrayList<>();

            for (Guild guild : guilds) {
                try {
                    List<GuildMember> members = membersByGuild.getOrDefault(guild.getId(), Collections.emptyList());
                    int memberCount = members.size();

                    if (memberCount < MIN_MEMBER_COUNT) continue;

                    long totalSeconds = members.stream().mapToLong(GuildMember::getWeeklyFocusTime).sum();
                    int avgSeconds = (int) (totalSeconds / memberCount);
                    double avgHours = avgSeconds / 3600.0;
                    int boostCount = (int) members.stream().filter(GuildMember::getIsBoosted).count();

                    int focusTimeReward = calculateBaseReward(avgHours);
                    int boostReward = boostCount * BOOST_BONUS;
                    int totalReward = Math.min(focusTimeReward + boostReward, MAX_REWARD);

                    if (totalReward == 0) continue;

                    for (GuildMember gm : members) {
                        Long memberId = gm.getMember().getId();
                        Mail newMailCandidate = createDiamondMail(gm.getMember(), guild.getName(), totalReward);

                        if (dbMailMap.containsKey(memberId)) {
                            Mail existingMail = dbMailMap.get(memberId);
                            if (newMailCandidate.getReward() > existingMail.getReward()) {
                                existingMail.updateRewardInfo(
                                        newMailCandidate.getTitle(),
                                        newMailCandidate.getDescription(),
                                        newMailCandidate.getPopupTitle(),
                                        newMailCandidate.getPopupContent(),
                                        newMailCandidate.getReward()
                                );
                            }
                            continue;
                        }

                        if (memberBestRewardMap.containsKey(memberId)) {
                            Mail existingMapMail = memberBestRewardMap.get(memberId);
                            if (newMailCandidate.getReward() > existingMapMail.getReward()) {
                                memberBestRewardMap.put(memberId, newMailCandidate);
                            }
                        } else {
                            memberBestRewardMap.put(memberId, newMailCandidate);
                        }
                    }

                    GuildWeeklyReward history;
                    if (dbHistoryMap.containsKey(guild.getId())) {
                        history = dbHistoryMap.get(guild.getId());
                        history.updateInfo(
                                avgSeconds,
                                boostCount * BOOST_BONUS,
                                focusTimeReward,
                                totalReward
                        );
                    } else {
                        history = GuildWeeklyReward.builder()
                                .guild(guild)
                                .avgFocusTime(avgSeconds)
                                .boostReward(boostCount * BOOST_BONUS)
                                .focusTimeReward(focusTimeReward)
                                .totalReward(totalReward)
                                .build();
                    }
                    historyToSave.add(history);

                    guild.updateWeeklyInfo(totalReward);
                    guildsToUpdate.add(guild);
                } catch (Exception e) {
                    log.error(">> Error processing guild ID {}: {}", guild.getId(), e.getMessage());
                    throw e;
                }
            }

            if (!memberBestRewardMap.isEmpty()) {
                mailRepository.saveAll(memberBestRewardMap.values());
            }
            if (!historyToSave.isEmpty()) {
                guildWeeklyRewardRepository.saveAll(historyToSave);
            }
            if (!guildsToUpdate.isEmpty()) {
                guildRepository.saveAll(guildsToUpdate);
            }
        };
    }

    //-------------------------------------------- PRIVATE METHOD --------------------------------------------

    private int calculateBaseReward(double avgHours) {
        if (avgHours >= 45) return 300;
        if (avgHours >= 40) return 250;
        if (avgHours >= 35) return 200;
        if (avgHours >= 30) return 150;
        if (avgHours >= 25) return 100;
        return 50;
    }

    private Mail createDiamondMail(Member member, String guildName, int diamondAmount) {
        return Mail.builder()
                .receiver(member)
                .senderName("Focus to Level Up")
                .type(MailType.GUILD_WEEKLY)
                .title("길드 주간 보상을 수령하세요")
                .description("길드 주간 보상 다이아 지급")
                .popupTitle("길드 주간 보상")
                .popupContent(guildName + "의 길드 주간 보상을 수령하세요")
                .reward(diamondAmount)
                .expiredAt(LocalDate.now(clock).plusDays(7))
                .build();
    }

    public static class GuildRewardSkipListener implements SkipListener<Guild, Guild> {
        @Override
        public void onSkipInRead(Throwable t) {
            log.error(">> [Skip] 읽기 중 에러: {}", t.getMessage());
        }

        @Override
        public void onSkipInWrite(Guild item, Throwable t) {
            log.error(">> [Skip] 쓰기 중 에러 (Guild ID: {}): {}", item.getId(), t.getMessage());
        }
    }
}
