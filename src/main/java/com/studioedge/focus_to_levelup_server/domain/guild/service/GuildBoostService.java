package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildBoostRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildListResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildBoost;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.AlreadyBoostedException;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.MaxBoostLimitExceededException;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.NotGuildMemberException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.PremiumSubscriptionRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 길드 부스트 서비스
 * - 프리미엄 구독자만 사용 가능
 * - 유저당 최대 2개 길드 부스트 가능
 * - 길드당 최대 10명 부스트 가능
 * - 부스트 효과: 1명당 주간 보상 +50 다이아 (길드 전체에 적용)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GuildBoostService {

    private final GuildBoostRepository guildBoostRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final GuildQueryService guildQueryService;

    private static final int MAX_BOOST_PER_MEMBER = 2;
    private static final int MAX_BOOST_PER_GUILD = 10;
    public static final int BOOST_DIAMOND_BONUS = 50; // 부스트 1명당 다이아 보너스

    /**
     * 길드 부스트 활성화
     * 1. 프리미엄 구독권 확인
     * 2. 유저 부스트 개수 확인 (< 2)
     * 3. 길드 부스트 개수 확인 (< 10)
     * 4. GuildBoost 생성
     * 5. GuildMember.isBoosted = true
     * 6. Subscription.activatedGuildId 업데이트
     */
    public void activateBoost(Long guildId, Long memberId) {
        // 1. 프리미엄 구독권 확인
        Subscription subscription = subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .filter(sub -> sub.getType() == SubscriptionType.PREMIUM)
                .orElseThrow(PremiumSubscriptionRequiredException::new);

        // 2. 이미 해당 길드에 부스트 중인지 확인
        if (guildBoostRepository.findByGuildIdAndMemberIdAndIsActiveTrue(guildId, memberId).isPresent()) {
            throw new AlreadyBoostedException();
        }

        // 3. 유저 부스트 개수 확인
        Long memberBoostCount = guildBoostRepository.countByMemberIdAndIsActiveTrue(memberId);
        if (memberBoostCount >= MAX_BOOST_PER_MEMBER) {
            throw new MaxBoostLimitExceededException();
        }

        // 4. 길드 부스트 개수 확인
        Long guildBoostCount = guildBoostRepository.countByGuildIdAndIsActiveTrue(guildId);
        if (guildBoostCount >= MAX_BOOST_PER_GUILD) {
            throw new MaxBoostLimitExceededException();
        }

        // 5. 길드원 확인
        Guild guild = guildQueryService.findGuildById(guildId);
        GuildMember guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId)
                .orElseThrow(NotGuildMemberException::new);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 6. GuildBoost 생성
        GuildBoost guildBoost = GuildBoost.builder()
                .guild(guild)
                .member(member)
                .startDate(LocalDate.now())
                .endDate(subscription.getEndDate())
                .isActive(true)
                .build();

        guildBoostRepository.save(guildBoost);

        // 7. GuildMember.isBoosted = true
        guildMember.activateBoost();

        // 8. Subscription.activatedGuildId 업데이트
        subscription.updateActivatedGuildId(guildId);
    }

    /**
     * 길드 부스트 비활성화
     * - GuildBoost.isActive = false
     * - GuildMember.isBoosted = false
     * - Subscription.activatedGuildId = null
     */
    public void deactivateBoost(Long guildId, Long memberId) {
        // 1. GuildBoost 조회
        GuildBoost guildBoost = guildBoostRepository.findByGuildIdAndMemberIdAndIsActiveTrue(guildId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 부스트를 찾을 수 없습니다."));

        // 2. GuildBoost 비활성화
        guildBoost.deactivate();

        // 3. GuildMember.isBoosted = false
        GuildMember guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId)
                .orElseThrow(NotGuildMemberException::new);
        guildMember.deactivateBoost();

        // 4. Subscription.activatedGuildId = null
        subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .filter(sub -> sub.getActivatedGuildId() != null && sub.getActivatedGuildId().equals(guildId))
                .ifPresent(sub -> sub.updateActivatedGuildId(null));
    }

    /**
     * 내가 부스트한 길드 목록 조회
     */
    @Transactional(readOnly = true)
    public GuildListResponse getMyBoostedGuilds(Long memberId) {
        List<GuildBoost> guildBoosts = guildBoostRepository.findAllByMemberIdAndIsActiveTrueWithGuild(memberId);

        List<GuildListResponse.GuildSummary> guilds = guildBoosts.stream()
                .map(gb -> GuildListResponse.GuildSummary.from(gb.getGuild()))
                .toList();

        return new GuildListResponse(guilds, 1, (long) guilds.size(), 0);
    }

    /**
     * 길드의 부스트 개수 조회 (주간 보상 계산용)
     */
    @Transactional(readOnly = true)
    public Long getGuildBoostCount(Long guildId) {
        return guildBoostRepository.countByGuildIdAndIsActiveTrue(guildId);
    }

    /**
     * 부스트 보너스 다이아 계산
     * 부스트 1명당 +50 다이아
     */
    public int calculateBoostBonus(Long guildId) {
        Long boostCount = getGuildBoostCount(guildId);
        return boostCount.intValue() * BOOST_DIAMOND_BONUS;
    }

    /**
     * 주간 평균 집중 시간에 따른 기본 보상 계산
     * 25시간 미만: 50
     * 25 ~ 30시간: 100
     * 30 ~ 35시간: 150
     * 35 ~ 40시간: 200
     * 40 ~ 45시간: 250
     * 45시간 이상: 300
     */
    public int calculateBaseReward(int averageFocusTimeSeconds) {
        int averageFocusTimeHours = averageFocusTimeSeconds / 3600; // 초 → 시간 변환

        if (averageFocusTimeHours < 25) {
            return 50;
        } else if (averageFocusTimeHours < 30) {
            return 100;
        } else if (averageFocusTimeHours < 35) {
            return 150;
        } else if (averageFocusTimeHours < 40) {
            return 200;
        } else if (averageFocusTimeHours < 45) {
            return 250;
        } else {
            return 300;
        }
    }

    /**
     * 총 주간 보상 계산 (기본 보상 + 부스트 보너스)
     */
    public int calculateTotalReward(Long guildId, int averageFocusTimeSeconds) {
        int baseReward = calculateBaseReward(averageFocusTimeSeconds);
        int boostBonus = calculateBoostBonus(guildId);
        return baseReward + boostBonus;
    }

    /**
     * 만료된 부스트 비활성화 (배치 작업용)
     */
    public void deactivateExpiredBoosts() {
        List<GuildBoost> expiredBoosts = guildBoostRepository.findAllExpiredBoosts(LocalDate.now());

        for (GuildBoost guildBoost : expiredBoosts) {
            guildBoost.deactivate();

            // GuildMember.isBoosted = false
            guildMemberRepository.findByGuildIdAndMemberId(
                    guildBoost.getGuild().getId(),
                    guildBoost.getMember().getId()
            ).ifPresent(GuildMember::deactivateBoost);
        }
    }
}
