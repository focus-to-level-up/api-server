package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SaveFocusService {
    private final MemberRepository memberRepository;
    private final SubjectRepository subjectStatRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;

    @Transactional
    public void saveFocus(Long memberId, Long subjectId, SaveFocusRequest request) {
        // member 레벨업 -> member.levelUp()
        // subject 공부 시간 누적
        // dailyGoal 누적
        // MemberCharacter 친밀도 누적
        // 주간 보상 누적
        int focusMinutes = request.focusSeconds() / 60;
        int restSeconds = request.focusSeconds() % 60;

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        member.levelUp(focusMinutes * 10);

        Subject subject = subjectStatRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        subject.increaseFocusSeconds(request.focusSeconds());

        DailyGoal dailyGoal = dailyGoalRepository.findByMemberAndDailyGoalDate(member, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        dailyGoal.increaseCurrentMinutes(focusMinutes);


    }
}
