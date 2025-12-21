package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자용 회원 정보 응답")
public record AdminMemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "닉네임", example = "공부왕")
        String nickname,

        @Schema(description = "소셜 로그인 타입", example = "KAKAO")
        SocialType socialType,

        @Schema(description = "회원 상태", example = "ACTIVE")
        MemberStatus status,

        @Schema(description = "현재 레벨", example = "15")
        Integer currentLevel,

        @Schema(description = "상태 메시지", example = "열심히 공부중!")
        String profileMessage,

        @Schema(description = "학교명", example = "서울대학교")
        String school,

        @Schema(description = "학교 주소", example = "서울특별시 관악구")
        String schoolAddress,

        @Schema(description = "메인 카테고리", example = "HIGH_SCHOOL")
        CategoryMainType categoryMain,

        @Schema(description = "서브 카테고리", example = "HIGH_3")
        CategorySubType categorySub,

        @Schema(description = "골드", example = "1500")
        Integer gold,

        @Schema(description = "다이아", example = "300")
        Integer diamond,

        @Schema(description = "가입일", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "마지막 로그인", example = "2024-03-20T15:45:00")
        LocalDateTime lastLoginDateTime
) {
    public static AdminMemberResponse from(Member member, MemberInfo memberInfo) {
        return new AdminMemberResponse(
                member.getId(),
                member.getNickname(),
                member.getSocialType(),
                member.getStatus(),
                member.getCurrentLevel(),
                memberInfo != null ? memberInfo.getProfileMessage() : null,
                memberInfo != null ? memberInfo.getSchool() : null,
                memberInfo != null ? memberInfo.getSchoolAddress() : null,
                memberInfo != null ? memberInfo.getCategoryMain() : null,
                memberInfo != null ? memberInfo.getCategorySub() : null,
                memberInfo != null ? memberInfo.getGold() : 0,
                memberInfo != null ? memberInfo.getDiamond() : 0,
                member.getCreatedAt(),
                member.getLastLoginDateTime()
        );
    }
}
