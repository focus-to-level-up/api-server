package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SaveSessionService;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SubjectService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubjectController {
    private final SubjectService subjectService;
    private final SaveSessionService saveSessionService;
    /**
     * 과목 리스트 조회
     * */
    @GetMapping("/v1/subjects")
    public ResponseEntity<CommonResponse<List<GetSubjectResponse>>> getSubjectList(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(subjectService.getSubjectList(memberId));
    }

    /**
     * 과목 생성
     * */
    @PostMapping("/v1/subject")
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
    public ResponseEntity<CommonResponse<Void>> saveSession(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        saveSessionService.saveSession(memberId, subjectId);
        return HttpResponseUtil.ok(null);
    }

    /**
     * 과목 수정
     * */
    @PutMapping("/v1/subject/{subjectId}")
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
    public ResponseEntity<CommonResponse<Void>> deleteSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.deleteSubject(memberId, subjectId);
        return HttpResponseUtil.delete(null);
    }
}
