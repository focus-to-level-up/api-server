package com.studioedge.admin.ranking;

import com.studioedge.admin.member.InvalidMemberOperationException;
import com.studioedge.admin.ranking.dto.RankingResponse;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.enums.SocialType;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Ranking;
import com.studioedge.ranking.enums.Tier;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void rejectsRankingBanForMemberWhoIsNotActive() {
        Member withdrawnMember = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("social-id")
                .nickname("focus")
                .status(MemberStatus.WITHDRAWN)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(withdrawnMember));

        assertThatThrownBy(() -> rankingService.excludeMemberFromRanking(1L))
                .isInstanceOf(InvalidMemberOperationException.class)
                .hasMessage("활성 회원만 랭킹에서 정지할 수 있습니다.");
    }

    @Test
    void reportsStoredAndActualMemberCountMismatchAndFiltersMembers() {
        League league = mock(League.class);
        Ranking ranking = mock(Ranking.class);
        Member member = Member.builder()
                .socialType(SocialType.KAKAO)
                .socialId("must-not-be-exposed")
                .nickname("focus")
                .status(MemberStatus.ACTIVE)
                .build();
        when(league.getCurrentMembers()).thenReturn(100);
        when(ranking.getMember()).thenReturn(member);
        when(ranking.getTier()).thenReturn(Tier.BRONZE);
        when(leagueRepository.findById(10L)).thenReturn(Optional.of(league));
        when(rankingRepository.findAllBySortedLeague(league)).thenReturn(List.of(ranking));

        RankingResponse response = rankingService.getRankingsByLeague(10L, "focus");

        assertThat(response.actualMembers()).isEqualTo(1);
        assertThat(response.storedMembers()).isEqualTo(100);
        assertThat(response.memberCountMismatch()).isTrue();
        assertThat(response.rankings()).extracting(RankingResponse.RankingInfo::nickname)
                .containsExactly("focus");
    }
}
