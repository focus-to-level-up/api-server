package com.studioedge.focus_to_levelup_server.domain.advertisement.controller;

import com.studioedge.focus_to_levelup_server.domain.advertisement.dto.AdvertisementResponse;
import com.studioedge.focus_to_levelup_server.domain.advertisement.service.AdvertisementService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdvertiseController {

    private final AdvertisementService advertisementService;

    @Operation(
            summary = "광고 목록 조회",
            description = """
                    인증된 회원 기준으로 노출/대상 광고 목록을 조회합니다. 
                    반환되는 DTO는 광고 정보(이미지, 타이틀, 링크 등)를 포함합니다.
                    
                    유저의 카테고리별로 조회해주며, 광고 선택 로직은 서버에서 담당합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공 - 광고 목록 반환",
                    content = @Content(schema = @Schema(implementation = AdvertisementResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/v1/advertisements")
    public ResponseEntity<CommonResponse<AdvertisementResponse>> getAdvertisement(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(advertisementService.getAdvertisement(member));
    }

    @Operation(
            summary = "광고 클릭 처리",
            description = "회원이 특정 광고를 클릭했을 때 클릭 수 증가와 관련 처리를 수행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공 - 클릭 기록 처리 완료"),
            @ApiResponse(responseCode = "404", description = "광고를 찾을 수 없음")
    })
    @PostMapping("/v1/advertisement/{advertisementId}/click")
    public ResponseEntity<CommonResponse<Void>> clickAdvertisement(
            @PathVariable Long advertisementId
    ) {
        advertisementService.clickAdvertisement(advertisementId);
        return HttpResponseUtil.created(null);
    }
}
