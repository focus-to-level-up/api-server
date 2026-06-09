package com.studioedge.admin.service;

import com.studioedge.admin.dto.response.AdminMemberResponse;
import com.studioedge.admin.exception.InvalidAdminMemberOperationException;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.enums.SocialType;
import com.studioedge.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminMemberService adminMemberService;

    @Test
    void rejectsRestoreForMemberWhoIsNotRankingBanned() {
        Member activeMember = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.ACTIVE)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> adminMemberService.restoreMember(1L))
                .isInstanceOf(InvalidAdminMemberOperationException.class)
                .hasMessage("랭킹 정지 회원만 복구할 수 있습니다.");
    }

    @Test
    void searchesNicknameWithPageableAndMapsResponse() {
        PageRequest pageable = PageRequest.of(1, 30);
        Member member = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.ACTIVE)
                .build();
        when(memberRepository.findByNicknameContaining("focus", pageable))
                .thenReturn(new PageImpl<>(List.of(member), pageable, 31));

        Page<AdminMemberResponse> result = adminMemberService.searchMembers("NICKNAME", "focus", pageable);

        assertThat(result.getTotalElements()).isEqualTo(31);
        assertThat(result.getContent()).extracting(AdminMemberResponse::nickname).containsExactly("focus");
        verify(memberRepository).findByNicknameContaining("focus", pageable);
    }
}
