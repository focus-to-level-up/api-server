package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MemberSettingDto(
        @Schema(description = "알람 여부", example = "true")
        @NotNull
        Boolean alarmOn,
        @Schema(description = "뽀모도로 여부", example = "true")
        @NotNull
        Boolean isPomodoro,
        @Schema(description = "AI플래너 여부", example = "true")
        @NotNull
        Boolean isAIPlanner,
        @Schema(description = "구독권 메세지 여부", example = "true")
        @NotNull
        Boolean isSubscriptionMessageBlocked
) {
    public static MemberSettingDto of(MemberSetting memberSetting) {
        return MemberSettingDto.builder()
                .alarmOn(memberSetting.getAlarmOn())
                .isPomodoro(memberSetting.getIsPomodoro())
                .isAIPlanner(memberSetting.getIsAIPlanner())
                .isSubscriptionMessageBlocked(memberSetting.getIsSubscriptionMessageBlocked())
                .build();
    }
}
