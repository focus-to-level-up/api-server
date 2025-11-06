package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.AllowedAppRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveAllowedAppRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.AllowedApp;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.AllowedAppNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final DailyGoalRepository dailyGoalRepository;
    private final SubjectRepository subjectRepository;
    private final AllowedAppRepository allowedAppRepository;

    @Transactional(readOnly = true)
    public List<GetSubjectResponse> getSubjectList(Long memberId) {
        List<Subject> subjects = subjectRepository.findAllByMemberId(memberId);
        return subjects.stream()
                .map(GetSubjectResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createSubject(Member member, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findByMemberAndName(member, request.name())
                .orElse(subjectRepository.save(CreateSubjectRequest.from(member, request)));
        subject.update(request);
    }

    @Transactional
    public void updateSubject(Long memberId, Long subjectId, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(memberId))
            throw new SubjectUnAuthorizedException();
        subject.update(request);
    }

    @Transactional
    public void deleteSubject(Long memberId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(memberId))
            throw new SubjectUnAuthorizedException();
        subject.delete();
    }

    @Transactional
    public void saveAllowedAppTime(Long memberId, SaveAllowedAppRequest request) {
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        AllowedApp allowedApp = allowedAppRepository.findByMemberIdAndAppIdentifier(memberId, request.appIdentifier())
                .orElseThrow(AllowedAppNotFoundException::new);

        dailyGoal.useApp(request.usingSeconds());
        allowedApp.useApp(request.usingSeconds());
    }
}
