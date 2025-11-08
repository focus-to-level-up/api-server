package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreatePlannerListRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreatePlannerRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.TodayPlannerListResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.TodayPlannerResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerService {
    private final DailyGoalRepository dailyGoalRepository;
    private final PlannerRepository plannerRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public TodayPlannerListResponse getTodayPlanner(Member member) {
        LocalDate serviceDate = AppConstants.getServiceDate();
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(member.getId(), serviceDate)
                .orElseThrow(DailyGoalNotFoundException::new);
        List<Planner> planners = plannerRepository.findAllByDailyGoalWithSubject(dailyGoal);
        List<TodayPlannerResponse> responses = planners.stream()
                .map(TodayPlannerResponse::of)
                .collect(Collectors.toList());

        return TodayPlannerListResponse.of(responses);
    }

    @Transactional
    public void upsertTodayPlanner(Member member, CreatePlannerListRequest requests) {
        LocalDate serviceDate = AppConstants.getServiceDate();
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(member.getId(), serviceDate)
                .orElseThrow(DailyGoalNotFoundException::new);
        // 1. 기존에 있던 플래너 항목들을 모두 삭제 (덮어쓰기)
        plannerRepository.deleteAllByDailyGoal(dailyGoal);

        // 2. 요청 DTO에서 Subject ID 목록만 추출 (N+1 방지용)
        Set<Long> subjectIds = requests.requestList().stream()
                .map(CreatePlannerRequest::subjectId)
                .collect(Collectors.toSet());

        if (subjectIds.isEmpty()) {
            return;
        }

        // 3. (최적화) Subject 목록을 DB에서 한 번에 조회
        Map<Long, Subject> subjectMap = subjectRepository.findAllById(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // 4. Planner 엔티티 리스트 생성
        List<Planner> plannersToSave = new ArrayList<>();
        for(CreatePlannerRequest request : requests.requestList()) {
            Subject subject = subjectMap.get(request.subjectId());
            if (!subject.getMember().getId().equals(member.getId())) {
                throw new SubjectUnAuthorizedException();
            }
            plannersToSave.add(CreatePlannerRequest.of(dailyGoal, subject, request));
        }

        plannerRepository.saveAll(plannersToSave);
    }
}
