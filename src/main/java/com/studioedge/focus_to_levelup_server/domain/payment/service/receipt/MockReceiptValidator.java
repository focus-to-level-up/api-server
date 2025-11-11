package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 로컬 개발용 Mock 영수증 검증기
 * 항상 성공을 반환합니다.
 *
 * @Profile("local", "dev"): 로컬 및 개발 환경에서만 활성화
 */
@Slf4j
@Service
@Profile({"local", "dev"})
public class MockReceiptValidator {

    public ReceiptValidationResult validate(String receiptData) {
        log.info("[MOCK] Receipt validation - always returns success");

        // Mock 데이터 생성
        String mockTransactionId = "MOCK_" + UUID.randomUUID().toString();
        String mockProductId = "mock.product.id";
        BigDecimal mockAmount = new BigDecimal("2200.00");
        Long mockPurchaseTime = System.currentTimeMillis();

        return ReceiptValidationResult.success(
                mockTransactionId,
                mockProductId,
                mockAmount,
                mockPurchaseTime
        );
    }
}
