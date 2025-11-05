package com.studioedge.focus_to_levelup_server.domain.focus.controller;

<<<<<<< HEAD
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveAllowedAppRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SaveFocusService;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SubjectService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import jakarta.validation.Valid;
=======
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.SubjectService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
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
<<<<<<< HEAD
    private final SaveFocusService saveFocusService;
    /**
     * 과목 리스트 조회
     * */
=======

>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    @GetMapping("/v1/subjects")
    public ResponseEntity<CommonResponse<List<GetSubjectResponse>>> getSubjectList(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(subjectService.getSubjectList(memberId));
    }

<<<<<<< HEAD
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
    public ResponseEntity<CommonResponse<Void>> saveAllowedAppTime(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody SaveAllowedAppRequest request
    ) {
        subjectService.saveAllowedAppTime(member, request);
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
=======
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

>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    @DeleteMapping("/v1/subject/{subjectId}")
    public ResponseEntity<CommonResponse<Void>> deleteSubject(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        subjectService.deleteSubject(memberId, subjectId);
        return HttpResponseUtil.delete(null);
    }
}
