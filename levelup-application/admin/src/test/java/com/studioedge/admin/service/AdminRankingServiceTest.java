package com.studioedge.admin.service;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRankingServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminRankingService adminRankingService;

    @Test
    void rejectsRankingBanForMemberWhoIsNotActive() {
        Member withdrawnMember = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.WITHDRAWN)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(withdrawnMember));

        assertThatThrownBy(() -> adminRankingService.excludeMemberFromRanking(1L))
                .isInstanceOf(InvalidAdminMemberOperationException.class)
                .hasMessage("활성 회원만 랭킹에서 정지할 수 있습니다.");
    }
}
