package com.studioedge.focus_to_levelup_server.domain.ranking.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dto.RankingResponse;
import com.studioedge.focus_to_levelup_server.domain.ranking.service.RankingService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingController {
    private final RankingService rankingService;
    @PostMapping("/v1/ranking")
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
    public ResponseEntity<CommonResponse<Page<RankingResponse>>> getRankingList(
            @AuthenticationPrincipal Member member,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return HttpResponseUtil.ok(rankingService.getRankingList(member, PageRequest.of(page, size)));
    }
}
