package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Service
public class GoogleReceiptValidator {
    private static final String GOOGLE_API_BASE_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3";
    private static final String SCOPE = "https://www.googleapis.com/auth/androidpublisher";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String packageName;
    private final String serviceAccountJson;

    public GoogleReceiptValidator(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${payment.google.package-name}") String packageName,
            @Value("${payment.google.service-account-json}") String serviceAccountJson
    ) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.packageName = packageName;
        this.serviceAccountJson = serviceAccountJson;
    }

    public ReceiptValidationResult validate(String purchaseToken, String productId) {
        try {
            // 1. Google Service Account로 Access Token 발급
            String accessToken = getAccessToken();

            // 2. Google Play Developer API 호출
            String url = String.format(
                    "%s/applications/%s/purchases/products/%s/tokens/%s",
                    GOOGLE_API_BASE_URL, packageName, productId, purchaseToken
            );

            String responseBody = webClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode response = objectMapper.readTree(responseBody);

            // 3. 구매 상태 확인
            int purchaseState = response.get("purchaseState").asInt();
            if (purchaseState != 0) {
                // 0: Purchased, 1: Canceled, 2: Pending
                return ReceiptValidationResult.failure("구매가 완료되지 않았습니다 (상태: " + purchaseState + ")");
            }

            // 4. 소비 상태 확인 (중복 방지)
            int consumptionState = response.get("consumptionState").asInt();
            if (consumptionState == 1) {
                // 0: Yet to be consumed, 1: Consumed
                return ReceiptValidationResult.failure("이미 소비된 구매입니다");
            }

            // 5. 응답 파싱
            String orderId = response.get("orderId").asText();
            Long purchaseTimeMillis = response.get("purchaseTimeMillis").asLong();

            // Google은 가격 정보 제공 (마이크로 단위: 1,000,000 = 1원)
            BigDecimal priceAmountMicros = response.has("priceAmountMicros")
                    ? new BigDecimal(response.get("priceAmountMicros").asText())
                    : null;
            BigDecimal amount = priceAmountMicros != null
                    ? priceAmountMicros.divide(new BigDecimal("1000000"))
                    : null;

            return ReceiptValidationResult.success(orderId, productId, amount, purchaseTimeMillis);

        } catch (Exception e) {
            log.error("Failed to validate Google receipt", e);
            return ReceiptValidationResult.failure("영수증 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String getAccessToken() throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singleton(SCOPE));

        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
