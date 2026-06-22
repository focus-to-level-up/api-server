package com.studioedge.admin.member;

import com.studioedge.admin.member.dto.MemberResponse;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.entity.MemberSetting;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.enums.SocialType;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.enums.Tier;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import com.studioedge.common.enums.CategoryMainType;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void rejectsRestoreForMemberWhoIsNotRankingBanned() {
        Member activeMember = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.ACTIVE)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> memberService.restoreMember(1L))
                .isInstanceOf(InvalidMemberOperationException.class)
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

        Page<MemberResponse> result = memberService.searchMembers("NICKNAME", "focus", pageable);

        assertThat(result.getTotalElements()).isEqualTo(31);
        assertThat(result.getContent()).extracting(MemberResponse::nickname).containsExactly("focus");
        verify(memberRepository).findByNicknameContaining("focus", pageable);
    }

    @Test
    void restoresRankingBannedMemberIntoBronzeLeagueImmediately() {
        MemberInfo memberInfo = mock(MemberInfo.class);
        MemberSetting memberSetting = mock(MemberSetting.class);
        Member member = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.RANKING_BANNED)
                .memberInfo(memberInfo)
                .memberSetting(memberSetting)
                .build();
        League bronzeLeague = mock(League.class);
        when(memberInfo.getCategoryMain()).thenReturn(CategoryMainType.HIGH_SCHOOL);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(leagueRepository.findSmallestBronzeLeagueForCategory(CategoryMainType.HIGH_SCHOOL))
                .thenReturn(Optional.of(bronzeLeague));

        memberService.restoreMember(1L);

        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        verify(bronzeLeague).increaseCurrentMembers();
        verify(rankingRepository).save(org.mockito.ArgumentMatchers.argThat(ranking -> ranking.getTier() == Tier.BRONZE));
    }
}
