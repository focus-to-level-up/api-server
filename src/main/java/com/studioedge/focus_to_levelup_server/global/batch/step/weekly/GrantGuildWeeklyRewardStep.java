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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    @Bean
    public Step grantGuildWeeklyReward() {
        return new StepBuilder("grantGuildWeeklyReward", jobRepository)
                .<Guild, Guild> chunk(20, platformTransactionManager)
                .reader(grantGuildWeeklyRewardReader())
                .processor(grantGuildWeeklyRewardProcessor())
                .writer(grantGuildWeeklyRewardWriter())
                .faultTolerant()
                // 1. 특정 예외 발생 시 배치를 멈추지 않고 건너뜀
                .skip(IllegalArgumentException.class) // 로직 에러
                .skip(NullPointerException.class)     // 데이터 누락 에러
                .skip(DataIntegrityViolationException.class) // DB 제약조건 에러
                // 2. 최대 몇 개까지 건너뛸지 제한
                .skipLimit(20)
                // 3. 재시도 로직 (DB 연결 실패 등 일시적 장애용)
                .retry(DeadlockLoserDataAccessException.class)
                .retryLimit(3)
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

            // 1. 모든 길드의 멤버들을 일괄 조회후, 길드-길드원 리스트 매핑.
            Map<Long, List<GuildMember>> membersByGuild = guildMemberRepository.findAllByGuildIdIn(guildIds)
                    .stream().collect(Collectors.groupingBy(gm -> gm.getGuild().getId()));

            // 현재 조죄한 맴버 ID 추출
            List<Long> allMemberIds = membersByGuild.values().stream()
                    .flatMap(List::stream)
                    .map(gm -> gm.getMember().getId())
                    .distinct() // 중복 제거
                    .toList();

            LocalDate oneWeekAgo = LocalDate.now().minusDays(6);
            List<Mail> existingMails = mailRepository.findAllByReceiverIdInAndTypeAndCreatedAtAfter(
                    allMemberIds, MailType.GUILD_WEEKLY, oneWeekAgo.atStartOfDay()
            );

            Map<Long, Mail> dbMailMap = existingMails.stream()
                    .collect(Collectors.toMap(m -> m.getReceiver().getId(), m -> m));
            Map<Long, Mail> memberBestRewardMap = new HashMap<>();
            List<GuildWeeklyReward> historyToSave = new ArrayList<>();
            List<Guild> guildsToUpdate = new ArrayList<>();

            for (Guild guild : guilds) {
                // 2. 각 길드의 맴버들 조회
                List<GuildMember> members = membersByGuild.getOrDefault(guild.getId(), Collections.emptyList());
                int memberCount = members.size();

                // 2명 미만인 길드는 보상 제외
                if (memberCount < MIN_MEMBER_COUNT) {
                    continue;
                }

                // 3. 통계 계산
                long totalSeconds = members.stream().mapToLong(GuildMember::getWeeklyFocusTime).sum();
                // 평균 집중 시간 (초) = 총합 / 인원수
                int avgSeconds = (int) (totalSeconds / memberCount);
                double avgHours = avgSeconds / 3600.0;
                // 부스트 사용한 유저의 수
                int boostCount = (int) members.stream().filter(GuildMember::getIsBoosted).count();

                // 4. 보상 다이아 계산
                int focusTimeReward = calculateBaseReward(avgHours);
                int boostReward = boostCount * BOOST_BONUS;
                int totalReward = Math.min(focusTimeReward + boostReward, MAX_REWARD);

                if (totalReward == 0) {
                    continue;
                }

                // 4. Mail 객체 생성 (모든 멤버에게 발송)
                for (GuildMember gm : members) {
                    Long memberId = gm.getMember().getId();
                    Mail newMailCandidate = createDiamondMail(gm.getMember(), guild.getName(), totalReward);

                    // Case A: DB에 이미 메일이 있는 경우 (다른 Chunk에서 이미 처리됨)
                    if (dbMailMap.containsKey(memberId)) {
                        Mail existingMail = dbMailMap.get(memberId);
                        // 새로운 보상이 기존 보상보다 클 때만 업데이트 (덮어쓰기)
                        if (newMailCandidate.getReward() > existingMail.getReward()) {
                            // [중요] 엔티티 값만 변경하면 Dirty Checking으로 자동 UPDATE 됨
                            existingMail.updateRewardInfo(
                                    newMailCandidate.getTitle(),
                                    newMailCandidate.getDescription(),
                                    newMailCandidate.getPopupTitle(),
                                    newMailCandidate.getPopupContent(),
                                    newMailCandidate.getReward()
                            );
                        }
                        // DB에 있는 메일이 더 좋거나 같으면 아무것도 안함 (유지)
                        continue;
                    }

                    // Case B: DB에는 없지만, 현재 Chunk 내에서 이미 처리된 경우 (같은 Chunk 내 다중 길드)
                    if (memberBestRewardMap.containsKey(memberId)) {
                        Mail existingMapMail = memberBestRewardMap.get(memberId);
                        if (newMailCandidate.getReward() > existingMapMail.getReward()) {
                            memberBestRewardMap.put(memberId, newMailCandidate); // 더 좋은 걸로 교체
                        }
                    } else {
                        // Case C: 처음 처리하는 경우
                        memberBestRewardMap.put(memberId, newMailCandidate);
                    }
                }

                // 5. 히스토리 및 길드 정보 갱신
                GuildWeeklyReward history = GuildWeeklyReward.builder()
                        .guild(guild)
                        .avgFocusTime(avgSeconds)
                        .boostReward(boostCount * BOOST_BONUS)
                        .focusTimeReward(focusTimeReward)
                        .totalReward(totalReward)
                        .build();
                historyToSave.add(history);

                guild.updateWeeklyInfo(totalReward);
                guildsToUpdate.add(guild);
            }

            // 6. 일괄 저장
            if (!memberBestRewardMap.isEmpty()) {
                mailRepository.saveAll(memberBestRewardMap.values());
            }
            if (!historyToSave.isEmpty()) {
                guildWeeklyRewardRepository.saveAll(historyToSave);
            }
            if (!guildsToUpdate.isEmpty()) {
                guildRepository.saveAll(guildsToUpdate);
            }

            log.info(">> Granted rewards for {} guilds. Mails: {}, Histories: {}",
                    historyToSave.size(), memberBestRewardMap.size(), historyToSave.size());
        };
    }

    /**
     * 평균 공부 시간에 따른 다이아 갯수 보상
     */
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
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
    }
}
