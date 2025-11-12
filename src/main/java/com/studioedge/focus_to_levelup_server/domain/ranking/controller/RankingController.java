package com.studioedge.focus_to_levelup_server.domain.ranking.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dto.RankingResponse;
import com.studioedge.focus_to_levelup_server.domain.ranking.service.RankingService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingController {
    private final RankingService rankingService;
    @PostMapping("/v1/rankings")
    @Operation(summary = "랭킹 조회(유저가 속한 리그의)", description = """
            ### 기능
            - 현재 로그인한 유저가 속한 시즌(Season)과 리그(League)의 랭킹 목록을 페이징하여 조회합니다.
            - 랭킹은 레벨(currentLevel)과 경험치(currentExp) 순서로 정렬됩니다.
            - 응답의 `isMe` 필드를 통해 랭킹 목록에서 본인을 식별할 수 있습니다.

            ### 요청
            - `page`: [쿼리 파라미터] 조회할 페이지 (default: 0)
            - `size`: [쿼리 파라미터] 페이지당 항목 수 (default: 20)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "랭킹 조회 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "랭킹을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Page<RankingResponse>>> getRankingList(
            @AuthenticationPrincipal Member member,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return HttpResponseUtil.ok(rankingService.getRankingList(member, PageRequest.of(page, size)));
    }
}
