package com.studioedge.focus_to_levelup_server.domain.system.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dto.request.CharacterSelectionRequest;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationCheckResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationRewardResponse;
import com.studioedge.focus_to_levelup_server.domain.system.service.PreRegistrationService;
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

@Tag(name = "Pre Registration", description = "사전예약 관련 API")
@RestController
@RequestMapping("/api/v1/pre-registration")
@RequiredArgsConstructor
public class PreRegistrationController {

    private final PreRegistrationService preRegistrationService;

    @PostMapping("/check")
    @Operation(summary = "사전예약 여부 확인", description = """
            ### 기능
            - 전화번호로 Firebase Firestore에서 사전예약 정보를 실시간 조회합니다.
            - 사전예약이 확인되면 전화번호를 PhoneNumberVerification 테이블에 저장합니다.
            - 이미 보상을 받은 경우에도 조회가 가능합니다.

            ### 요청
            - `phoneNumber`: [쿼리 파라미터] 사전예약 시 등록한 전화번호 (예: 01012345678)

            ### 응답
            - `isPreRegistered`: 사전예약 여부
            - `isRewardClaimed`: 보상 수령 여부
            - `registrationDate`: 사전예약 날짜 (yyyy-MM-dd)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사전예약 확인 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 다른 계정에서 사용 중인 전화번호입니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 정보가 존재하지 않습니다."
            )
    })
    public ResponseEntity<CommonResponse<PreRegistrationCheckResponse>> checkPreRegistration(
            @AuthenticationPrincipal Member member,
            @RequestParam String phoneNumber
    ) {
        PreRegistrationCheckResponse response = preRegistrationService.checkAndSavePhoneNumber(member.getId(), phoneNumber);
        return HttpResponseUtil.ok(response);
    }

    @PostMapping("/claim")
    @Operation(summary = "사전예약 보상 수령", description = """
            ### 기능
            - 사전예약 보상을 수령합니다.
            - 보상: 다이아 3000개 + 프리미엄 구독권 2주 + RARE 등급 캐릭터 1개
            - 모든 보상은 우편함으로 지급됩니다.

            ### 요청
            - `characterId`: [Request Body] 선택할 RARE 등급 캐릭터 ID

            ### 제약사항
            - 사전예약 확인(/check)을 먼저 진행해야 합니다.
            - RARE 등급 캐릭터만 선택 가능합니다.
            - 보상은 1회만 수령 가능합니다.

            ### 응답
            - `diamondAmount`: 지급된 다이아 수량
            - `subscriptionDays`: 프리미엄 구독 일수
            - `character`: 지급된 캐릭터 정보
            - `mailIds`: 생성된 우편함 ID 목록
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사전예약 보상 수령 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "RARE 등급이 아닌 캐릭터를 선택했거나 사전예약 정보가 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 정보 또는 캐릭터를 찾을 수 없습니다."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "이미 사전예약 보상을 받으셨습니다."
            )
    })
    public ResponseEntity<CommonResponse<PreRegistrationRewardResponse>> claimPreRegistrationReward(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody CharacterSelectionRequest request
    ) {
        PreRegistrationRewardResponse response = preRegistrationService.claimPreRegistrationReward(
                member.getId(),
                request.characterId()
        );
        return HttpResponseUtil.ok(response);
    }
}