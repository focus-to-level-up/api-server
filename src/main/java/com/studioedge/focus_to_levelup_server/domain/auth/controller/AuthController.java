package com.studioedge.focus_to_levelup_server.domain.auth.controller;

import com.studioedge.focus_to_levelup_server.domain.auth.dto.*;
import com.studioedge.focus_to_levelup_server.domain.auth.service.AuthService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/{socialType}")
    @Operation(
            summary = "소셜 로그인",
            description = "Identity Token을 검증하고 JWT 토큰을 발급합니다. 등록되지 않은 사용자는 401 에러를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "등록되지 않은 사용자 (회원가입 필요)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 Identity Token"
            )
    })
    public ResponseEntity<CommonResponse<LoginResponse>> signIn(
            @Parameter(description = "소셜 로그인 타입 (apple, kakao, naver, google)", example = "apple")
            @PathVariable String socialType,
            @Valid @RequestBody LoginRequest request
    ) {
        SocialType type = SocialType.valueOf(socialType.toUpperCase());
        LoginResponse response = authService.signIn(request, type);
        return HttpResponseUtil.ok(response);
    }

    @PostMapping("/signup/{socialType}")
    @Operation(
            summary = "소셜 회원가입",
            description = "Identity Token과 Authorization Code를 검증하고 회원가입을 진행합니다. 기존 회원인 경우 정보를 업데이트합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = SignUpResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 Token 또는 Authorization Code"
            )
    })
    public ResponseEntity<CommonResponse<SignUpResponse>> signUp(
            @Parameter(description = "소셜 로그인 타입 (apple, kakao, naver, google)", example = "apple")
            @PathVariable String socialType,
            @Valid @RequestBody SignUpRequest request
    ) {
        SocialType type = SocialType.valueOf(socialType.toUpperCase());
        SignUpResponse response = authService.signUp(request, type);
        return HttpResponseUtil.created(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 토큰 타입 (Access Token이 아닌 Refresh Token이 필요)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "만료되었거나 일치하지 않는 Refresh Token"
            )
    })
    public ResponseEntity<CommonResponse<TokenRefreshResponse>> refresh(
            @Parameter(description = "Refresh Token (Bearer 헤더에 포함)", required = true)
            @RequestHeader("Authorization") String authorization
    ) {
        String refreshToken = authorization.replace("Bearer ", "");
        TokenRefreshResponse response = authService.refresh(refreshToken);
        return HttpResponseUtil.ok(response);
    }

    @DeleteMapping("/resign/{socialType}")
    @Operation(
            summary = "회원탈퇴",
            description = "현재 로그인한 사용자의 회원탈퇴를 처리합니다. Apple의 경우 Token revoke도 함께 수행됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원탈퇴 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    public ResponseEntity<CommonResponse<Void>> resign(
            @Parameter(description = "소셜 로그인 타입 (apple, kakao, naver, google)", example = "apple")
            @PathVariable String socialType,
            @AuthenticationPrincipal Member member
    ) {
        SocialType type = SocialType.valueOf(socialType.toUpperCase());
        authService.resign(member.getId(), type);
        return HttpResponseUtil.ok(null);
    }
}
