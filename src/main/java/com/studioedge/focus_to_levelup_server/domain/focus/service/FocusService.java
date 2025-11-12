package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.exception.SchoolNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.FocusModeImageResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.MonsterAnimationResponse;
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
import com.studioedge.focus_to_levelup_server.domain.system.dao.BackgroundRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MonsterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MonsterRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Background;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Monster;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MonsterImage;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MonsterImageType;
import com.studioedge.focus_to_levelup_server.domain.system.exception.BackgroundNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FocusService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubjectRepository subjectRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final SchoolRepository schoolRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterImageRepository monsterImageRepository;
    private final BackgroundRepository backgroundRepository;
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
        int focusExp = focusMinutes * 10;

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
        member.levelUp(focusExp);
        memberInfo.addGold(focusExp);

        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(m.getId(), LocalDate.now())
                .orElseThrow(DailyGoalNotFoundException::new);
        dailyGoal.increaseCurrentMinutes(focusMinutes);

        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefault(m.getId(), true)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        memberCharacter.increaseLevel(focusExp);

        member.focusOff();
        if (AppConstants.SCHOOL_CATEGORIES.contains(memberInfo.getCategoryMain())) {
            schoolRepository.findByName(memberInfo.getBelonging())
                    .orElseThrow(SchoolNotFoundException::new)
                    .plusTotalLevel(focusExp);
        }

    }

    @Transactional
    public void startFocus(Member m) {
        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        member.focusOn();
    }

    @Transactional(readOnly = true)
    public FocusModeImageResponse getFocusAnimation(Member member) {
        // @TODO: 향후 리팩토링 필요함. 몬스터 종류 많아지고, 맵마다 다른 몬스터가 나온다면
        List<Monster> monsters = monsterRepository.findAll();
        List<MonsterAnimationResponse> responses = new ArrayList<>();
        for(Monster monster : monsters) {
            List<MonsterImage> monsterImages = monsterImageRepository.findAllByMonster(monster);
            String move = monsterImages.get(0).getType().equals(MonsterImageType.MOVE) ?
                    monsterImages.get(0).getImageUrl() : monsterImages.get(1).getImageUrl();
            String die = monsterImages.get(0).getType().equals(MonsterImageType.DIE) ?
                    monsterImages.get(0).getImageUrl() : monsterImages.get(1).getImageUrl();
            responses.add(MonsterAnimationResponse.of(monster.getName(), die, move))
        }
        Background background = backgroundRepository.findByName(AppConstants.DEFAULT_FOCUS_BACKGROUND_NAME)
                .orElseThrow(BackgroundNotFoundException::new);
        return FocusModeImageResponse.of(background.getImageUrl(), responses);
    }

}
