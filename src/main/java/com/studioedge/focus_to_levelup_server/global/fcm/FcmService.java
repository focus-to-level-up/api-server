package com.studioedge.focus_to_levelup_server.global.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FCM 메시지 발송 코어 서비스
 * - Firebase Cloud Messaging API를 사용하여 푸시 알림 발송
 * - 단일 발송, 대량 발송(Multicast) 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private static final String NOTIFICATION_TITLE = "Focus to Level Up";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * 단일 기기로 푸시 알림 발송
     *
     * @param fcmToken FCM 토큰
     * @param type 알림 유형
     * @param additionalData 추가 데이터 (targetId, requesterId 등)
     * @param messageArgs 메시지 포맷팅 인자 (닉네임 등)
     * @return 발송 성공 여부
     */
    public boolean sendNotification(
            String fcmToken,
            NotificationType type,
            Map<String, String> additionalData,
            String... messageArgs
    ) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token is null or blank. Skipping notification.");
            return false;
        }

        try {
            // 메시지 본문 생성
            String messageBody = type.formatMessage(messageArgs);

            // FCM 데이터 페이로드 구성
            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());
            data.put("targetScreen", type.getTargetScreen());
            data.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            // 추가 데이터 병합
            if (additionalData != null) {
                data.putAll(additionalData);
            }

            // FCM 메시지 빌드
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(NOTIFICATION_TITLE)
                            .setBody(messageBody)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setContentAvailable(true)
                                    .build())
                            .putHeader("apns-priority", "10")
                            .build())
                    .build();

            // FCM 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM message sent successfully. Type: {}, Response: {}", type, response);
            return true;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message. Type: {}, Token: {}, Error: {}",
                    type, fcmToken, e.getMessage());

            // 토큰 만료/유효하지 않은 경우 로그 추가
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM token is invalid or expired: {}", fcmToken);
            }

            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM message", e);
            return false;
        }
    }

    /**
     * MulticastMessage를 사용한 대량 발송
     * - 최대 500개 토큰까지 한 번에 발송 가능
     * - 1번의 API 호출로 여러 디바이스에 동시 발송
     *
     * @param fcmTokens FCM 토큰 리스트 (최대 500개)
     * @param type 알림 유형
     * @param additionalData 추가 데이터
     * @param messageArgs 메시지 포맷팅 인자
     * @return 발송 성공한 토큰 개수
     */
    public int sendMulticastNotification(
            List<String> fcmTokens,
            NotificationType type,
            Map<String, String> additionalData,
            String... messageArgs
    ) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            log.warn("FCM token list is null or empty. Skipping multicast notification.");
            return 0;
        }

        // null 또는 빈 토큰 필터링
        List<String> validTokens = fcmTokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .toList();

        if (validTokens.isEmpty()) {
            log.warn("No valid FCM tokens found. Skipping multicast notification.");
            return 0;
        }

        if (validTokens.size() > 500) {
            log.warn("Token count exceeds 500. Sending only first 500 tokens.");
            validTokens = validTokens.subList(0, 500);
        }

        try {
            // 메시지 본문 생성
            String messageBody = type.formatMessage(messageArgs);

            // FCM 데이터 페이로드 구성
            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());
            data.put("targetScreen", type.getTargetScreen());
            data.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            if (additionalData != null) {
                data.putAll(additionalData);
            }

            // MulticastMessage 빌드
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(validTokens)
                    .setNotification(Notification.builder()
                            .setTitle(NOTIFICATION_TITLE)
                            .setBody(messageBody)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setContentAvailable(true)
                                    .build())
                            .putHeader("apns-priority", "10")
                            .build())
                    .build();

            // 대량 발송
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("Multicast FCM notification sent. Type: {}, Total: {}, Success: {}, Failure: {}",
                    type, validTokens.size(), response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 로깅 (선택적)
            if (response.getFailureCount() > 0) {
                for (int i = 0; i < response.getResponses().size(); i++) {
                    SendResponse sendResponse = response.getResponses().get(i);
                    if (!sendResponse.isSuccessful()) {
                        log.warn("Failed to send to token: {}, Error: {}",
                                validTokens.get(i), sendResponse.getException().getMessage());
                    }
                }
            }

            return response.getSuccessCount();

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM notification. Type: {}, Error: {}",
                    type, e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("Unexpected error while sending multicast FCM notification", e);
            return 0;
        }
    }
}
