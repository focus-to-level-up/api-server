package com.studioedge.focus_to_levelup_server.domain.member.controller;

import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.service.MemberService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 완료"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효성 검사에서 실패했습니다."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 닉네임입니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> completeSignUp(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid CompleteSignUpRequest request
    ) {
        memberService.completeSignUp(member, request);
        return HttpResponseUtil.created(null);
    }

    @PostMapping("/v1/member/report/{id}")
    @Operation(summary = "유저 신고하기", description = """
            ### 기능
            - 특정 유저를 신고 사유와 함께 신고합니다.
            - 신고, 피신고자, 신고내용을 기록합니다.
            
            ### 요청
            - `{id}`: 신고당하는 유저(경로변수)
            - `reportType`: [필수] 신고유형 (IMPROPER_NICKNAME, IMPROPER_MESSAGE)
            - `reason`: 신고 사유
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고당하는 유저를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> reportMember(
            @AuthenticationPrincipal Member member,
            @PathVariable(name = "id") Long memberId,
            @RequestBody @Valid ReportMemberRequest request
    ) {
        memberService.reportMember(member, memberId, request);
        return HttpResponseUtil.ok(null);
    }

    @GetMapping("/v1/member/profile/asset")
    @Operation(summary = "유저 프로필 에셋 조회", description = """
            ### 기능
            - 현재 로그인한 유저가 소유한 모든 프로필 에셋(이미지, 테두리) 목록을 페이징하여 조회합니다.
            
            ### 요청
            - `page`: 조회할 페이지 번호 (default: 0)
            - `size`: 페이지당 아이템 수 (default: 10)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProfileAssetResponse.class))
            )
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
            ### 기능
            - 특정 `memberId`를 가진 유저의 프로필을 조회합니다.
            
            ### 요청
            - `id`: 조회할 유저의 pk. ('유저 리스트 조회'에서 주어진 pk를 활용합니다.)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = GetProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<GetProfileResponse>> getMemberProfile(
            @PathVariable(name = "id") Long memberId
    ) {
        return HttpResponseUtil.ok(memberService.getMemberProfile(memberId));
    }



    @GetMapping("/v1/member/settings")
    @Operation(summary = "유저 세팅 조회", description = """
            ### 기능
            - 유저가 자신의 세팅상태를 조회합니다.
            - 아래 '유저 허용가능 앱 수정'에서 위 기능을 이용합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "세팅상태 조회 완료",
                    content = @Content(schema = @Schema(implementation = GetProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<MemberSettingDto>> getMemberSetting(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(memberService.getMemberSetting(member));
    }

    @PutMapping("/v1/member/profile")
    @Operation(summary = "유저 프로필 업데이트", description = """
            ### 기능
            - 특정 유저의 프로필을 업데이트 합니다.
            
            ### 요청
            - `profileImageId`: 장착할 MemberAsset pk(프로필 이미지)
            - `profileBorderId`: 장착할 MemberAsset pk(프로필 테두리)
            - `profileMessage`: 상태 메세지
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 프로필 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 에셋을 장착할 권한이 없습니다."
            )
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
            ### 기능
            - 유저의 닉네임을 업데이트합니다.
            - 유저는 변경일 기준으로 한달이 지난 이후에 닉네임을 업데이트 가능합니다.
            
            ### 요청
            - `nickname`: 업데이트할 닉네임
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 닉네임 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 닉네임은 변경일 기준으로 1달이 지나야 합니다."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 닉네임은 이미 존재합니다."
            )
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
            ### 기능
            - 유저의 카테고리를 업데이트합니다.
            - 유저는 변경일 기준으로 한달이 지난 이후에 카테고리를 업데이트 가능합니다.
            
            ### 요청
            - `categoryMain`: 업데이트할 메인 카테고리
            - `categorySub`: 업데이트할 서브 카테고리
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 카테고리 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 카테고리는 변경일 기준으로 1달이 지나야 합니다."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 카테고리의 상하관계가 일치하지 않습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> updateCategory(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        memberService.updateCategory(member, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/member/setting")
    @Operation(summary = "유저 세팅 업데이트", description = """
            ### 기능
            - 유저가 세팅(on/off)하는 값들을 반영합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "세팅 변경 성공"
            )
    })
    public ResponseEntity<CommonResponse<Void>> updateMemberSetting(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody MemberSettingDto request
    ) {
        memberService.updateMemberSetting(member, request);
        return HttpResponseUtil.updated(null);
    }

}
