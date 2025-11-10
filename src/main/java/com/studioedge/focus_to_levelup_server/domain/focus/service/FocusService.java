package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.FocusModeAnimationResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.DailyGoalNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FocusService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubjectRepository subjectRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    @Transactional
    public void saveFocus(Member m, Long subjectId, SaveFocusRequest request) {
        /**
         * member 레벨업 -> member.levelUp()
         * subject 공부 시간 누적
         * dailyGoal 누적
         * 대표 캐릭터 친밀도 누적
         * 현재 집중중 상태 해제
         * */

        int focusMinutes = request.focusSeconds() / 60;

        Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(m.getId())) {
            throw new SubjectUnAuthorizedException();
        }
        subject.increaseFocusSeconds(request.focusSeconds());

        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(m.getId())
                .orElseThrow(InvalidMemberException::new);
        member.levelUp(focusMinutes * 10);
        memberInfo.addGold(focusMinutes * 10);

        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(m.getId(), LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        dailyGoal.increaseCurrentMinutes(focusMinutes);

        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(m.getId(), true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        memberCharacter.increaseLevel(focusMinutes * 10);

        member.focusOff();
    }

    @Transactional
    public void startFocus(Member m) {
        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        member.focusOn();
    }

    @Transactional(readOnly = true)
    public FocusModeAnimationResponse getFocusAnimation(Member member) {
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(member.getId(), true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        return null;
    }

}
