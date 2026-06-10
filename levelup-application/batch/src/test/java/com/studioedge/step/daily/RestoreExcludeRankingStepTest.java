package com.studioedge.step.daily;

import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.repository.MemberSettingRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestoreExcludeRankingStepTest {

    @Test
    void increasesBronzeLeagueMemberCountWhenRestoringMember() throws Exception {
        LeagueRepository leagueRepository = mock(LeagueRepository.class);
        RankingRepository rankingRepository = mock(RankingRepository.class);
        RestoreExcludeRankingStep step = new RestoreExcludeRankingStep(
                mock(JobRepository.class),
                mock(PlatformTransactionManager.class),
                mock(MemberSettingRepository.class),
                rankingRepository,
                leagueRepository
        );
        MemberInfo memberInfo = mock(MemberInfo.class);
        Member member = mock(Member.class);
        League bronzeLeague = mock(League.class);
        when(member.getMemberInfo()).thenReturn(memberInfo);
        when(memberInfo.getCategoryMain()).thenReturn(CategoryMainType.ADULT);
        when(leagueRepository.findSmallestBronzeLeagueForCategory(CategoryMainType.ADULT))
                .thenReturn(Optional.of(bronzeLeague));

        step.restoreExcludeRankingWriter().write(new Chunk<>(member));

        verify(bronzeLeague).increaseCurrentMembers();
        verify(rankingRepository).saveAll(anyList());
    }
}
