package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.StartFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.StartFocusRequestV2;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.FocusModeImageResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.FocusService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Focus")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FocusController {

    private final FocusService focusService;

    @PutMapping("/v1/focus")
    @Operation(summary = "유저 집중 시작", description = """
            ### 기능
            - 유저가 과목에서 공부를 시작합니다.
            - 집중중인 상태로 만들어줍니다.
            - 종료 상태는 '과목 시간 저정'단계에서 수행됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "집중 상태변경 성공"
            )
    })
    public ResponseEntity<CommonResponse<Void>> startFocusV1(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody StartFocusRequest request
    ) {
        focusService.startFocusV1(member, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v2/focus")
    @Operation(summary = "유저 집중 시작 ver2", description = """
            ### 기능
            - 유저가 과목에서 공부를 시작합니다.
            - 집중중인 상태로 만들어줍니다.
            - 종료 상태는 '과목 시간 저정'단계에서 수행됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "집중 상태변경 성공"
            )
    })
    public ResponseEntity<CommonResponse<Void>> startFocusV2(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody StartFocusRequestV2 request
    ) {
        focusService.startFocusV2(member, request);
        return HttpResponseUtil.updated(null);
    }



    /**
     * @TODO:
     * 현재는 집중모드 배경이 하나로 정해져있음.
     * 향후 캐릭터 or 레벨마다 달라질 여부가 있음. 리팩토링 예정
     * */
    @GetMapping("/v1/focus")
    @Operation(summary = "집중모드 에셋 조회", description = """
            ### 기능
            - 집중모드에 진입했을 떄 필요한 이미지 객체들을 조회합니다.
            - 등장하는 몬스터, 배경화면 등을 조회합니다.
            - 앱 초기에는 정해진 몇몇 몬스터와 기본 배경이지만, 향후 디벨롭하면서 다양한 몬스터, 배경이 등장하기에 사용합니다.
            """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "집중 에셋 전달"
            )
    })
    public ResponseEntity<CommonResponse<FocusModeImageResponse>> getFocusAnimation(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(focusService.getFocusAnimation(member));
    }
}
