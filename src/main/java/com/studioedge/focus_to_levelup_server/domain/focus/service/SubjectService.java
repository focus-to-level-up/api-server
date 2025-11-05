package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetSubjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public List<GetSubjectResponse> getSubjectList(Long memberId) {
        return null;
    }

    public void createSubject(Long memberId) {

    }

    public void saveSession(Long memberId, Long subjectId) {

    }

    public void updateSubject(Long memberId, Long subjectId) {

    }

    public void  deleteSubject(Long memberId, Long subjectId) {

    }
}
