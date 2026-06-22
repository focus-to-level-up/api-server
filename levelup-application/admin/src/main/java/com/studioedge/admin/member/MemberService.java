package com.studioedge.admin.member;

import com.studioedge.admin.member.dto.DailyStatResponse;
import com.studioedge.admin.member.dto.MemberResponse;
import com.studioedge.admin.member.dto.MemberStatsResponse;
import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.entity.MemberSetting;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.repository.RankingRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Ranking;
import com.studioedge.ranking.domain.RankingBanPolicy;
import com.studioedge.ranking.exception.LeagueNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;

    /**
     * 회원 검색 (ID 또는 닉네임 부분 일치)
     */
    public Page<MemberResponse> searchMembers(String type, String keyword, Pageable pageable) {
        Page<Member> members = switch (type.toUpperCase()) {
            case "ID" -> searchById(keyword, pageable);
            case "STATUS" -> searchByStatus(keyword, pageable);
            default -> memberRepository.findByNicknameContaining(keyword, pageable);
        };
        return members.map(member -> MemberResponse.from(member, member.getMemberInfo()));
    }

    /**
     * 회원 통계 조회 (기간 지정)
     */
    public MemberStatsResponse getMemberStats(Long memberId, LocalDate startDate, LocalDate endDate) {
        // 1. 전체 누적 평균 (변경 없음)
        Double avgFocusTime = dailyGoalRepository.getAverageFocusTimeByMemberId(memberId);
        Double avgMaxConsecutiveTime = dailyGoalRepository.getAverageMaxConsecutiveFocusTimeByMemberId(memberId);

        // 2. DB에서 해당 기간 데이터 조회
        List<DailyStatResponse> dbStats = dailyGoalRepository
                .findDailyStatsByMemberIdAndDateRange(memberId, startDate, endDate)
                .stream()
                .map(stat -> new DailyStatResponse(
                        stat.getDate(),
                        stat.getTotalFocusSeconds(),
                        stat.getMaxConsecutiveSeconds()
                ))
                .toList();

        // 3. 빈 날짜 채우기 (Map으로 변환하여 빠른 조회)
        Map<LocalDate, DailyStatResponse> statMap = dbStats.stream()
                .collect(Collectors.toMap(DailyStatResponse::date, Function.identity()));

        List<DailyStatResponse> resultStats = new ArrayList<>();

        // startDate부터 endDate까지 하루씩 증가하며 리스트 생성
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (statMap.containsKey(date)) {
                resultStats.add(statMap.get(date));
            } else {
                // 데이터가 없는 날은 0으로 채움
                resultStats.add(new DailyStatResponse(date, 0, 0));
            }
        }

        return MemberStatsResponse.of(avgFocusTime, avgMaxConsecutiveTime, resultStats);
    }

    /**
     * 회원 ID로 회원 조회
     */
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElse(null);
        return MemberResponse.from(member, memberInfo);
    }

    /**
     * 닉네임 변경 (1달 제한 무시)
     */
    @Transactional
    public MemberResponse updateNickname(Long memberId, String newNickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // Admin은 1달 제한 없이 변경 가능
        member.updateNickname(newNickname);

        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId).orElse(null);
        return MemberResponse.from(member, memberInfo);
    }

    /**
     * 상태 메시지 변경
     */
    @Transactional
    public MemberResponse updateProfileMessage(Long memberId, String newProfileMessage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(MemberNotFoundException::new);

        memberInfo.updateProfileMessage(newProfileMessage);

        return MemberResponse.from(member, memberInfo);
    }

    /**
     * 학교 정보 변경
     */
    @Transactional
    public MemberResponse updateSchool(Long memberId, String school, String schoolAddress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(MemberNotFoundException::new);

        memberInfo.updateSchoolByAdmin(school, schoolAddress);

        return MemberResponse.from(member, memberInfo);
    }

    /**
     * 맴버 ACTIVE 상태로 복구
     */
    @Transactional
    public void restoreMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        if (member.getStatus() != MemberStatus.RANKING_BANNED) {
            throw new InvalidMemberOperationException("랭킹 정지 회원만 복구할 수 있습니다.");
        }
        MemberSetting memberSetting = member.getMemberSetting();
        League league = leagueRepository.findSmallestBronzeLeagueForCategory(
                member.getMemberInfo().getCategoryMain()
        ).orElseThrow(LeagueNotFoundException::new);

        member.reactivate();
        memberSetting.clearRankingWarning();
        league.increaseCurrentMembers();
        rankingRepository.save(
                Ranking.builder()
                        .league(league)
                        .tier(RankingBanPolicy.restoreTier())
                        .member(member)
                        .build()
        );
    }

    private Page<Member> searchById(String keyword, Pageable pageable) {
        try {
            return memberRepository.findById(Long.parseLong(keyword))
                    .<Page<Member>>map(member -> new PageImpl<>(List.of(member), pageable, 1))
                    .orElseGet(() -> Page.empty(pageable));
        } catch (NumberFormatException exception) {
            return Page.empty(pageable);
        }
    }

    private Page<Member> searchByStatus(String keyword, Pageable pageable) {
        try {
            return memberRepository.findAllByStatus(MemberStatus.valueOf(keyword.toUpperCase()), pageable);
        } catch (IllegalArgumentException exception) {
            return Page.empty(pageable);
        }
    }
}
