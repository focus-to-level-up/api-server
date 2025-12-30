package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.PlannerListResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.PlannerResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.PlannerNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerService {
    private final PlannerRepository plannerRepository;

    @Transactional(readOnly = true)
    public PlannerListResponse getTodayPlanner(Member member, LocalDate date) {
        LocalDate serviceDate = date == null ? AppConstants.getServiceDate() : date;
        List<Planner> planners = plannerRepository.findAllWithMemberAndSubjectByMemberIdAndDate(member.getId(), serviceDate);
        if (planners.isEmpty()) {
            throw new PlannerNotFoundException();
        }
        List<PlannerResponse> responses = planners.stream()
                .map(PlannerResponse::of)
                .collect(Collectors.toList());

        return PlannerListResponse.of(responses);
    }
}
