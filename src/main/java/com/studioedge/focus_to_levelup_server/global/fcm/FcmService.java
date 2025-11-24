package com.studioedge.focus_to_levelup_server.global.fcm;

import com.google.firebase.messaging.*;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.fcm.exception.EmptyFcmTokenListException;
import com.studioedge.focus_to_levelup_server.global.fcm.exception.FcmSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final MemberCharacterRepository memberCharacterRepository;

    /**
     * 단건 전송
     */
    public String sendToOne(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패: token={}, title={}, error={}", token, title, e.getMessage());
            throw new FcmSendException();
        }
    }

    /**
     * 다건 전송 (Multicast)
     */
    public void sendToMany(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) {
            throw new EmptyFcmTokenListException();
        }

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info(">> FCM Multicast sent. Success: {}, Failure: {}",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM Multicast 전송 실패: title={}, error={}", title, e.getMessage());
            throw new FcmSendException();
        }
    }

    /**
     * 주간 보상 알림 (월요일)
     */
    public void sendWeeklyRewardNotification(List<Member> members) {
        for (Member member : members) {
            if (member.getFcmToken() == null) continue;

            try {
                sendToOne(
                        member.getFcmToken(),
                        "주간 보상 수령",
                        "지난주의 다이아 보상을 획득하세요!"
                );
                log.info(">> Sent weekly reward notification to member: {}", member.getId());
            } catch (Exception e) {
                log.error(">> Failed to send FCM to member: {}", member.getId(), e);
            }
        }
    }

    /**
     * 미접속 알림 (24시간/72시간)
     */
    public void sendInactiveUserNotification(List<Member> members, int hours) {
        for (Member member : members) {
            if (member.getFcmToken() == null || member.getNickname() == null) continue;

            try {
                // 유저의 대표 캐릭터 조회
                Optional<MemberCharacter> defaultCharacter = memberCharacterRepository
                        .findByMemberIdAndIsDefaultTrue(member.getId());

                String characterName = defaultCharacter
                        .map(mc -> mc.getCharacter().getName())
                        .orElse("캐릭터"); // 대표 캐릭터가 없는 경우 기본값

                String title;
                String body;

                if (hours == 24) {
                    title = characterName;
                    body = member.getNickname() + "님이 안 들어와서 화가 났어요";
                } else if (hours == 72) {
                    title = characterName;
                    body = member.getNickname() + "님을 기다리다가 누군지 까먹을 것 같다고 하네요";
                } else {
                    continue;
                }

                sendToOne(member.getFcmToken(), title, body);
                log.info(">> Sent inactive user notification to member: {} ({}시간, character: {})",
                        member.getId(), hours, characterName);
            } catch (Exception e) {
                log.error(">> Failed to send FCM to member: {}", member.getId(), e);
            }
        }
    }
}
