package com.studioedge.focus_to_levelup_server.domain.event.controller;

import com.studioedge.focus_to_levelup_server.domain.event.dto.SchoolResponse;
import com.studioedge.focus_to_levelup_server.domain.event.service.SchoolService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
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

@Tag(name = "Event (School)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {
    private final SchoolService schoolService;
    @PostMapping("/v1/event/school")
    @Operation(summary = "학교 현재 랭킹 조회", description = """
            ### 기능
            - 학교별 누적 레벨(`totalLevel`)을 기준으로 학교 랭킹 목록을 페이징하여 조회합니다.
            - 랭킹은 `totalLevel`이 높은 순서대로 자동 정렬됩니다.
            - 응답의 `mySchool` 필드(true/false)를 통해, 현재 로그인한 유저의 소속 학교를 랭킹 목록에서 식별할 수 있습니다.

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
    public ResponseEntity<CommonResponse<Page<SchoolResponse>>> getRankingList(
            @AuthenticationPrincipal Member member,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return HttpResponseUtil.ok(schoolService.getRankingList(member, PageRequest.of(page, size)));
    }
}
