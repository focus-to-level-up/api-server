package com.studioedge.admin.mail.dto;

import com.studioedge.mail.entity.Mail;
import com.studioedge.mail.enums.MailType;

import java.time.LocalDate;
import java.time.LocalDateTime;
public record MailResponse(
        Long mailId,
        Long receiverId,
        String receiverNickname,
        MailType type,
        String title,
        String description,
        Integer diamondAmount,
        Integer goldAmount,
        Integer bonusTicketCount,
        Boolean isReceived,
        LocalDate expiredAt,
        LocalDateTime createdAt
) {
    public static MailResponse from(Mail mail) {
        return new MailResponse(
                mail.getId(),
                mail.getReceiver().getId(),
                mail.getReceiver().getNickname(),
                mail.getType(),
                mail.getTitle(),
                mail.getDescription(),
                mail.getDiamondAmount(),
                mail.getGoldAmount(),
                mail.getBonusTicketCount(),
                mail.getIsReceived(),
                mail.getExpiredAt(),
                mail.getCreatedAt()
        );
    }
}
