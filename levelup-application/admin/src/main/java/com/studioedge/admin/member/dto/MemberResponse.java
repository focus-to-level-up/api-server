package com.studioedge.admin.member.dto;

import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.enums.SocialType;
import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.common.enums.CategorySubType;

import java.time.LocalDateTime;
public record MemberResponse(
        Long memberId,
        String nickname,
        SocialType socialType,
        MemberStatus status,
        Integer currentLevel,
        String profileMessage,
        String school,
        String schoolAddress,
        CategoryMainType categoryMain,
        CategorySubType categorySub,
        Integer gold,
        Integer diamond,
        LocalDateTime createdAt,
        LocalDateTime lastLoginDateTime
) {
    public static MemberResponse from(Member member, MemberInfo memberInfo) {
        return new MemberResponse(
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
