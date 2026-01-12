package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminMemberStatsResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminDailyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
import lombok.RequiredArgsConstructor;
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
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;

    /**
     * 회원 검색 (ID 또는 닉네임 부분 일치)
     */
    public List<AdminMemberResponse> searchMembers(String type, String keyword) {
        List<Member> members;

        if ("ID".equalsIgnoreCase(type)) {
            try {
                Long memberId = Long.parseLong(keyword);
                members = memberRepository.findById(memberId)
                        .map(List::of)
                        .orElse(List.of());
            } catch (NumberFormatException e) {
                return List.of();
            }
        } else if (type.equals("STATUS")) {
            try {
                MemberStatus status = MemberStatus.valueOf(keyword.toUpperCase()); // 대소문자 무시 처리
                members = memberRepository.findAllByStatus(status);
            } catch (IllegalArgumentException e) {
                return List.of(); // 잘못된 상태 값이면 빈 리스트 반환
            }
        } else {
            members = memberRepository.findByNicknameContaining(keyword);
        }

        return members.stream()
                .map(m -> AdminMemberResponse.from(m, m.getMemberInfo()))
                .toList();
    }

    /**
     * 회원 통계 조회 (기간 지정)
     */
    public AdminMemberStatsResponse getMemberStats(Long memberId, LocalDate startDate, LocalDate endDate) {
        // 1. 전체 누적 평균 (변경 없음)
        Double avgFocusTime = dailyGoalRepository.getAverageFocusTimeByMemberId(memberId);
        Double avgMaxConsecutiveTime = dailyGoalRepository.getAverageMaxConsecutiveFocusTimeByMemberId(memberId);

        // 2. DB에서 해당 기간 데이터 조회
        List<AdminDailyStatResponse> dbStats = dailyGoalRepository.findDailyStatsByMemberIdAndDateRange(memberId, startDate, endDate);

        // 3. 빈 날짜 채우기 (Map으로 변환하여 빠른 조회)
        Map<LocalDate, AdminDailyStatResponse> statMap = dbStats.stream()
                .collect(Collectors.toMap(AdminDailyStatResponse::date, Function.identity()));

        List<AdminDailyStatResponse> resultStats = new ArrayList<>();

        // startDate부터 endDate까지 하루씩 증가하며 리스트 생성
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (statMap.containsKey(date)) {
                resultStats.add(statMap.get(date));
            } else {
                // 데이터가 없는 날은 0으로 채움
                resultStats.add(new AdminDailyStatResponse(date, 0, 0));
            }
        }

        return AdminMemberStatsResponse.of(avgFocusTime, avgMaxConsecutiveTime, resultStats);
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

        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId).orElse(null);
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

        memberInfo.updateSchoolByAdmin(school, schoolAddress);

        return AdminMemberResponse.from(member, memberInfo);
    }

    /**
     * 맴버 ACTIVE 상태로 복구
     */
    @Transactional
    public void restoreMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberSetting memberSetting = member.getMemberSetting();
        Tier tier = memberSetting.getBannedTier() == null ? Tier.BRONZE : memberSetting.getBannedTier();
        League league = leagueRepository.findSmallestLeagueForCategoryAndTier(
                member.getMemberInfo().getCategoryMain(), tier
        ).orElseThrow(LeagueNotFoundException::new);

        member.reactivate();
        memberSetting.clearRankingWarning();
        league.increaseCurrentMembers();
        rankingRepository.save(
                Ranking.builder()
                        .league(league)
                        .tier(league.getTier())
                        .member(member)
                        .build()
        );
    }
}
