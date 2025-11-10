package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveAllowedAppRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.service.AllowedAppService;
import com.studioedge.focus_to_levelup_server.domain.member.dto.AllowedAppsDto;
import com.studioedge.focus_to_levelup_server.domain.member.dto.GetProfileResponse;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AllowedApp")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AllowedAppController {
    private final AllowedAppService allowedAppService;
    @GetMapping("/v1/apps")
    @Operation(summary = "유저 허용가능 앱 조회", description = """
            ### 기능
            - 유저가 자신의 허용가능 앱을 조회합니다.
            - 아래 '유저 허용가능 앱 수정'에서 위 기능을 이용합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "허용가능 앱 조회 완료",
                    content = @Content(schema = @Schema(implementation = GetProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<AllowedAppsDto>> getAllowedApps(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(allowedAppService.getAllowedApps(member));
    }
    @PutMapping("/v1/apps")
    @Operation(summary = "허용가능 앱 생성 및 수정(덮어쓰기)", description = """
            ### 기능
            - 유저의 집중 시 허용가능한 앱 목록을 클라이언트가 보낸 목록으로 설정합니다.
            - 빈 리스트를 보낼 경우, 모든 허용가능한 앱이 삭제됩니다.
            
            ### 요청
            - `appIdentifier`: 앱 식별자(패키지명/번들ID) (-> 해당 정보 외에도 추가적인 정보를 저장해야한다면 말씀해주세요!)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "허용가능앱 업데이트 성공"
            )
    })
    public ResponseEntity<CommonResponse<Void>> updateAllowedApps(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody AllowedAppsDto requests
    ) {
        allowedAppService.updateAllowedApps(member, requests);
        return HttpResponseUtil.updated(null);
    }

    /**
     * 과목 집중중, 앱 사용시간 저장
     * */
    @PostMapping("/v1/app")
    @Operation(summary = "허용가능 앱 사용시간 저장", description = """
            ### 기능
            - 학습 중 '허용 앱'을 사용한 시간을 저장합니다.
            - 해당 사용앱과 총 사용한 허용 앱 총 사용시간에 저장합니다.
            
            ### 요청
            - `appIdentifier`: [필수] 사용한 앱 식별자 (패키지명/번들ID)
            - `durationMinutes`: [필수] 사용한 시간(분)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "허용 앱 사용 시간 저장 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO 유효성 검사 실패"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "허용 앱 목록에 해당 앱이 등록되어 있지 않습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> saveAllowedAppTime(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody SaveAllowedAppRequest request
    ) {
        allowedAppService.saveAllowedAppTime(member, request);
        return HttpResponseUtil.ok(null);
    }
}
