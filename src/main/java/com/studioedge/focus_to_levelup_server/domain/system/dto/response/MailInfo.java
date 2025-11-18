package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

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
        String iconType              // "DIAMOND", "SUBSCRIPTION", "TIER", "CHARACTER" 등
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
                .build();
    }

    /**
     * 우편 타입에 따른 아이콘 타입 결정
     */
    private static String determineIconType(MailType type) {
        return switch (type) {
            case PRE_REGISTRATION, GUILD_WEEKLY, TIER_PROMOTION, SEASON_END, EVENT, PURCHASE, RANKING, GUILD -> "DIAMOND";
            case GIFT_SUBSCRIPTION, SUBSCRIPTION -> "SUBSCRIPTION";
            case GIFT_BONUS_TICKET -> "BONUS_TICKET";
            case CHARACTER_REWARD, CHARACTER_SELECTION_TICKET -> "CHARACTER";
        };
    }
}
