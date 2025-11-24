package com.studioedge.focus_to_levelup_server.test;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.fcm.FcmService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Test - FCM", description = "FCM 테스트 API (로컬/개발 환경 전용)")
@RestController
@RequestMapping("/test/fcm")
@RequiredArgsConstructor
@Profile({"local", "dev"}) // 로컬/개발 환경에서만 활성화
public class FcmTestController {

    private final FcmService fcmService;
    private final MemberRepository memberRepository;

    @PostMapping("/weekly-reward")
    @Operation(
            summary = "주간 보상 알림 테스트",
            description = "주간 보상 미수령 유저에게 FCM 알림을 발송합니다."
    )
    public ResponseEntity<CommonResponse<TestResult>> testWeeklyReward() {
        List<Member> members = memberRepository.findAllByIsReceivedWeeklyRewardIsFalseAndFcmTokenIsNotNull();
        fcmService.sendWeeklyRewardNotification(members);
        return HttpResponseUtil.ok(new TestResult("Sent to " + members.size() + " members", members.size()));
    }

    @PostMapping("/inactive-24h")
    @Operation(
            summary = "24시간 미접속 알림 테스트",
            description = "24~48시간 전에 마지막 접속한 유저에게 FCM 알림을 발송합니다."
    )
    public ResponseEntity<CommonResponse<TestResult>> testInactive24h() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(48);
        LocalDateTime end = now.minusHours(24);
        List<Member> members = memberRepository.findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(start, end);
        fcmService.sendInactiveUserNotification(members, 24);
        return HttpResponseUtil.ok(new TestResult("Sent to " + members.size() + " members (24h inactive)", members.size()));
    }

    @PostMapping("/inactive-72h")
    @Operation(
            summary = "72시간 미접속 알림 테스트",
            description = "72~96시간 전에 마지막 접속한 유저에게 FCM 알림을 발송합니다."
    )
    public ResponseEntity<CommonResponse<TestResult>> testInactive72h() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(96);
        LocalDateTime end = now.minusHours(72);
        List<Member> members = memberRepository.findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(start, end);
        fcmService.sendInactiveUserNotification(members, 72);
        return HttpResponseUtil.ok(new TestResult("Sent to " + members.size() + " members (72h inactive)", members.size()));
    }

    @PostMapping("/single")
    @Operation(
            summary = "단건 FCM 테스트",
            description = "특정 FCM 토큰으로 테스트 알림을 발송합니다."
    )
    public ResponseEntity<CommonResponse<TestResult>> testSingle(@RequestBody FcmTestRequest request) {
        String messageId = fcmService.sendToOne(request.token, request.title, request.body);
        return HttpResponseUtil.ok(new TestResult("Sent successfully. Message ID: " + messageId, 1));
    }

    public record TestResult(String message, int count) {}

    public record FcmTestRequest(String token, String title, String body) {}
}
