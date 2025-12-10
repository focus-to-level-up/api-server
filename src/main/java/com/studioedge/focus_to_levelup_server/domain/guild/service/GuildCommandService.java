package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildWeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildCreateRequest;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildUpdateRequest;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.*;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

/**
 * 길드 생성/수정/삭제 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GuildCommandService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final GuildWeeklyRewardRepository guildWeeklyRewardRepository;
    private final MemberRepository memberRepository;
    private final GuildQueryService guildQueryService;
    private final FcmService fcmService;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String FOCUS_REQUEST_KEY_PREFIX = "focus-request:";
    private static final Duration FOCUS_REQUEST_COOLDOWN = Duration.ofHours(1);

    /**
     * 길드 생성
     * - 생성자를 LEADER로 자동 등록
     */
    public GuildResponse createGuild(GuildCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 비공개 길드인 경우 비밀번호 암호화
        String encodedPassword = null;
        if (!request.isPublic() && request.password() != null) {
            encodedPassword = passwordEncoder.encode(request.password());
        }

        // Guild 엔티티 생성
        Guild guild = Guild.builder()
                .name(request.name())
                .description(request.description())
                .targetFocusTime(request.targetFocusTime())
                .category(request.category())
                .isPublic(request.isPublic())
                .password(encodedPassword)
                .maxMembers(20)
                .currentMembers(1)
                .averageFocusTime(0)
                .build();

        Guild savedGuild = guildRepository.save(guild);

        // GuildMember 생성 (LEADER)
        GuildMember guildMember = GuildMember.builder()
                .guild(savedGuild)
                .member(member)
                .role(GuildRole.LEADER)
                .weeklyFocusTime(0)
                .isBoosted(false)
                .build();

        guildMemberRepository.save(guildMember);

        return GuildResponse.of(savedGuild, Optional.of(guildMember));
    }

    /**
     * 길드 정보 수정 (LEADER만 가능)
     * - 비밀번호 변경은 별도 API 사용 (changePassword)
     * - 비공개 전환 시에만 password 필드 사용
     */
    public GuildResponse updateGuild(Long guildId, GuildUpdateRequest request, Long memberId) {
        Guild guild = guildQueryService.findGuildById(guildId);

        // 공개 여부 변경 시 처리
        if (request.isPublic() != null) {
            // 공개 → 비공개 전환 시 비밀번호 설정
            if (!request.isPublic() && guild.getIsPublic()) {
                if (request.password() == null || request.password().isBlank()) {
                    throw new IllegalArgumentException("비공개 전환 시 비밀번호는 필수입니다.");
                }
                guild.updatePassword(passwordEncoder.encode(request.password()));
            }
            // 비공개 → 공개 전환 시 비밀번호 제거
            if (request.isPublic() && !guild.getIsPublic()) {
                guild.updatePassword(null);
            }
            guild.updateIsPublic(request.isPublic());
        }

        // 기타 필드 업데이트
        if (request.name() != null && !request.name().isBlank()) {
            guild.updateName(request.name());
        }
        if (request.description() != null && !request.description().isBlank()) {
            guild.updateDescription(request.description());
        }
        if (request.targetFocusTime() != null) {
            guild.updateTargetFocusTime(request.targetFocusTime());
        }

        Optional<GuildMember> guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId);
        GuildWeeklyReward guildWeeklyReward = guildWeeklyRewardRepository
                .findFirstByGuildIdOrderByCreatedAtDesc(guildId)
                .orElse(null);
        return GuildResponse.of(guild, guildMember, guildWeeklyReward);
    }

    /**
     * 길드 비밀번호 변경 (LEADER만 가능)
     * - 비공개 길드만 비밀번호 변경 가능
     * - 현재 비밀번호 검증 필수
     */
    public void changePassword(Long guildId, String currentPassword, String newPassword) {
        Guild guild = guildQueryService.findGuildById(guildId);

        // 공개 길드는 비밀번호 변경 불가
        if (guild.getIsPublic()) {
            throw new IllegalArgumentException("공개 길드는 비밀번호를 설정할 수 없습니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, guild.getPassword())) {
            throw new InvalidGuildPasswordException();
        }

        // 새 비밀번호 설정
        guild.updatePassword(passwordEncoder.encode(newPassword));
        log.info("Guild {} password changed", guildId);
    }

    /**
     * 길드 가입
     * - 비공개 길드는 비밀번호 검증
     * - 사용자당 최대 10개 길드 가입 제한
     */
    public GuildResponse joinGuild(Long guildId, String password, Long memberId) {
        Guild guild = guildQueryService.findGuildById(guildId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 정원 체크
        if (guild.isFull()) {
            throw new GuildFullException();
        }

        // 중복 가입 체크
        if (guildMemberRepository.existsByGuildIdAndMemberId(guildId, memberId)) {
            throw new AlreadyJoinedGuildException();
        }

        // 사용자당 최대 10개 길드 가입 제한 체크
        long memberGuildCount = guildMemberRepository.countByMemberId(memberId);
        if (memberGuildCount >= 10) {
            throw new MaxGuildMembershipExceededException();
        }

        // 비공개 길드 비밀번호 검증
        if (!guild.getIsPublic()) {
            if (password == null || !passwordEncoder.matches(password, guild.getPassword())) {
                throw new InvalidGuildPasswordException();
            }
        }

        // GuildMember 생성
        GuildMember guildMember = GuildMember.builder()
                .guild(guild)
                .member(member)
                .role(GuildRole.MEMBER)
                .weeklyFocusTime(0)
                .isBoosted(false)
                .build();

        guildMemberRepository.save(guildMember);

        // 길드 인원 증가
        guild.incrementMemberCount();
        GuildWeeklyReward guildWeeklyReward = guildWeeklyRewardRepository
                .findFirstByGuildIdOrderByCreatedAtDesc(guildId)
                .orElse(null);

        return GuildResponse.of(guild, Optional.of(guildMember), guildWeeklyReward);
    }

    /**
     * 길드 탈퇴
     * - 길드장은 먼저 권한 위임을 해야 탈퇴 가능
     */
    public void leaveGuild(Long guildId, Long memberId) {
        GuildMember guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId)
                .orElseThrow(NotGuildMemberException::new);

        Guild guild = guildQueryService.findGuildById(guildId);

        // 길드장은 혼자일 때만 탈퇴 가능 (다른 멤버가 있으면 먼저 위임 필요)
        if (guildMember.getRole() == GuildRole.LEADER && guild.getCurrentMembers() > 1) {
            throw new LeaderCannotLeaveException();
        }

        // GuildMember 삭제
        guildMemberRepository.delete(guildMember);

        // 길드 인원 감소
        guild.decrementMemberCount();

        // 길드원이 0명이 되면 길드 삭제
        if (guild.getCurrentMembers() == 0) {
            guildRepository.delete(guild);
        }
    }

    /**
     * 리더 위임 후 탈퇴 (하나의 트랜잭션으로 처리)
     * - 리더가 다른 멤버에게 리더 권한을 위임하고 동시에 탈퇴
     * - 원자적 처리로 중간 실패 시 롤백
     */
    public void transferLeaderAndLeave(Long guildId, Long newLeaderMemberId, Long currentLeaderMemberId) {
        Guild guild = guildQueryService.findGuildById(guildId);

        // 현재 리더 검증
        GuildMember currentLeader = guildMemberRepository.findByGuildIdAndMemberId(guildId, currentLeaderMemberId)
                .orElseThrow(NotGuildMemberException::new);

        if (currentLeader.getRole() != GuildRole.LEADER) {
            throw new IllegalArgumentException("리더만 권한을 위임할 수 있습니다.");
        }

        // 새 리더 검증
        GuildMember newLeader = guildMemberRepository.findByGuildIdAndMemberId(guildId, newLeaderMemberId)
                .orElseThrow(NotGuildMemberException::new);

        if (currentLeaderMemberId.equals(newLeaderMemberId)) {
            throw new IllegalArgumentException("자기 자신에게 위임할 수 없습니다.");
        }

        // 1. 새 리더에게 LEADER 역할 부여
        newLeader.updateRole(GuildRole.LEADER);

        // 2. 현재 리더 삭제 (탈퇴)
        guildMemberRepository.delete(currentLeader);

        // 3. 길드 인원 감소
        guild.decrementMemberCount();

        log.info("Guild {} leader transferred from {} to {} and left", guildId, currentLeaderMemberId, newLeaderMemberId);
    }

    /**
     * 길드 삭제 (LEADER만 가능, 모든 길드원 탈퇴 후)
     */
    public void deleteGuild(Long guildId, Long memberId) {
        Guild guild = guildQueryService.findGuildById(guildId);

        // 길드원이 본인만 남았는지 확인
        if (guild.getCurrentMembers() > 1) {
            throw new CannotDeleteGuildWithMembersException();
        }

        // 본인의 GuildMember 삭제
        GuildMember guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("길드원이 아닙니다."));

        guildMemberRepository.delete(guildMember);

        // Guild 삭제
        guildRepository.delete(guild);
    }

    /**
     * 길드원 집중 요청 (FCM 푸시 알림)
     * 특정 길드원에게 알림 전송
     * - 같은 대상자에게 1시간 내 재요청 불가
     */
    public void sendFocusRequest(Long guildId, Long requesterId, Long targetMemberId) {
        // 쿨다운 체크
        String cooldownKey = buildFocusRequestKey(guildId, requesterId, targetMemberId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new FocusRequestCooldownException();
        }

        // 요청자 검증
        GuildMember requester = guildMemberRepository.findByGuildIdAndMemberId(guildId, requesterId)
                .orElseThrow(NotGuildMemberException::new);

        // 대상자 검증 (같은 길드에 속해있는지)
        GuildMember target = guildMemberRepository.findByGuildIdAndMemberId(guildId, targetMemberId)
                .orElseThrow(NotGuildMemberException::new);

        Member requesterMember = requester.getMember();
        Member targetMember = target.getMember();

        // 대상자의 FCM 토큰 확인
        if (targetMember.getFcmToken() == null) {
            log.warn(">> Target member {} has no FCM token", targetMemberId);
            return; // 토큰이 없으면 알림 전송 불가
        }

        // 대상자에게 알림 전송
        try {
            fcmService.sendToOne(
                    targetMember.getFcmToken(),
                    "집중요청알림",
                    requesterMember.getNickname() + "님이 집중을 요청했어요!"
            );

            // 성공 시 쿨다운 설정
            redisTemplate.opsForValue().set(cooldownKey, "1", FOCUS_REQUEST_COOLDOWN);

            log.info(">> Focus request sent from member {} to member {} in guild {}", requesterId, targetMemberId, guildId);
        } catch (Exception e) {
            log.error(">> Failed to send focus request FCM from {} to {}", requesterId, targetMemberId, e);
        }
    }

    private String buildFocusRequestKey(Long guildId, Long requesterId, Long targetMemberId) {
        return FOCUS_REQUEST_KEY_PREFIX + guildId + ":" + requesterId + ":" + targetMemberId;
    }
}
