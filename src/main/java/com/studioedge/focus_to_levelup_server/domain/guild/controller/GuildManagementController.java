package com.studioedge.focus_to_levelup_server.domain.guild.controller;

import com.studioedge.focus_to_levelup_server.domain.guild.dto.*;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildCommandService;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildMemberCommandService;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildPermissionService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 길드 관리자 기능 Controller
 * - 길드 생성, 수정, 삭제
 * - 길드원 관리 (강퇴, 역할 변경)
 */
@Tag(name = "Guild Management", description = "길드 관리 API")
@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildManagementController {

    private final GuildCommandService guildCommandService;
    private final GuildMemberCommandService guildMemberCommandService;
    private final GuildPermissionService guildPermissionService;

    @PostMapping
    @Operation(summary = "길드 생성", description = """
            ### 기능
            - 새로운 길드를 생성합니다.
            - 생성자는 자동으로 LEADER 역할을 부여받습니다.

            ### 요청 필드
            - `name`: [필수] 길드명 (최대 50자)
            - `description`: [필수] 길드 소개 (최대 500자)
            - `targetFocusTime`: [필수] 목표 집중 시간 (초 단위)
            - `isPublic`: [필수] 공개/비공개 여부
            - `password`: [선택] 비공개 길드 비밀번호 (비공개 시 필수)
            - `category`: [필수] 길드 카테고리
              - STUDENT: 학년
              - COLLEGE: 대학
              - EXAM_PREPARATION: 고시
              - WORKING: 업무시
              - NO_RESTRICTION: 제한 없음
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "길드 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    })
    public ResponseEntity<CommonResponse<GuildResponse>> createGuild(
            @Valid @RequestBody GuildCreateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        GuildResponse response = guildCommandService.createGuild(request, member.getId());
        return HttpResponseUtil.created(response);
    }

    @PutMapping("/{guildId}")
    @Operation(summary = "길드 정보 수정 (LEADER 전용)", description = """
            ### 기능
            - 길드의 기본 정보를 수정합니다.
            - LEADER 권한이 필요합니다.

            ### 수정 가능 필드 (모두 선택사항)
            - `name`: 길드명
            - `description`: 길드 소개
            - `isPublic`: 공개/비공개 여부
            - `password`: 비밀번호 (비공개로 변경 시 필수)
            - `targetFocusTime`: 목표 집중 시간

            ### 참고사항
            - 공개로 변경 시 비밀번호는 자동으로 제거됩니다.
            - 비공개로 변경 시 비밀번호를 함께 제공해야 합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 수정 성공"),
            @ApiResponse(responseCode = "403", description = "LEADER 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<GuildResponse>> updateGuild(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @Valid @RequestBody GuildUpdateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        // LEADER 권한 검증
        guildPermissionService.validateLeaderPermission(guildId, member.getId());

        GuildResponse response = guildCommandService.updateGuild(guildId, request, member.getId());
        return HttpResponseUtil.updated(response);
    }

    @DeleteMapping("/{guildId}")
    @Operation(summary = "길드 삭제 (LEADER 전용)", description = """
            ### 기능
            - 길드를 삭제합니다.
            - LEADER 권한이 필요합니다.
            - 길드원이 본인만 남았을 때만 삭제 가능합니다.

            ### 주의사항
            - 다른 길드원이 있는 경우 먼저 모두 탈퇴시켜야 합니다.
            - 삭제 후 복구가 불가능합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "길드원이 남아있어 삭제할 수 없습니다."),
            @ApiResponse(responseCode = "403", description = "LEADER 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> deleteGuild(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @AuthenticationPrincipal Member member
    ) {
        // LEADER 권한 검증
        guildPermissionService.validateLeaderPermission(guildId, member.getId());

        guildCommandService.deleteGuild(guildId, member.getId());
        return HttpResponseUtil.delete(null);
    }

    @DeleteMapping("/{guildId}/members/{memberId}")
    @Operation(summary = "길드원 강퇴 (LEADER/SUB_LEADER)", description = """
            ### 기능
            - 특정 길드원을 강퇴합니다.
            - LEADER 또는 SUB_LEADER 권한이 필요합니다.

            ### 처리 내용
            - 해당 길드원의 GuildMember 삭제
            - 길드 인원 감소
            - 강퇴된 멤버의 길드 부스트 비활성화 (있다면)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드원 강퇴 성공"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "길드 또는 길드원을 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> kickMember(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @Parameter(description = "강퇴할 회원 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal Member member
    ) {
        // LEADER 또는 SUB_LEADER 권한 검증
        guildPermissionService.validateLeaderOrSubLeaderPermission(guildId, member.getId());

        guildMemberCommandService.kickMember(guildId, memberId);
        return HttpResponseUtil.delete(null);
    }

    @PutMapping("/{guildId}/members/{memberId}/role")
    @Operation(summary = "길드원 역할 변경 (LEADER 전용)", description = """
            ### 기능
            - 길드원의 역할을 변경합니다.
            - LEADER 권한이 필요합니다.

            ### 역할 변경 규칙
            - LEADER → SUB_LEADER/MEMBER: 길드장 위임 (요청자는 SUB_LEADER로 강등)
            - SUB_LEADER ↔ MEMBER: 일반 역할 변경

            ### 요청 필드
            - `role`: [필수] 변경할 역할
              - LEADER: 길드장 위임
              - SUB_LEADER: 부길드장 임명
              - MEMBER: 일반 멤버로 변경
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @ApiResponse(responseCode = "403", description = "LEADER 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "길드 또는 길드원을 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<GuildMemberResponse>> updateMemberRole(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @Parameter(description = "역할을 변경할 회원 ID") @PathVariable Long memberId,
            @Valid @RequestBody GuildRoleUpdateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        // LEADER 권한 검증
        guildPermissionService.validateLeaderPermission(guildId, member.getId());
        GuildMemberResponse response = guildMemberCommandService.updateGuildMemberRole(guildId, memberId, request, member.getId());
        return HttpResponseUtil.updated(response);
    }
}
