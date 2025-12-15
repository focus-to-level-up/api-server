package com.studioedge.focus_to_levelup_server.domain.guild.controller;

import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildListResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildSearchResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildMemberQueryService;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildQueryService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 길드 조회 및 검색 Controller
 */
@Tag(name = "Guild", description = "길드 API")
@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildController {

    private final GuildQueryService guildQueryService;
    private final GuildMemberQueryService guildMemberQueryService;

    @GetMapping
    @Operation(summary = "길드 목록 조회", description = """
            ### 기능
            - 길드 리스트를 페이징하여 조회합니다.
            - 정렬 기준을 선택할 수 있습니다.

            ### 쿼리 파라미터
            - `page`: 페이지 번호 (default: 0)
            - `size`: 페이지 크기 (default: 20)
            - `sortBy`: 정렬 기준 (default: currentMembers)
              - currentMembers: 참여 인원
              - targetFocusTime: 목표 집중 시간
              - averageFocusTime: 평균 집중 시간
              - lastWeekDiamondReward: 지난주 다이아 보상
            - `order`: 정렬 순서 (default: desc)
              - asc: 오름차순
              - desc: 내림차순
            - `excludeFull`: 정원이 찬 길드 제외 여부 (default: false)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 목록 조회 성공")
    })
    public ResponseEntity<CommonResponse<GuildListResponse>> getGuilds(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "currentMembers") String sortBy,
            @Parameter(description = "정렬 순서 (asc/desc)") @RequestParam(defaultValue = "desc") String order,
            @Parameter(description = "정원이 찬 길드 제외 여부") @RequestParam(defaultValue = "false") boolean excludeFull
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        GuildListResponse response = guildQueryService.getAllGuilds(pageable, excludeFull);
        return HttpResponseUtil.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "길드 검색", description = """
            ### 기능
            - 키워드와 카테고리로 길드를 검색합니다.
            - 검색 결과에 가입 가능 여부가 포함됩니다.

            ### 쿼리 파라미터
            - `keyword`: 길드명 검색 키워드 (Optional)
            - `category`: 길드 카테고리 필터 (Optional)
              - STUDENT: 학년 (초/중/고등학생)
              - COLLEGE: 대학 (대학생 + 대학원생)
              - EXAM_PREPARATION: 고시
              - WORKING: 업무시
              - NO_RESTRICTION: 제한 없음
            - `page`, `size`, `sortBy`, `order`: 페이징 및 정렬 옵션
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 검색 성공")
    })
    public ResponseEntity<CommonResponse<GuildSearchResponse>> searchGuilds(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "길드 카테고리") @RequestParam(required = false) CategorySubType category,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "currentMembers") String sortBy,
            @Parameter(description = "정렬 순서") @RequestParam(defaultValue = "desc") String order
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        GuildSearchResponse response;
        if (category != null && keyword != null) {
            response = guildQueryService.searchGuildsByCategory(keyword, category, pageable);
        } else if (keyword != null) {
            response = guildQueryService.searchGuilds(keyword, pageable);
        } else if (category != null) {
            // 카테고리만 있는 경우
            GuildListResponse listResponse = guildQueryService.getGuildsByCategory(category, pageable, false);
            response = new GuildSearchResponse(
                    listResponse.guilds(),
                    listResponse.totalPages(),
                    listResponse.totalElements(),
                    listResponse.currentPage(),
                    null,
                    category
            );
        } else {
            // 둘 다 없으면 전체 조회
            GuildListResponse listResponse = guildQueryService.getAllGuilds(pageable, false);
            response = new GuildSearchResponse(
                    listResponse.guilds(),
                    listResponse.totalPages(),
                    listResponse.totalElements(),
                    listResponse.currentPage(),
                    null,
                    null
            );
        }

        return HttpResponseUtil.ok(response);
    }

    @GetMapping("/{guildId}")
    @Operation(summary = "길드 상세 조회", description = """
            ### 기능
            - 특정 길드의 상세 정보를 조회합니다.
            - 현재 유저의 가입 상태 및 역할이 포함됩니다.

            ### 응답 정보
            - 길드 기본 정보 (이름, 소개, 목표 시간 등)
            - 현재 인원 및 최대 인원
            - 카테고리, 공개 여부
            - 가입 가능 여부 (isJoinable)
            - 현재 유저의 가입 상태 (memberStatus)
              - isMember: 가입 여부
              - role: 길드 내 역할 (LEADER/SUB_LEADER/MEMBER)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 조회 성공"),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<GuildResponse>> getGuild(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @AuthenticationPrincipal Member member
    ) {
        GuildResponse response = guildQueryService.getGuildById(guildId, member.getId());
        return HttpResponseUtil.ok(response);
    }

    @GetMapping("/{guildId}/members")
    @Operation(summary = "길드원 조회", description = """
            ### 기능
            - 특정 길드의 모든 길드원을 조회합니다.
            - 주간 집중 시간 기준 내림차순으로 정렬됩니다.
            - 랭킹 정보가 포함됩니다.

            ### 응답 정보
            - memberId: 회원 ID
            - nickname: 닉네임
            - profileImageUrl: 프로필 이미지 URL
            - role: 길드 내 역할
            - weeklyFocusTime: 주간 집중 시간 (초 단위)
            - isBoosted: 부스트 사용 여부
            - ranking: 길드 내 랭킹 (1위부터)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드원 조회 성공"),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<GuildMemberResponse.GuildMemberListResponse>> getGuildMembers(
            @Parameter(description = "길드 ID") @PathVariable Long guildId
    ) {
        GuildMemberResponse.GuildMemberListResponse response =
                guildMemberQueryService.getGuildMembers(guildId);
        return HttpResponseUtil.ok(response);
    }
}
