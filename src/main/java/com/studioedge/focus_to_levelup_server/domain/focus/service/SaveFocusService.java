package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
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
        /**
         * member 레벨업 -> member.levelUp()
         * subject 공부 시간 누적
         * dailyGoal 누적
         * 대표 캐릭터 친밀도 누적
         * 현재 집중중 상태 해제
         * */

        int focusMinutes = request.focusSeconds() / 60;

        Subject subject = subjectStatRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(memberId)) {
            throw new SubjectUnAuthorizedException();
        }
        subject.increaseFocusSeconds(request.focusSeconds());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        member.levelUp(focusMinutes * 10);

        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        dailyGoal.increaseCurrentMinutes(focusMinutes);

        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(memberId, true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        memberCharacter.increaseLevel(focusMinutes * 10);

        member.focusOff();
    }
}
