package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import lombok.Builder;

import java.util.List;

/**
 * 우편 목록 응답
 */
@Builder
public record MailListResponse(
        List<MailInfo> mails
) {
    public static MailListResponse from(List<Mail> mails) {
        return MailListResponse.builder()
                .mails(mails.stream()
                        .map(MailInfo::from)
                        .toList())
                .build();
    }
}
