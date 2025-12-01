package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "payment_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(unique = true, nullable = false)
    private String productTransactionId; // 결제 영수증 (transactionId)

    private BigDecimal paidAmount; // 실제 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDING'")
    private PurchaseStatus status; // 결제 상태

    @Enumerated(EnumType.STRING)
    private PaymentPlatform platform; // Apple/Google

    @Column(columnDefinition = "TEXT")
    private String receiptData; // 영수증 데이터

    @Column(unique = true)
    private String revenueCatEventId; // RevenueCat Webhook 이벤트 ID (멱등성 보장)

    private LocalDateTime refundedAt; // 환불 일시

    private String refundReason; // 환불 사유

    @Builder
    public PaymentLog(Member member, Product product, String productTransactionId,
                      BigDecimal paidAmount, PurchaseStatus status, PaymentPlatform platform,
                      String receiptData, String revenueCatEventId) {
        this.member = member;
        this.product = product;
        this.productTransactionId = productTransactionId;
        this.paidAmount = paidAmount;
        this.status = status != null ? status : PurchaseStatus.PENDING;
        this.platform = platform;
        this.receiptData = receiptData;
        this.revenueCatEventId = revenueCatEventId;
    }

    // 비즈니스 메서드
    public boolean isRefundable() {
        if (status != PurchaseStatus.COMPLETED) {
            return false;
        }
        // 구매일로부터 7일 이내인지 확인
        LocalDateTime createdAt = this.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        long daysSinceCreated = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        return daysSinceCreated <= 7;
    }

    public void refund(String reason) {
        if (!isRefundable()) {
            throw new IllegalStateException("환불이 불가능한 결제입니다");
        }
        this.status = PurchaseStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundReason = reason;
    }
}
