package com.studioedge.admin.service;

import com.studioedge.admin.dto.response.AdminMemberResponse;
import com.studioedge.admin.dto.response.AdminRankingResponse;
import com.studioedge.admin.exception.InvalidAdminMemberOperationException;
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
public class AdminRankingService {
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;
    private final MemberRepository memberRepository;
    private final MailRepository mailRepository;

    public AdminRankingResponse getRankingsByLeague(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(LeagueNotFoundException::new);
        List<Ranking> rankings = rankingRepository.findAllBySortedLeague(league);
        return AdminRankingResponse.of(league, rankings);
    }

    @Transactional
    public AdminMemberResponse excludeMemberFromRanking(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new InvalidAdminMemberOperationException("활성 회원만 랭킹에서 정지할 수 있습니다.");
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
                        .popupContent("비정상적인 이용으로 랭킹이용이 정지되었습니다\n정상적인 형태로 이용을 하시면 1주 후에 랭킹에 다시 참여하실 수 있습니다.")
                        .expiredAt(LocalDate.now().plusDays(7))
                        .build()
        );

        return AdminMemberResponse.from(member, member.getMemberInfo());
    }
}
