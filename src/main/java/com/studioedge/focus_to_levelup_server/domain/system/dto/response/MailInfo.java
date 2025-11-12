package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 우편 정보
 */
@Builder
public record MailInfo(
        Long id,
        String senderName,
        MailType type,
        String title,
        String description,
        Integer reward,
        Boolean isReceived,
        LocalDate expiredAt,
        String formattedExpiredAt
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public static MailInfo from(Mail mail) {
        return MailInfo.builder()
                .id(mail.getId())
                .senderName(mail.getSenderName())
                .type(mail.getType())
                .title(mail.getTitle())
                .description(mail.getDescription())
                .reward(mail.getReward())
                .isReceived(mail.getIsReceived())
                .expiredAt(mail.getExpiredAt())
                .formattedExpiredAt(mail.getExpiredAt().format(DATE_FORMATTER) + " 까지")
                .build();
    }
}
