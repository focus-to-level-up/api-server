package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveAllowedAppRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SaveFocusService;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SubjectService;
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

import java.util.List;

@Tag(name = "Subject")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubjectController {
    private final SubjectService subjectService;
    private final SaveFocusService saveFocusService;
    /**
     * 과목 리스트 조회
     * */
    @GetMapping("/v1/subjects")
    @Operation(summary = "유저의 모든 과목 리스트 조회", description = """
            ### 기능
            - 현재 로그인한 유저가 생성한 모든 과목 리스트를 조회합니다.
            - 과목을 생성한 적이 없거나 비어있다면, 빈 리스트가 전달됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetSubjectResponse.class))
            )
    })
    public ResponseEntity<CommonResponse<List<GetSubjectResponse>>> getSubjectList(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(subjectService.getSubjectList(memberId));
    }

    /**
     * 과목 생성
     * */
    @PostMapping("/v1/subject")
    @Operation(summary = "과목 생성", description = """
            ### 기능
            - 새로운 과목을 생성합니다.
            
            ### 요청
            - `name`: [필수] 과목 이름
            - `color`: [필수] 헥스 코드 (e.g., `#FFFFFF`)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "과목 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO 유효성 검사 실패 (e.g., 헥스 코드 패턴 불일치)"
            )
    })
    public ResponseEntity<CommonResponse<Void>> createSubject(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        subjectService.createSubject(member, request);
        return HttpResponseUtil.created(null);
    }

    /**
     * 과목 공부시간 저장
     * */
    @PostMapping("/v1/subject/{subjectId}")
    @Operation(summary = "과목 학습 시간 저장 (세션 종료)", description = """
            ### 기능
            - 뽀모도로/일반 학습 세션 종료 후, 실제 학습 시간을 저장하고 보상을 정산받습니다.
            
            ### 개발 유의사항
            - 1분에 경험치 10, 골드 10이 지급됩니다. (레벨당 600 EXP)
            - `DailyGoal`의 `currentMinutes` 필드 누적
            - `Member`의 `currentExp`, `currentLevel` 업데이트 (레벨업 확인)
            - `MemberCharacter`의 친밀도 누적
            
            ### 요청
            - `subjectId`: [경로] 학습한 과목 PK
            - `focusMinutes`: [Body] [필수] 실제 학습한 시간(분)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "학습 시간 저장 및 보상 정산 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO 유효성 검사 실패 (e.g., 1분 미만)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목/유저/일일목표를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> saveFocus(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody SaveFocusRequest request
    ) {
        saveFocusService.saveFocus(memberId, subjectId, request);
        return HttpResponseUtil.ok(null);
    }

    /**
     * 과목 집중중, 앱 사용시간 저장
     * */
    @PostMapping("/v1/subject/app")
    @Operation(summary = "허용 앱 사용 시간 저장", description = """
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
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody SaveAllowedAppRequest request
    ) {
        subjectService.saveAllowedAppTime(memberId, request);
        return HttpResponseUtil.ok(null);
    }

    /**
     * 과목 수정
     * */
    @PutMapping("/v1/subject/{subjectId}")
    @Operation(summary = "과목 수정", description = """
            ### 기능
            - `subjectId`에 해당하는 과목의 이름과 색상을 수정합니다.
            
            ### 요청
            - `subjectId`: [경로] 수정할 과목 PK
            - `name`: [Body] [필수] 새 과목 이름
            - `color`: [Body] [필수] 새 헥스 코드 (e.g., `#FF0000`)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "과목 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO 유효성 검사 실패"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> updateSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        subjectService.updateSubject(memberId, subjectId, request);
        return HttpResponseUtil.updated(null);
    }

    /**
     * 과목 삭제
     * */
    @DeleteMapping("/v1/subject/{subjectId}")
    @Operation(summary = "과목 삭제", description = """
            ### 기능
            - `subjectId`에 해당하는 과목을 삭제합니다.
            - 과목 삭제 시, 하위에 종속된 Todo들도 함께 삭제됩니다.
            
            ### 요청
            - `subjectId`: [경로] 삭제할 과목 PK
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "과목 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> deleteSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.deleteSubject(memberId, subjectId);
        return HttpResponseUtil.delete(null);
    }
}
