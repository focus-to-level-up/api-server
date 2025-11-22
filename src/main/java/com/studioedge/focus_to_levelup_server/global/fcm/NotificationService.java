package com.studioedge.focus_to_levelup_server.global.fcm;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 발송 비즈니스 로직 서비스
 * - 4가지 유형의 알림 발송 메서드 제공
 * - FcmService를 사용하여 실제 FCM 메시지 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final FcmService fcmService;
    private final MemberRepository memberRepository;

    /**
     * 주간 보상 알림 발송 (배치 작업에서 호출)
     * - 메시지: "지난주의 다이아 보상을 획득하세요!"
     * - 발송 조건: 지난주에 길드 활동이 있었던 유저
     *
     * @param memberId 유저 ID
     * @return 발송 성공 여부
     */
    public boolean sendWeeklyRewardNotification(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null || member.getFcmToken() == null) {
            log.warn("Member not found or FCM token is null. MemberId: {}", memberId);
            return false;
        }

        // 알림 설정 확인 (일반 알림 OFF면 발송 안함)
        if (member.getMemberSetting() != null && !member.getMemberSetting().isGeneralNotificationEnabled()) {
            log.info("General notification disabled for member: {}", memberId);
            return false;
        }

        return fcmService.sendNotification(
                member.getFcmToken(),
                NotificationType.WEEKLY_REWARD,
                null
        );
    }

    /**
     * 1일 미접속 알림 발송 (배치 작업에서 호출)
     * - 메시지: "{닉네임}님이 안들어와서 화가 났어요"
     * - 발송 조건: 마지막 로그인으로부터 1일 지남
     *
     * @param memberId 유저 ID
     * @return 발송 성공 여부
     */
    public boolean sendInactive1DayNotification(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null || member.getFcmToken() == null) {
            log.warn("Member not found or FCM token is null. MemberId: {}", memberId);
            return false;
        }

        // 알림 설정 확인
        if (member.getMemberSetting() != null && !member.getMemberSetting().isGeneralNotificationEnabled()) {
            log.info("General notification disabled for member: {}", memberId);
            return false;
        }

        return fcmService.sendNotification(
                member.getFcmToken(),
                NotificationType.INACTIVE_1DAY,
                null,
                member.getNickname() // 메시지 포맷팅 인자
        );
    }

    /**
     * 3일 미접속 알림 발송 (배치 작업에서 호출)
     * - 메시지: "{닉네임}님을 기다리다가 누군지 까먹을 것 같다고 하네요"
     * - 발송 조건: 마지막 로그인으로부터 3일 지남
     *
     * @param memberId 유저 ID
     * @return 발송 성공 여부
     */
    public boolean sendInactive3DayNotification(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null || member.getFcmToken() == null) {
            log.warn("Member not found or FCM token is null. MemberId: {}", memberId);
            return false;
        }

        // 알림 설정 확인
        if (member.getMemberSetting() != null && !member.getMemberSetting().isGeneralNotificationEnabled()) {
            log.info("General notification disabled for member: {}", memberId);
            return false;
        }

        return fcmService.sendNotification(
                member.getFcmToken(),
                NotificationType.INACTIVE_3DAY,
                null,
                member.getNickname()
        );
    }

    /**
     * 길드 집중 요청 알림 발송 (즉시 발송)
     * - 메시지: "{길드원 닉네임}이 집중을 요청했어요!"
     * - 발송 조건: 요청자와 같은 길드에 속한 모든 멤버
     *
     * @param guildId 길드 ID
     * @param requesterId 요청자 ID
     * @param recipientFcmTokens 수신자 FCM 토큰 리스트
     * @param requesterNickname 요청자 닉네임
     * @return 발송 성공한 토큰 개수
     */
    public int sendGuildFocusRequestNotification(
            Long guildId,
            Long requesterId,
            List<String> recipientFcmTokens,
            String requesterNickname
    ) {
        if (recipientFcmTokens == null || recipientFcmTokens.isEmpty()) {
            log.warn("Recipient FCM token list is empty. GuildId: {}", guildId);
            return 0;
        }

        Map<String, String> data = new HashMap<>();
        data.put("targetId", String.valueOf(guildId));
        data.put("requesterId", String.valueOf(requesterId));
        data.put("requesterNickname", requesterNickname);

        return fcmService.sendMulticastNotification(
                recipientFcmTokens,
                NotificationType.GUILD_FOCUS_REQUEST,
                data,
                requesterNickname
        );
    }
}