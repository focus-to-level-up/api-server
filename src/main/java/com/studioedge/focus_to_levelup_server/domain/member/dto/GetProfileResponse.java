package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetProfileResponse(
        @Schema(description = "유저 pk", example = "1")
        Long id,
        @Schema(description = "닉네임", example = "닉네임")
        String nickname,
        @Schema(description = "메인 카테고리", example = "HIGH_SCHOOL")
        CategoryMainType categoryMain,
        @Schema(description = "서브 카테고리", example = "HIGH_2")
        CategorySubType categorySub,
        @Schema(description = "프로필 이미지 pk", example = "2")
        Long profileImageId,
        @Schema(description = "프로필 이미지 url", example = "https://lvup-image.s3.ap-northeast-2.amazonaws.com/1629780000000.jpg")
        String profileImageUrl,
        @Schema(description = "프로필 테두리 pk", example = "3")
        Long profileBorderId,
        @Schema(description = "프로필 테두리 url", example = "https://lvup-image.s3.ap-northeast-2.amazonaws.com/1629780000000.jpg")
        String profileBorderUrl,
        @Schema(description = "소속 이름", example = "서울고등학교")
        String belonging,
        @Schema(description = "현재 티어", example = "BRONZE")
        String currentTier,
        @Schema(description = "자난 최고 티어", example = "SILVER")
        String highestTier,
        @Schema(description = "현재 레벨", example = "40")
        Integer currentLevel,
        @Schema(description = "총 레벨", example = "230")
        Integer totalLevel,
        @Schema(description = "현재 경험치", example = "200")
        Integer currentExp,
        @Schema(description = "부스트 여부", example = "true")
        Boolean boosted,
        @Schema(description = "집중 여부", example = "true")
        Boolean focusOn,
        @Schema(description = "구독 상태", example = "PREMIUM")
        SubscriptionType subscriptionType,
        @Schema(description = "프로필 메세지", example = "으아! 할수있다!!")
        String profileMessage,
        @Schema(description = "보너스 티켓 개수", example = "3")
        Integer bonusTicketCount,
        @Schema(description = "다이아 갯수", example = "400")
        Integer diamond,
        @Schema(description = "골드 갯수", example = "1500")
        Integer gold,
        @Schema(description = "오늘 공부 시간 상위 %", example = "0.2")
        Float topPercent,
        @Schema(description = "유저 등록 날짜", example = "2026-01-15T13:45:30.123")
        LocalDateTime createdAt
) {
    public static GetProfileResponse of(Member member, MemberInfo memberInfo, String ranking,
                                        SubscriptionType subscriptionType, boolean boosted, float topPercent) {
        return GetProfileResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .categoryMain(memberInfo.getCategoryMain())
                .categorySub(memberInfo.getCategorySub())
                .profileImageId(memberInfo.getProfileImage().getId())
                .profileImageUrl(memberInfo.getProfileImage().getAsset().getAssetUrl())
                .profileBorderId(memberInfo.getProfileBorder().getId())
                .profileBorderUrl(memberInfo.getProfileBorder().getAsset().getAssetUrl())
                .belonging(memberInfo.getSchool())
                .boosted(boosted)
                .focusOn(member.getIsFocusing())
                .subscriptionType(subscriptionType)
                .currentTier(ranking)
                .highestTier(memberInfo.getHighestTier() == null ? "-" : memberInfo.getHighestTier().toString())
                .currentLevel(member.getCurrentLevel())
                .currentExp(member.getCurrentExp())
                .totalLevel(memberInfo.getTotalLevel())
                .profileMessage(memberInfo.getProfileMessage())
                .bonusTicketCount(memberInfo.getBonusTicketCount())
                .diamond(memberInfo.getDiamond())
                .gold(memberInfo.getGold())
                .topPercent(topPercent)
                .createdAt(member.getCreatedAt())
                .build();
    }
}
