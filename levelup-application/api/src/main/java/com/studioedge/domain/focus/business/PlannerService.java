package com.studioedge.domain.focus.business;

import com.studioedge.focus.repository.PlannerRepository;
import com.studioedge.domain.focus.response.PlannerListResponse;
import com.studioedge.domain.focus.response.PlannerResponse;
import com.studioedge.focus.entity.Planner;
import com.studioedge.focus.exception.PlannerNotFoundException;
import com.studioedge.member.entity.Member;
import com.studioedge.AppConstants;
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
