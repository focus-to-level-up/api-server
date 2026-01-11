package com.studioedge.focus_to_levelup_server.domain.member.service;

import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    void completeSignUp(Member member, CompleteSignUpRequest request);

    void reportMember(Member member, Long memberId, ReportMemberRequest request);

    Page<ProfileAssetResponse> getMemberAsset(Member member, Pageable pageable);

    GetProfileResponse getMemberProfile(Long memberId);

    void updateMemberProfile(Member member, UpdateProfileRequest request);

    void updateNickname(Member member, UpdateNicknameRequest request);

    void updateCategory(Member member, UpdateCategoryRequest request);

    void updateMemberSetting(Member member, MemberSettingDto request);

    MemberSettingDto getMemberSetting(Member member);

    MemberCurrencyResponse getMemberCurrency(Member member);
}
