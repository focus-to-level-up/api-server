package com.studioedge.domain.focus.business;

import com.studioedge.focus.repository.AllowedAppRepository;
import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.domain.focus.request.SaveAllowedAppRequest;
import com.studioedge.focus.entity.AllowedApp;
import com.studioedge.focus.entity.DailyGoal;
import com.studioedge.focus.exception.AllowedAppNotFoundException;
import com.studioedge.focus.exception.DailyGoalNotFoundException;
import com.studioedge.domain.member.response.AllowedAppsDto;
import com.studioedge.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllowedAppService {
    private final DailyGoalRepository dailyGoalRepository;
    private final AllowedAppRepository allowedAppRepository;
    @Transactional
    public void saveAllowedAppTime(Member member, SaveAllowedAppRequest request) {
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(member.getId(), LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        AllowedApp allowedApp = allowedAppRepository.findByMemberIdAndAppIdentifier(member.getId(), request.appIdentifier())
                .orElseThrow(AllowedAppNotFoundException::new);

        dailyGoal.useApp(request.usingSeconds());
        allowedApp.useApp(request.usingSeconds());
    }
    @Transactional
    public void updateAllowedApps(Member member, AllowedAppsDto requests) {
        List<AllowedApp> allowedApps = allowedAppRepository.findAllByMember(member);
        allowedAppRepository.deleteAll(allowedApps);
        allowedAppRepository.saveAll(AllowedAppsDto.from(member, requests));
    }
    public AllowedAppsDto getAllowedApps(Member member) {
        List<AllowedApp> allowedApps = allowedAppRepository.findAllByMemberId(member.getId());
        return AllowedAppsDto.of(allowedApps);
    }
}
