package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceiptValidationService implements ReceiptValidator {
    private final AppleReceiptValidator appleReceiptValidator;
    private final GoogleReceiptValidator googleReceiptValidator;

    @Autowired(required = false)
    private MockReceiptValidator mockReceiptValidator;

    @Value("${payment.mode}")
    private String paymentMode;

    public ReceiptValidationService(AppleReceiptValidator appleReceiptValidator,
                                   GoogleReceiptValidator googleReceiptValidator) {
        this.appleReceiptValidator = appleReceiptValidator;
        this.googleReceiptValidator = googleReceiptValidator;
    }

    @Override
    public ReceiptValidationResult validate(String receiptData, PaymentPlatform platform) {
        // Mock 모드: 항상 성공 반환 (local, dev 프로파일에서만 사용 가능)
        if ("mock".equalsIgnoreCase(paymentMode)) {
            if (mockReceiptValidator != null) {
                log.info("[MOCK MODE] Skipping actual receipt validation");
                return mockReceiptValidator.validate(receiptData);
            } else {
                log.warn("[MOCK MODE] MockReceiptValidator is not available in this profile");
            }
        }

        // 실제 검증
        return switch (platform) {
            case APPLE -> appleReceiptValidator.validate(receiptData);
            case GOOGLE -> {
                // Google은 purchaseToken과 productId가 필요
                // receiptData를 JSON으로 파싱하거나, 별도 파라미터로 받아야 함
                // 일단 간단하게 구현 (실제로는 DTO로 분리 필요)
                String[] parts = receiptData.split("\\|");
                if (parts.length >= 2) {
                    String purchaseToken = parts[0];
                    String productId = parts[1];
                    yield googleReceiptValidator.validate(purchaseToken, productId);
                } else {
                    yield ReceiptValidationResult.failure("Google 영수증 형식이 잘못되었습니다");
                }
            }
        };
    }
}
