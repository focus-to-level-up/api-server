package com.studioedge.focus_to_levelup_server.domain.member.controller;

import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.service.MemberService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/v1/member/signUp")
    @Operation(summary = "유저 가입(추가 정보 입력)", description = """
            ### 기능
            - 소셜로그인 직후, 추가정보(닉네임, 나이, 성별, 카테고리, 학교)를 입력받아 회원가입을 완료합니다.
            
            ### 요청
            - `age`: [필수] 나이(8 ~ 100)
            - `gender`: [필수] 성별(MALE, FEMALE)
            - `categoryMain`: [필수] 메인 카테고리
                - 종류: `ELEMENTARY_SCHOOL`, `MIDDLE_SCHOOL`, `HIGH_SCHOOL`, `ADULT`
            - `categorySub`: [필수] 서브 카테고리
                - 종류: 
                - `ELEMENTARY_1`~`ELEMENTARY_6`, `MIDDLE_1`~`MIDDLE_3`, `HIGH_1`~`HIGH_3`
                - `N_SU`, `UNIVERSITY_STUDENT`, `GRADUATE_STUDENT`, `EXAM_TAKER`
                - `PUBLIC_SERVANT`, `JOB_SEEKER`, `OFFICE_WORKER`
            - `schoolName`: [선택] 학교 이름 (단, categoryMain이 학생일 경우 필수)
            - `nickName`: [필수] 닉네임 (2-16자, 한/영/숫자, 특수문자/공백 불가)
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> completeSignUp(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid CompleteSignUpRequest request
    ) {
        memberService.completeSignUp(member, request);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @PostMapping("/v1/member/report/{id}")
    @Operation(summary = "유저 신고하기", description = """
            
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> reportMember(
            @AuthenticationPrincipal Member member,
            @PathVariable(name = "id") Long memberId,
            @RequestBody @Valid ReportMemberRequest request
    ) {
        memberService.reportMember(member, memberId, request);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @GetMapping("/v1/member/profile/asset")
    @Operation(summary = "유저 프로필 에셋 조회", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Page<ProfileAssetResponse>>> getMemberAsset(
            @AuthenticationPrincipal Member member,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return HttpResponseUtil.ok(memberService.getMemberAsset(member, PageRequest.of(page, size)));
    }

    @GetMapping("/v1/member/profile/{id}")
    @Operation(summary = "유저 프로필 단일 조회", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<GetProfileResponse>> getMemberProfile(
            @PathVariable(name = "id") Long memberId
    ) {
        return HttpResponseUtil.ok(memberService.getMemberProfile(memberId));
    }

    @PutMapping("/v1/member/profile")
    @Operation(summary = "유저 프로필 업데이트", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> updateProfile(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        memberService.updateMemberProfile(member, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/member/nickname")
    @Operation(summary = "유저 닉네임 업데이트", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> updateNickname(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateNicknameRequest request
    ) {
        memberService.updateNickname(member, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/member/category")
    @Operation(summary = "유저 카테고리 업데이트", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> updateCategory(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        memberService.updateCategory(member, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/member/alarm")
    @Operation(summary = "유저 알람 업데이트", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> updateAlarm(
            @AuthenticationPrincipal Member member
    ) {
        memberService.updateAlarmSetting(member);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/member/apps")
    @Operation(summary = "유저 허용가능앱 업데이트", description = """
            """
    )
    @ApiResponses({
            @ApiResponse()
    })
    public ResponseEntity<CommonResponse<Void>> updateAllowedApps(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody List<UpdateAllowedAppsRequest> requests
    ) {
        memberService.updateAllowedApps(member, requests);
        return HttpResponseUtil.updated(null);
    }
}
