package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 닉네임으로 회원 검색
     */
    public AdminMemberResponse searchMemberByNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                .orElse(null);
        return AdminMemberResponse.from(member, memberInfo);
    }

    /**
     * 회원 ID로 회원 조회
     */
    public AdminMemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElse(null);
        return AdminMemberResponse.from(member, memberInfo);
    }

    /**
     * 닉네임 변경 (1달 제한 무시)
     */
    @Transactional
    public AdminMemberResponse updateNickname(Long memberId, String newNickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // Admin은 1달 제한 없이 변경 가능
        member.updateNickname(newNickname);

        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElse(null);
        return AdminMemberResponse.from(member, memberInfo);
    }

    /**
     * 상태 메시지 변경
     */
    @Transactional
    public AdminMemberResponse updateProfileMessage(Long memberId, String newProfileMessage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(MemberNotFoundException::new);

        memberInfo.updateProfileMessage(newProfileMessage);

        return AdminMemberResponse.from(member, memberInfo);
    }

    /**
     * 학교 정보 변경
     */
    @Transactional
    public AdminMemberResponse updateSchool(Long memberId, String school, String schoolAddress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(MemberNotFoundException::new);

        memberInfo.updateSchool(school, schoolAddress);

        return AdminMemberResponse.from(member, memberInfo);
    }
}
