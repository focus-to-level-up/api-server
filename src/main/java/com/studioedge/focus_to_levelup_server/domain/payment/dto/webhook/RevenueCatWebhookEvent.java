package com.studioedge.focus_to_levelup_server.domain.payment.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * RevenueCat Webhook 이벤트 DTO
 * @see <a href="https://www.revenuecat.com/docs/integrations/webhooks/event-types-and-fields">RevenueCat Webhook Docs</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatWebhookEvent {

    @JsonProperty("api_version")
    private String apiVersion;

    private EventPayload event;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventPayload {
        private String type;  // INITIAL_PURCHASE, RENEWAL, CANCELLATION, etc.
        private String id;    // 이벤트 고유 ID

        @JsonProperty("app_id")
        private String appId;

        @JsonProperty("event_timestamp_ms")
        private Long eventTimestampMs;

        @JsonProperty("app_user_id")
        private String appUserId;  // 서버 Member ID

        @JsonProperty("original_app_user_id")
        private String originalAppUserId;

        private List<String> aliases;

        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("entitlement_ids")
        private List<String> entitlementIds;

        @JsonProperty("period_type")
        private String periodType;

        @JsonProperty("purchased_at_ms")
        private Long purchasedAtMs;

        @JsonProperty("expiration_at_ms")
        private Long expirationAtMs;

        private String store;  // APP_STORE, PLAY_STORE

        private String environment;  // SANDBOX, PRODUCTION

        @JsonProperty("transaction_id")
        private String transactionId;

        @JsonProperty("original_transaction_id")
        private String originalTransactionId;

        private BigDecimal price;

        private String currency;

        @JsonProperty("is_trial_conversion")
        private Boolean isTrialConversion;

        @JsonProperty("cancel_reason")
        private String cancelReason;

        @JsonProperty("expiration_reason")
        private String expirationReason;

        @JsonProperty("country_code")
        private String countryCode;

        /**
         * 환불 여부 확인
         */
        public boolean isRefund() {
            return cancelReason != null &&
                    (cancelReason.contains("REFUND") || "CUSTOMER_SUPPORT".equals(cancelReason));
        }

        /**
         * Apple 스토어 여부 확인
         */
        public boolean isAppleStore() {
            return "APP_STORE".equalsIgnoreCase(store);
        }

        /**
         * Google Play 스토어 여부 확인
         */
        public boolean isGooglePlayStore() {
            return "PLAY_STORE".equalsIgnoreCase(store);
        }

        /**
         * Free Trial 여부 확인
         * RevenueCat에서 Free Trial인 경우 period_type이 "TRIAL"로 전송됨
         */
        public boolean isFreeTrial() {
            return "TRIAL".equalsIgnoreCase(periodType);
        }

        /**
         * Free Trial → 유료 구독 전환 여부 확인
         */
        public boolean isTrialConversion() {
            return Boolean.TRUE.equals(isTrialConversion);
        }
    }
}
