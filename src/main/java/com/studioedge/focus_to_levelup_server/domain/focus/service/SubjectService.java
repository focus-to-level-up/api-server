package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.TodoRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateSubjectRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetSubjectResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetTodoResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public List<GetSubjectResponse> getSubjectList(Member member, LocalDate date) {
        LocalDate serviceDate = date == null ? getServiceDate() : date;
        List<Subject> subjects = subjectRepository.findAllByMemberAndDeleteAtIsNull(member);
        List<DailySubject> dailySubjects = dailySubjectRepository.findAllByMemberAndDate(member, serviceDate);
        Map<Long, Integer> todayMinutesMap = dailySubjects.stream()
                .collect(Collectors.toMap(
                        dailySubject -> dailySubject.getSubject().getId(),
                        DailySubject::getFocusSeconds
                ));

        return subjects.stream()
                .map(subject -> {
                    Integer todaySeconds = todayMinutesMap.getOrDefault(subject.getId(), 0);
                    List<Todo> todos = todoRepository.findAllBySubjectId(subject.getId());
                    List<GetTodoResponse> responses = todos.stream()
                            .map(GetTodoResponse::of)
                            .collect(Collectors.toList());
                    return GetSubjectResponse.of(subject, todaySeconds, responses);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void createSubject(Member member, CreateSubjectRequest request) {
        List<Subject> subjects = subjectRepository.findAllByMemberAndName(member, request.name());
        Subject targetSubject = subjects.stream()
                .filter(s -> s.getDeleteAt() == null) // 활성화된 과목
                .findFirst()
                .orElseGet(() -> {
                    return subjects.stream()
                            .max((s1, s2) -> s1.getId().compareTo(s2.getId()))
                            .orElse(null);
                });

        if (targetSubject == null) {
            Subject newSubject = CreateSubjectRequest.from(member, request);
            subjectRepository.save(newSubject);
        } else {
            targetSubject.update(request);
        }
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
