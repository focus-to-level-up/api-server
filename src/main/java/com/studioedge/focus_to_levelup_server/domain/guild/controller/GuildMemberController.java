package com.studioedge.focus_to_levelup_server.domain.guild.controller;

import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildListResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildBoostService;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildCommandService;
import com.studioedge.focus_to_levelup_server.domain.guild.service.GuildMemberQueryService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.fcm.NotificationService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 길드 가입 및 멤버 관리 Controller
 * - 길드 가입/탈퇴
 * - 길드 부스트
 * - 길드원 집중 요청
 */
@Tag(name = "Guild Member", description = "길드 멤버 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GuildMemberController {

    private final GuildCommandService guildCommandService;
    private final GuildMemberQueryService guildMemberQueryService;
    private final GuildBoostService guildBoostService;
    private final NotificationService notificationService;

    @PostMapping("/guilds/{guildId}/join")
    @Operation(summary = "길드 가입", description = """
            ### 기능
            - 특정 길드에 가입합니다.
            - 비공개 길드는 비밀번호가 필요합니다.
            - 사용자당 최대 10개 길드까지 가입 가능합니다.

            ### 처리 절차
            1. 정원 확인 (현재 인원 < 20명)
            2. 중복 가입 확인
            3. 사용자 길드 가입 수 확인 (< 10개)
            4. 비공개 길드 시 비밀번호 검증
            5. GuildMember 생성 (role=MEMBER)
            6. Guild.currentMembers 증가

            ### 쿼리 파라미터
            - `password`: 비공개 길드 비밀번호 (비공개 길드만 필수)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "길드 가입 성공"),
            @ApiResponse(responseCode = "400", description = "정원 초과 또는 중복 가입 또는 비밀번호 불일치 또는 최대 가입 수 초과 (10개)"),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<GuildResponse>> joinGuild(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @Parameter(description = "비공개 길드 비밀번호") @RequestParam(required = false) String password,
            @AuthenticationPrincipal Member member
    ) {
        GuildResponse response = guildCommandService.joinGuild(guildId, password, member.getId());
        return HttpResponseUtil.created(response);
    }

    @DeleteMapping("/guilds/{guildId}/leave")
    @Operation(summary = "길드 탈퇴", description = """
            ### 기능
            - 현재 길드에서 탈퇴합니다.
            - 길드장은 먼저 권한을 위임해야 탈퇴할 수 있습니다.

            ### 처리 절차
            1. GuildMember 조회 및 삭제
            2. Guild.currentMembers 감소
            3. 길드장이 혼자인 경우 길드 자동 삭제

            ### 제한사항
            - 길드장(LEADER)은 다른 멤버가 있으면 탈퇴 불가
            - 먼저 다른 멤버에게 권한을 위임해야 함
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "길드장은 권한 위임 후 탈퇴 가능"),
            @ApiResponse(responseCode = "404", description = "길드 또는 길드원을 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> leaveGuild(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @AuthenticationPrincipal Member member
    ) {
        guildCommandService.leaveGuild(guildId, member.getId());
        return HttpResponseUtil.delete(null);
    }

    @PostMapping("/guilds/{guildId}/boost")
    @Operation(summary = "길드 부스트 활성화 (프리미엄 전용)", description = """
            ### 기능
            - 길드에 부스트를 활성화합니다.
            - 프리미엄 구독권이 필요합니다.

            ### 부스트 제한
            - 유저당 최대 2개 길드 부스트 가능
            - 길드당 최대 10명까지 부스트 가능

            ### 부스트 효과
            - 부스트 사용자 1명당 길드 주간 보상 +50 다이아
            - 길드원 전체가 혜택을 받음

            ### 처리 절차
            1. 프리미엄 구독권 확인
            2. 유저 부스트 개수 확인 (< 2)
            3. 길드 부스트 개수 확인 (< 10)
            4. GuildBoost 생성
            5. GuildMember.isBoosted = true
            6. Subscription.activatedGuildId 업데이트
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "길드 부스트 활성화 성공"),
            @ApiResponse(responseCode = "400", description = "부스트 한도 초과"),
            @ApiResponse(responseCode = "403", description = "프리미엄 구독권이 없습니다."),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> activateGuildBoost(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @AuthenticationPrincipal Member member
    ) {
        guildBoostService.activateBoost(guildId, member.getId());
        return HttpResponseUtil.created(null);
    }

    @DeleteMapping("/guilds/{guildId}/boost")
    @Operation(summary = "길드 부스트 비활성화", description = """
            ### 기능
            - 활성화된 길드 부스트를 비활성화합니다.

            ### 처리 절차
            1. GuildBoost.isActive = false
            2. GuildMember.isBoosted = false
            3. Subscription.activatedGuildId = null
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "길드 부스트 비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "활성화된 부스트를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> deactivateGuildBoost(
            @Parameter(description = "길드 ID") @PathVariable Long guildId,
            @AuthenticationPrincipal Member member
    ) {
        guildBoostService.deactivateBoost(guildId, member.getId());
        return HttpResponseUtil.delete(null);
    }

    @GetMapping("/members/me/guilds")
    @Operation(summary = "내가 가입한 길드 목록 조회", description = """
            ### 기능
            - 현재 로그인한 유저가 가입한 모든 길드를 조회합니다.

            ### 응답 정보
            - 길드 기본 정보 (id, name, description, currentMembers, maxMembers 등)
            - 가입 가능 여부 (isJoinable)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 길드 목록 조회 성공")
    })
    public ResponseEntity<CommonResponse<GuildListResponse>> getMyGuilds(
            @AuthenticationPrincipal Member member
    ) {
        GuildListResponse response = guildMemberQueryService.getMyGuilds(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @GetMapping("/members/me/guilds/boosted")
    @Operation(summary = "내가 부스트한 길드 목록 조회", description = """
            ### 기능
            - 현재 활성화된 부스트가 적용된 길드 목록을 조회합니다.

            ### 응답 정보
            - 부스트 중인 길드 정보
            - 최대 2개까지 표시됨 (유저당 부스트 한도)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부스트 길드 목록 조회 성공")
    })
    public ResponseEntity<CommonResponse<GuildListResponse>> getMyBoostedGuilds(
            @AuthenticationPrincipal Member member
    ) {
        GuildListResponse response = guildBoostService.getMyBoostedGuilds(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @PostMapping("/guilds/{guildId}/focus-request")
    @Operation(summary = "길드 집중 요청 (FCM 푸시 알림)", description = """
            ### 기능
            - 길드원들에게 집중 요청 푸시 알림을 전송합니다.
            - 요청자를 제외한 모든 길드원에게 알림이 발송됩니다.
            - FCM 토큰이 등록된 유저에게만 알림이 전송됩니다.

            ### 알림 내용
            - 메시지: "{요청자 닉네임}이 집중을 요청했어요!"
            - 클릭 시: 타이머 화면으로 이동
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "집중 요청 알림 발송 성공"),
            @ApiResponse(responseCode = "404", description = "길드를 찾을 수 없습니다.")
    })
    public ResponseEntity<CommonResponse<Void>> requestFocus(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "길드 ID") @PathVariable Long guildId
    ) {
        guildCommandService.requestFocus(guildId, member.getId(), member.getNickname());
        return HttpResponseUtil.ok(null);
    }
}
