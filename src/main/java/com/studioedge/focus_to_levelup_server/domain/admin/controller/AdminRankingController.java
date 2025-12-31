package com.studioedge.focus_to_levelup_server.domain.admin.controller;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminLeagueResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminRankingResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminAuthService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminLeagueService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminRankingService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - League & ranking", description = "관리자 리그 & 랭킹 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminRankingController {
    private final AdminAuthService adminAuthService;
    private final AdminLeagueService adminLeagueService;
    private final AdminRankingService adminRankingService;

    @GetMapping("/leagues")
    @Operation(summary = "리그 조회", description = "리그 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminLeagueResponse>> getLeagues(
            @AuthenticationPrincipal Member member
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminLeagueService.getLeagues());
    }

    @GetMapping("/leagues/{leagueId}/rankings")
    @Operation(summary = "리그별 랭킹 조회", description = "특정 리그의 랭킹 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminRankingResponse>>  getRankingsByLeague(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 리그 ID") @PathVariable Long leagueId
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminRankingService.getRankingsByLeague(leagueId));
    }

    @PostMapping("/rankings/{memberId}/exclude")
    @Operation(summary = "랭킹 제외 처리", description = """
            ### 기능
            - 특정 멤버를 랭킹에서 제외 처리합니다.
            - 관리자는 부정 집중, 규칙 위반 등의 사유로 멤버를 랭킹에서 제외할 수 있습니다.
            
            ### 요청 경로 변수
            - `memberId`: 랭킹에서 제외할 멤버의 ID
            
            ### 동작
            - 해당 멤버를 랭킹 시스템에서 제외 처리합니다.
            - 유저의 status를 `RANKING_BANNED`로 변경합니다.
            - 제외된 멤버는 일정 기간 후에 자동으로 랭킹에 복귀될 수 있습니다.
    """)
    public ResponseEntity<CommonResponse<AdminMemberResponse>> excludeMemberFromRanking(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "제외할 맴버 ID") @PathVariable Long memberId
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminRankingService.excludeMemberFromRanking(memberId));
    }
}
