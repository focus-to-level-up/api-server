package com.studioedge.fcm;

import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.repository.MemberCharacterRepository;
import com.studioedge.infra.client.fcm.FcmClient;
import com.studioedge.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!local")
public class FcmService {

    private final FcmClient fcmClient;
    private final MemberCharacterRepository memberCharacterRepository;

    public void sendWeeklyRewardNotification(List<Member> members) {
        for (Member member : members) {
            if (member.getFcmToken() == null) {
                continue;
            }

            try {
                fcmClient.sendToOne(
                        member.getFcmToken(),
                        "주간 보상 수령",
                        "지난주의 다이아 보상을 획득하세요!"
                );
                log.info(">> Sent weekly reward notification to member: {}", member.getId());
            } catch (Exception e) {
                log.error(">> Failed to send weekly reward FCM to member: {}", member.getId(), e);
            }
        }
    }

    public void sendInactiveUserNotification(List<Member> members, int hours) {
        for (Member member : members) {
            if (member.getFcmToken() == null || member.getNickname() == null) {
                continue;
            }

            try {
                Optional<MemberCharacter> defaultCharacter = memberCharacterRepository
                        .findByMemberIdAndIsDefaultTrue(member.getId());

                String characterName = defaultCharacter
                        .map(memberCharacter -> memberCharacter.getCharacter().getName())
                        .orElse("캐릭터");

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

                fcmClient.sendToOne(member.getFcmToken(), title, body);
                log.info(">> Sent inactive user notification to member: {} ({} hours)", member.getId(), hours);
            } catch (Exception e) {
                log.error(">> Failed to send inactive user FCM to member: {}", member.getId(), e);
            }
        }
    }
}
