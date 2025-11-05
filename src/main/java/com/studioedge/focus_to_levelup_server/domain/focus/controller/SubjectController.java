package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SubjectService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
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

    @GetMapping("/v1/subjects")
    public ResponseEntity<CommonResponse<List<GetSubjectResponse>>> getSubjectList(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(subjectService.getSubjectList(memberId));
    }

    @PostMapping("/v1/subject")
    public ResponseEntity<CommonResponse<Void>> createSubject(
            @AuthenticationPrincipal Long memberId
    ) {
        subjectService.createSubject(memberId);
        return HttpResponseUtil.created(null);
    }

    @PostMapping("/v1/subject/{subjectId}")
    public ResponseEntity<CommonResponse<Void>> saveSession(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.saveSession(memberId, subjectId);
        return HttpResponseUtil.ok(null);
    }

    @PutMapping("/v1/subject/{subjectId}")
    public ResponseEntity<CommonResponse<Void>> updateSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.updateSubject(memberId, subjectId);
        return HttpResponseUtil.updated(null);
    }

    @DeleteMapping("/v1/subject/{subjectId}")
    public ResponseEntity<CommonResponse<Void>> deleteSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.deleteSubject(memberId, subjectId);
        return HttpResponseUtil.delete(null);
    }
}
