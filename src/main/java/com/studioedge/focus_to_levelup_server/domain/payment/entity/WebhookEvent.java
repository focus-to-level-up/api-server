package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.WebhookEventStatus;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events", indexes = {
        @Index(name = "idx_webhook_app_user_id", columnList = "appUserId"),
        @Index(name = "idx_webhook_event_type", columnList = "eventType"),
        @Index(name = "idx_webhook_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WebhookEvent {

    @Id
    @Column(length = 255)
    private String eventId;  // RevenueCat 이벤트 ID (PK로 사용하여 중복 처리 방지)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WebhookEventType eventType;

    @Column(nullable = false)
    private String appUserId;  // Member ID

    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime processedAt;

    @Builder
    public WebhookEvent(String eventId, WebhookEventType eventType, String appUserId,
                        String productId, WebhookEventStatus status, String rawPayload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.appUserId = appUserId;
        this.productId = productId;
        this.status = status != null ? status : WebhookEventStatus.RECEIVED;
        this.rawPayload = rawPayload;
    }

    public void markAsProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
    }

    public void markAsFailed(String errorMessage) {
        this.status = WebhookEventStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
