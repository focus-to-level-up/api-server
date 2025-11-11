package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.FocusModeAnimationResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
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

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
public class FocusService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubjectRepository subjectRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final DailySubjectRepository dailySubjectRepository;
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
        LocalDate serviceDate = getServiceDate();

        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(m.getId())
                .orElseThrow(InvalidMemberException::new);
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(m.getId(), LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(m.getId(), true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        DailySubject dailySubject = dailySubjectRepository.findByMemberAndSubjectAndDate(member, subject, serviceDate)
                .orElseGet(() -> {
                    // 오늘 해당 과목으로 공부한 기록이 없으면, 새로 생성
                    return DailySubject.builder()
                            .member(member)
                            .subject(subject)
                            .date(serviceDate)
                            .build();
                    // save()는 @Transactional 종료 시 자동으로 수행됨 (orElseGet 내부에서는 save 명시)
                    // -> 수정: orElseGet 밖에서 save/update를 처리하는 것이 더 명확함.
                });
        if (!subject.getMember().getId().equals(m.getId())) {
            throw new SubjectUnAuthorizedException();
        }

        // 레벨 업
        member.levelUp(focusMinutes * 10);
        // 골드 획득
        memberInfo.addGold(focusMinutes * 10);
        // 일일 목표 공부 시간 더하기
        dailyGoal.addCurrentMinutes(focusMinutes);
        // 과목 공부 시간 더하기
        dailySubject.addSeconds(request.focusSeconds());
        // 캐릭터 친밀도 상승
        memberCharacter.levelUp(focusMinutes * 10);
        // 집중 상태 해제
        member.focusOff();

        // 만약 dailySubject가 생성되어있지 않다면 저장해야함.
        dailySubjectRepository.save(dailySubject);
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
