package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.exception.SchoolNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.FocusModeImageResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.MonsterAnimationResponse;
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
import java.util.Map;
import java.util.stream.Collectors;

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
    private final SchoolRepository schoolRepository;
    private final GuildMemberRepository guildMemberRepository;
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
        LocalDate serviceDate = getServiceDate();
        int focusExp = focusMinutes * 10;


        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(m.getId())
                .orElseThrow(InvalidMemberException::new);
        DailyGoal dailyGoal = dailyGoalRepository.findByMemberIdAndDailyGoalDate(m.getId(), serviceDate)
                .orElseThrow(DailyGoalNotFoundException::new);
        Subject subject = this.subjectRepository.findByIdAndDeleteAtIsNull(subjectId)
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
        member.levelUp(focusExp);
        // 골드 획득
        memberInfo.addGold(focusExp);
        // 일일 목표 공부 시간 더하기
        dailyGoal.addCurrentMinutes(focusMinutes);
        // 과목 공부 시간 더하기
        dailySubject.addSeconds(request.focusSeconds());
        // 캐릭터 친밀도 상승
        memberCharacter.levelUp(focusExp);
        // 집중 상태 해제
        member.focusOff();

        // 만약 dailySubject가 생성되어있지 않다면 저장해야함.
        dailySubjectRepository.save(dailySubject);
        if (AppConstants.SCHOOL_CATEGORIES.contains(memberInfo.getCategoryMain())) {
            schoolRepository.findByName(memberInfo.getBelonging())
                    .orElseThrow(SchoolNotFoundException::new)
                    .plusTotalLevel(focusExp);
        }

        // 길드 주간 집중 시간 업데이트 (가입한 모든 길드)
        List<GuildMember> guildMembers = guildMemberRepository.findAllByMemberIdWithGuild(m.getId());
        for (GuildMember gm : guildMembers) {
            gm.addWeeklyFocusTime(request.focusSeconds());
        }

    }

    @Transactional
    public void startFocus(Member m) {
        Member member = memberRepository.findById(m.getId())
                .orElseThrow(MemberNotFoundException::new);
        member.focusOn();
    }

    // @TODO: 향후 리팩토링 필요함. 몬스터 종류 많아지고, 맵마다 다른 몬스터가 나온다면
    @Transactional(readOnly = true)
    public FocusModeImageResponse getFocusAnimation(Member member) {
        List<MonsterImage> monsterImages = monsterImageRepository.findAllWithMonster();
        // 몬스터(Monster) 객체 기준으로 그룹화
        Map<Monster, List<MonsterImage>> imagesByMonster = monsterImages.stream()
                .collect(Collectors.groupingBy(MonsterImage::getMonster));

        List<MonsterAnimationResponse> responses = new ArrayList<>();
        for (Map.Entry<Monster, List<MonsterImage>> entry : imagesByMonster.entrySet()) {

            Monster monster = entry.getKey();
            List<MonsterImage> images = entry.getValue();

            String moveUrl = findUrlByType(images, MonsterImageType.MOVE);
            String dieUrl = findUrlByType(images, MonsterImageType.DIE);

            responses.add(MonsterAnimationResponse.of(
                    monster.getName(),
                    dieUrl,
                    moveUrl
            ));
        }

        Background background = backgroundRepository.findByName(AppConstants.DEFAULT_FOCUS_BACKGROUND_NAME)
                .orElseThrow(BackgroundNotFoundException::new);

        return FocusModeImageResponse.of(background.getImageUrl(), responses);
    }

    /**
     * 몬스터 이미지 리스트에서 특정 타입의 URL을 찾는 헬퍼 메서드
     */
    private String findUrlByType(List<MonsterImage> images, MonsterImageType type) {
        return images.stream()
                .filter(m -> m.getType() == type)
                .map(MonsterImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }
}
