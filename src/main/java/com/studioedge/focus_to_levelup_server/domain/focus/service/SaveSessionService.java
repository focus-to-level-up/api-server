package com.studioedge.focus_to_levelup_server.domain.focus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveSessionService {
    public void saveSession(Long memberId, Long subjectId) {
        // member 레벨업 -> member.levelUp()
        // subject 공부 시간 누적
        // dailyGoal 누적
        // MemberCharacter 친밀도 누적
        //
    }
}
