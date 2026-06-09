package com.studioedge.admin.controller;

import com.studioedge.admin.dto.response.AdminLeagueResponse;
import com.studioedge.admin.dto.response.AdminMemberResponse;
import com.studioedge.admin.dto.response.AdminRankingResponse;
import com.studioedge.admin.service.AdminLeagueService;
import com.studioedge.admin.service.AdminRankingService;
import com.studioedge.response.CommonResponse;
import com.studioedge.admin.support.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - League & ranking", description = "관리자 리그 & 랭킹 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminRankingController {
    private final AdminLeagueService adminLeagueService;
    private final AdminRankingService adminRankingService;

    @GetMapping("/leagues")
    @Operation(summary = "리그 조회", description = "리그 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminLeagueResponse>> getLeagues(
    ) {
        return HttpResponseUtil.ok(adminLeagueService.getLeagues());
    }

    @GetMapping("/leagues/{leagueId}/rankings")
    @Operation(summary = "리그별 랭킹 조회", description = "특정 리그의 랭킹 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminRankingResponse>>  getRankingsByLeague(
            @Parameter(description = "조회할 리그 ID") @PathVariable Long leagueId
    ) {
        return HttpResponseUtil.ok(adminRankingService.getRankingsByLeague(leagueId));
    }

    @PostMapping("/rankings/{memberId}/exclude")
    @Operation(summary = "랭킹 제외 처리", description = "특정 회원을 랭킹에서 제외 처리합니다.")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> excludeMemberFromRanking(
            @Parameter(description = "제외할 맴버 ID") @PathVariable Long memberId
    ) {
        return HttpResponseUtil.ok(adminRankingService.excludeMemberFromRanking(memberId));
    }
}
