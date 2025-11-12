package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AppleReceiptValidator {
    private static final String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
    private static final String PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String sharedSecret;
    private final boolean useSandbox;

    public AppleReceiptValidator(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${payment.apple.shared-secret}") String sharedSecret,
            @Value("${payment.apple.use-sandbox:true}") boolean useSandbox
    ) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.sharedSecret = sharedSecret;
        this.useSandbox = useSandbox;
    }

    public ReceiptValidationResult validate(String receiptData) {
        try {
            // 1. 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("receipt-data", receiptData);
            requestBody.put("password", sharedSecret);
            requestBody.put("exclude-old-transactions", true);

            // 2. Production 검증 시도
            String url = useSandbox ? SANDBOX_URL : PRODUCTION_URL;
            String responseBody = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode response = objectMapper.readTree(responseBody);
            int status = response.get("status").asInt();

            // 3. Sandbox 영수증인 경우 재시도
            if (status == 21007 && !useSandbox) {
                log.info("Receipt is from sandbox, retrying with sandbox URL");
                responseBody = webClient.post()
                        .uri(SANDBOX_URL)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                response = objectMapper.readTree(responseBody);
                status = response.get("status").asInt();
            }

            // 4. 검증 성공 (status 0)
            if (status == 0) {
                JsonNode receipt = response.get("receipt");
                JsonNode latestReceiptInfo = response.has("latest_receipt_info")
                        ? response.get("latest_receipt_info").get(0)
                        : receipt.get("in_app").get(0);

                String transactionId = latestReceiptInfo.get("transaction_id").asText();
                String productId = latestReceiptInfo.get("product_id").asText();
                Long purchaseDate = latestReceiptInfo.get("purchase_date_ms").asLong();

                // Apple은 영수증에 금액 정보 없음 (서버에서 검증 필요)
                return ReceiptValidationResult.success(transactionId, productId, null, purchaseDate);
            } else {
                String errorMessage = getAppleErrorMessage(status);
                log.error("Apple receipt validation failed: status={}, message={}", status, errorMessage);
                return ReceiptValidationResult.failure(errorMessage);
            }
        } catch (Exception e) {
            log.error("Failed to validate Apple receipt", e);
            return ReceiptValidationResult.failure("영수증 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String getAppleErrorMessage(int status) {
        return switch (status) {
            case 21000 -> "App Store가 JSON 객체를 읽을 수 없습니다";
            case 21002 -> "영수증 데이터 형식이 잘못되었습니다";
            case 21003 -> "영수증을 인증할 수 없습니다";
            case 21004 -> "제공된 공유 암호가 일치하지 않습니다";
            case 21005 -> "영수증 서버를 현재 사용할 수 없습니다";
            case 21006 -> "영수증이 유효하지만 구독이 만료되었습니다";
            case 21007 -> "영수증이 샌드박스용입니다";
            case 21008 -> "영수증이 프로덕션용입니다";
            case 21010 -> "영수증을 승인할 수 없습니다";
            default -> "알 수 없는 오류 (status: " + status + ")";
        };
    }
}
