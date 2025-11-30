package com.studioedge.focus_to_levelup_server.domain.payment.service.revenuecat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * RevenueCat REST API 호출 서비스
 * @see <a href="https://www.revenuecat.com/docs/api-v1">RevenueCat API v1</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueCatApiService {

    private final WebClient webClient;

    @Value("${revenuecat.base-url}")
    private String baseUrl;

    @Value("${revenuecat.secret-key}")
    private String secretKey;

    @Value("${revenuecat.entitlement-id.premium}")
    private String premiumEntitlementId;

    @Value("${revenuecat.enabled}")
    private boolean enabled;

    /**
     * 사전예약 프리미엄 구독권 부여 (14일)
     * end_time_ms 파라미터를 사용하여 정확히 14일 기간 설정
     *
     * @param appUserId 사용자 ID (Member ID)
     */
    public void grantPreRegistrationPremium(Long appUserId) {
        if (!enabled) {
            log.warn("RevenueCat is disabled. Skipping grant promotional entitlement for user {}", appUserId);
            return;
        }

        // 14일 = 현재 시간 + 14일 (밀리초)
        long endTimeMs = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000);
        grantPromotionalEntitlementWithEndTime(appUserId, endTimeMs);

        log.info("Granted 14-day premium promotional entitlement to user {}", appUserId);
    }

    /**
     * RevenueCat Grant Promotional Entitlement API (end_time_ms 사용)
     * 정확한 만료 시간을 지정하여 promotional entitlement 부여
     *
     * @param appUserId 사용자 ID (Member ID)
     * @param endTimeMs 만료 시간 (Unix timestamp in milliseconds)
     * @see <a href="https://www.revenuecat.com/docs/api-v1#tag/Entitlements/operation/grant-a-promotional-entitlement">Grant Promotional Entitlement</a>
     */
    public void grantPromotionalEntitlementWithEndTime(Long appUserId, long endTimeMs) {
        if (!enabled) {
            log.warn("RevenueCat is disabled. Skipping grant promotional entitlement for user {}", appUserId);
            return;
        }

        String url = String.format("%s/subscribers/%s/entitlements/%s/promotional",
                baseUrl, appUserId, premiumEntitlementId);

        Map<String, Object> requestBody = Map.of("end_time_ms", endTimeMs);

        try {
            webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info(
                            "Granted {} promotional entitlement (expires: {}) to user {}",
                            premiumEntitlementId, endTimeMs, appUserId))
                    .doOnError(error -> log.error(
                            "Failed to grant promotional entitlement to user {}: {}",
                            appUserId, error.getMessage()))
                    .block(Duration.ofSeconds(10));

        } catch (Exception e) {
            log.error("Exception while granting promotional entitlement to user {}", appUserId, e);
            throw new RuntimeException("Failed to grant promotional entitlement", e);
        }
    }

    /**
     * RevenueCat Grant Promotional Entitlement API (duration 사용)
     *
     * @param appUserId 사용자 ID (Member ID)
     * @param duration 기간 (daily, three_day, weekly, monthly, yearly, lifetime)
     * @see <a href="https://www.revenuecat.com/docs/api-v1#tag/Entitlements/operation/grant-a-promotional-entitlement">Grant Promotional Entitlement</a>
     */
    public void grantPromotionalEntitlement(Long appUserId, String duration) {
        if (!enabled) {
            log.warn("RevenueCat is disabled. Skipping grant promotional entitlement for user {}", appUserId);
            return;
        }

        String url = String.format("%s/subscribers/%s/entitlements/%s/promotional",
                baseUrl, appUserId, premiumEntitlementId);

        Map<String, Object> requestBody = Map.of("duration", duration);

        try {
            webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info(
                            "Granted {} promotional entitlement ({}) to user {}",
                            premiumEntitlementId, duration, appUserId))
                    .doOnError(error -> log.error(
                            "Failed to grant promotional entitlement to user {}: {}",
                            appUserId, error.getMessage()))
                    .block(Duration.ofSeconds(10));

        } catch (Exception e) {
            log.error("Exception while granting promotional entitlement to user {}", appUserId, e);
            throw new RuntimeException("Failed to grant promotional entitlement", e);
        }
    }
}
