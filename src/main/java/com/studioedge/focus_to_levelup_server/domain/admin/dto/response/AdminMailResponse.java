package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "관리자용 우편 응답")
public record AdminMailResponse(
        @Schema(description = "우편 ID", example = "1")
        Long mailId,

        @Schema(description = "수신자 ID", example = "1")
        Long receiverId,

        @Schema(description = "수신자 닉네임", example = "공부왕")
        String receiverNickname,

        @Schema(description = "우편 타입", example = "EVENT")
        MailType type,

        @Schema(description = "제목", example = "CS 보상 지급")
        String title,

        @Schema(description = "설명", example = "문의하신 내용에 대한 보상입니다.")
        String description,

        @Schema(description = "다이아 지급량", example = "100")
        Integer diamondAmount,

        @Schema(description = "골드 지급량", example = "500")
        Integer goldAmount,

        @Schema(description = "보너스 티켓 지급량", example = "1")
        Integer bonusTicketCount,

        @Schema(description = "수령 여부", example = "false")
        Boolean isReceived,

        @Schema(description = "만료일", example = "2024-04-20")
        LocalDate expiredAt,

        @Schema(description = "생성일", example = "2024-03-21T10:30:00")
        LocalDateTime createdAt
) {
    public static AdminMailResponse from(Mail mail) {
        return new AdminMailResponse(
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