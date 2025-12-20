package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "mails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member receiver;

    @Column(nullable = false)
    @ColumnDefault("'운영자'")
    private String senderName = "운영자";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MailType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 999, nullable = false)
    private String description;

    private String popupTitle; // 팝업 타이틀 (UI용)

    @Column(length = 999)
    private String popupContent; // 팝업 내용 (UI용, 멀티라인 가능)

    @Column(nullable = false)
    @ColumnDefault("0")
    private int reward = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceived = false;

    @Column(nullable = false)
    private LocalDate expiredAt;

    @Column(name = "payment_log_id")
    private Long paymentLogId; // 구매 관련 우편의 경우 결제 로그 ID 저장

    // === 보상 상세 정보 (nullable) ===

    /** 다이아 개수 (DIAMOND 관련 보상) */
    private Integer diamondAmount;

    /** 골드 개수 */
    private Integer goldAmount;

    /** 캐릭터 ID */
    private Long characterId;

    /** 캐릭터 이미지 URL */
    private String characterImageUrl;

    /** 프로필 테두리 티어 */
    @Enumerated(EnumType.STRING)
    private Tier profileBorderTier;

    /** 프로필 테두리 이미지 URL */
    private String profileBorderImageUrl;

    /** 보너스 티켓 개수 */
    private Integer bonusTicketCount;

    /** 자산 이름 (PROFILE_BORDER 관련 보상) */
    private String assetName;

    /** 허용 등급 (CHARACTER_SELECTION_TICKET 전용) */
    private String allowedRarity;

    @Builder
    public Mail(Member receiver, String senderName, MailType type, String title,
                String description, String popupTitle, String popupContent, Integer reward, LocalDate expiredAt, Long paymentLogId,
                Integer diamondAmount, Integer goldAmount,
                Long characterId, String characterImageUrl, Tier profileBorderTier, String profileBorderImageUrl, Integer bonusTicketCount,
                String assetName, String allowedRarity)
    {
        this.receiver = receiver;
        this.senderName = senderName != null ? senderName : "운영자";
        this.type = type;
        this.title = title;
        this.description = description;
        this.popupTitle = popupTitle;
        this.popupContent = popupContent;
        this.reward = reward != null ? reward : 0;
        this.expiredAt = expiredAt;
        this.paymentLogId = paymentLogId;
        // 보상 상세 정보
        this.diamondAmount = diamondAmount;
        this.goldAmount = goldAmount;
        this.characterId = characterId;
        this.characterImageUrl = characterImageUrl;
        this.profileBorderTier = profileBorderTier;
        this.profileBorderImageUrl = profileBorderImageUrl;
        this.bonusTicketCount = bonusTicketCount;
        this.assetName = assetName;
        this.allowedRarity = allowedRarity;
    }

    // 비즈니스 메서드

    /**
     * 우편 수령 처리
     */
    public void markAsReceived() {
        this.isReceived = true;
    }

    /**
     * 우편 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiredAt);
    }

    /**
     * 우편 소유권 확인
     */
    public boolean isOwnedBy(Long memberId) {
        return this.receiver.getId().equals(memberId);
    }

    /**
     * 만료까지 남은 일수 계산
     */
    public int getDaysUntilExpired() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), expiredAt);
    }

    /**
     * 다이아 포맷팅 (x500, x1K, x1.8K 등)
     */
    public String getFormattedDiamondAmount() {
        if (reward < 1000) {
            return "x" + reward;
        } else {
            double k = reward / 1000.0;
            if (k == (int) k) {
                return "x" + (int) k + "K";
            } else {
                return "x" + String.format("%.1fK", k);
            }
        }
    }

    /**
     * 메일 내용 업데이트 (batch 시스템 활용)
     * */
    public void updateRewardInfo(String title, String description, String popupTitle,
                                 String popupContent, int reward) {
        this.title = title;
        this.description = description;
        this.popupTitle = popupTitle;
        this.popupContent = popupContent;
        this.reward = reward;
    }
}
