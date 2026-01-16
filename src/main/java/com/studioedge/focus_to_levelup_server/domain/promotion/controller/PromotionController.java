package com.studioedge.focus_to_levelup_server.domain.promotion.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.ReferralInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.RegisterCodeRequest;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.RouletteSpinResponse;
import com.studioedge.focus_to_levelup_server.domain.promotion.service.PromotionService;
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

@Tag(name = "Promotion", description = "친구 초대 및 룰렛 이벤트 API")
@RestController
@RequestMapping("/api/v1/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "내 레퍼럴 정보 조회", description = """
            ### 기능
            - 친구 초대 팝업 진입 시 호출합니다.
            - 나의 코드, 보유 룰렛 티켓 수, 코드 등록 여부를 반환합니다.
            """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReferralInfoResponse.class))
            )
    })
    public ResponseEntity<CommonResponse<ReferralInfoResponse>> getInfo(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(promotionService.getPromotionInfo(member.getId()));
    }

    @PostMapping("/code")
    @Operation(summary = "추천인 코드 등록", description = """
            ### 기능
            - 타인의 레퍼럴 코드를 입력하여 등록합니다.
            - 성공 시 **초대 코드 주인(상대방)**에게 룰렛 티켓 1장이 즉시 지급됩니다.
            
            ### 제약 사항
            - 한 유저는 평생 **단 한 번**만 코드를 등록할 수 있습니다.
            - 본인의 코드는 등록할 수 없습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공 (상대방에게 티켓 지급됨)"),
            @ApiResponse(responseCode = "400", description = "본인 코드를 입력했거나, 유효하지 않은 코드입니다."),
            @ApiResponse(responseCode = "400", description = "이미 코드를 등록한 유저입니다.")
    })
    public ResponseEntity<CommonResponse<Void>> registerCode(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid RegisterCodeRequest request
    ) {
        promotionService.registerCode(member.getId(), request);
        return HttpResponseUtil.ok(null);
    }

    @PostMapping("/roulette")
    @Operation(summary = "룰렛 돌리기", description = """
            ### 기능
            - 룰렛 티켓 1장을 소모하여 룰렛을 돌리고 보상을 획득합니다.
            - 획득한 보상은 **우편함(Mail)**으로 발송됩니다.
            """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "룰렛 실행 성공",
                    content = @Content(schema = @Schema(implementation = RouletteSpinResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "티켓이 부족합니다.")
    })
    public ResponseEntity<CommonResponse<RouletteSpinResponse>> spinRoulette(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(promotionService.spinRoulette(member.getId()));
    }
}
