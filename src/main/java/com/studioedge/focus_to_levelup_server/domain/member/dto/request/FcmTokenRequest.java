package com.studioedge.focus_to_levelup_server.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * FCM 토큰 등록/갱신 요청 DTO
 */
@Schema(description = "FCM 토큰 등록 요청")
public record FcmTokenRequest(
        @Schema(description = "FCM 토큰", example = "dXsK9Q:APA91bGXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        String fcmToken
) {
}