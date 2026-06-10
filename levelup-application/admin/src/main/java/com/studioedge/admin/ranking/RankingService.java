package com.studioedge.admin.ranking;

import com.studioedge.admin.member.InvalidMemberOperationException;
import com.studioedge.admin.member.dto.MemberResponse;
import com.studioedge.admin.ranking.dto.RankingResponse;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Ranking;
import com.studioedge.ranking.exception.LeagueNotFoundException;
import com.studioedge.mail.repository.MailRepository;
import com.studioedge.mail.entity.Mail;
import com.studioedge.mail.enums.MailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;
    private final MemberRepository memberRepository;
    private final MailRepository mailRepository;

    public RankingResponse getRankingsByLeague(Long leagueId, String keyword) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(LeagueNotFoundException::new);
        List<Ranking> rankings = rankingRepository.findAllBySortedLeague(league);
        List<Ranking> filteredRankings = filterRankings(rankings, keyword);
        return RankingResponse.of(league, filteredRankings, rankings.size());
    }

    @Transactional
    public MemberResponse excludeMemberFromRanking(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new InvalidMemberOperationException("활성 회원만 랭킹에서 정지할 수 있습니다.");
        }
        Ranking ranking = rankingRepository.findByMemberId(member.getId())
                .orElseThrow(MemberNotFoundException::new);

        // 비즈니스 로직: 상태 변경 (Member 엔티티 내부에 메서드 권장)
        member.banRanking();
        member.getMemberSetting().banRanking(ranking.getTier());

        ranking.getLeague().decreaseCurrentMembers();
        rankingRepository.deleteByMemberId(member.getId());

        mailRepository.save(
                Mail.builder()
                        .receiver(member)
                        .senderName("운영자")
                        .type(MailType.WARNING)
                        .title("랭킹 정지")
                        .description("비정상적인 이용으로 랭킹이용이 정지되었습니다")
                        .popupTitle("랭킹 정지")
                        .popupContent("비정상적인 이용으로 랭킹이용이 정지되었습니다\n1주 후 브론즈 리그부터 랭킹에 다시 참여하실 수 있습니다.")
                        .expiredAt(LocalDate.now().plusDays(7))
                        .build()
        );

        return MemberResponse.from(member, member.getMemberInfo());
    }

    private List<Ranking> filterRankings(List<Ranking> rankings, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return rankings;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        return rankings.stream()
                .filter(ranking -> matchesKeyword(ranking.getMember(), normalizedKeyword))
                .toList();
    }

    private boolean matchesKeyword(Member member, String keyword) {
        return String.valueOf(member.getId()).contains(keyword)
                || member.getNickname().toLowerCase().contains(keyword);
    }
}
