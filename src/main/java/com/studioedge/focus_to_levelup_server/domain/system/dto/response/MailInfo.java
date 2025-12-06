package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 우편 정보
 * Flutter UI/UX 개선을 위한 추가 필드 포함
 */
@Builder
public record MailInfo(
        Long id,
        String senderName,
        MailType type,
        String title,
        String description,
        String popupTitle,           // 팝업 타이틀 (UI용)
        String popupContent,         // 팝업 내용 (UI용, 멀티라인 가능)
        Integer reward,
        String formattedDiamondAmount,  // "x500", "x1K", "x3K" 등
        Boolean isReceived,
        LocalDate expiredAt,
        String formattedExpiredAt,
        Integer daysUntilExpired,    // 남은 일수
        String iconType,             // "DIAMOND", "SUBSCRIPTION", "TIER", "CHARACTER" 등

        // === 보상 상세 정보 (nullable) ===
        Integer diamondAmount,           // 다이아 개수
        Integer goldAmount,              // 골드 개수
        Long characterId,                // 캐릭터 ID
        String characterImageUrl,        // 캐릭터 이미지 URL
        Tier profileBorderTier,          // 프로필 테두리 티어
        String profileBorderImageUrl,    // 프로필 테두리 이미지 URL
        Integer bonusTicketCount         // 보너스 티켓 개수
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public static MailInfo from(Mail mail) {
        return MailInfo.builder()
                .id(mail.getId())
                .senderName(mail.getSenderName())
                .type(mail.getType())
                .title(mail.getTitle())
                .description(mail.getDescription())
                .popupTitle(mail.getPopupTitle())
                .popupContent(mail.getPopupContent())
                .reward(mail.getReward())
                .formattedDiamondAmount(mail.getFormattedDiamondAmount())
                .isReceived(mail.getIsReceived())
                .expiredAt(mail.getExpiredAt())
                .formattedExpiredAt(mail.getExpiredAt().format(DATE_FORMATTER) + " 까지")
                .daysUntilExpired(mail.getDaysUntilExpired())
                .iconType(determineIconType(mail.getType()))
                // 보상 상세 정보
                .diamondAmount(mail.getDiamondAmount())
                .goldAmount(mail.getGoldAmount())
                .characterId(mail.getCharacterId())
                .characterImageUrl(mail.getCharacterImageUrl())
                .profileBorderTier(mail.getProfileBorderTier())
                .profileBorderImageUrl(mail.getProfileBorderImageUrl())
                .bonusTicketCount(mail.getBonusTicketCount())
                .build();
    }

    /**
     * 우편 타입에 따른 아이콘 타입 결정
     */
    private static String determineIconType(MailType type) {
        return switch (type) {
            case GUILD_WEEKLY, TIER_PROMOTION, SEASON_END, EVENT -> "DIAMOND";
            case GIFT_BONUS_TICKET -> "BONUS_TICKET";
            case CHARACTER_SELECTION_TICKET, CHARACTER_REWARD -> "CHARACTER";
            case COUPON -> "EVENT"; // 쿠폰은 다양한 보상 가능 (기본: EVENT 아이콘)
            case PROFILE_BORDER -> "PROFILE_BORDER";
        };
    }
}
