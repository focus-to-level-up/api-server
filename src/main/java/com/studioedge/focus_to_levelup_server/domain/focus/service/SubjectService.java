package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final DailySubjectRepository dailySubjectRepository;

    @Transactional(readOnly = true)
    public List<GetSubjectResponse> getSubjectList(Member member) {
        List<Subject> subjects = subjectRepository.findAllByMemberAndDeletedAtIsNull(member);
        List<DailySubject> dailySubjects = dailySubjectRepository.findAllByMemberAndDate(member, getServiceDate());
        Map<Long, Integer> todayMinutesMap = dailySubjects.stream()
                .collect(Collectors.toMap(
                        dailySubject -> dailySubject.getSubject().getId(),
                        DailySubject::getFocusSeconds
                ));

        return subjects.stream()
                .map(subject -> {
                    Integer todaySeconds = todayMinutesMap.getOrDefault(subject.getId(), 0);
                    return GetSubjectResponse.of(subject, todaySeconds);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void createSubject(Member member, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findByMemberAndName(member, request.name())
                .orElseGet(() -> {
                    return subjectRepository.save(CreateSubjectRequest.from(member, request));
                });
        subject.update(request);
    }

    @Transactional
    public void updateSubject(Member member, Long subjectId, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(member.getId()))
            throw new SubjectUnAuthorizedException();
        subject.update(request);
    }

    @Transactional
    public void deleteSubject(Member member, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(member.getId()))
            throw new SubjectUnAuthorizedException();
        subject.delete();
    }
}
