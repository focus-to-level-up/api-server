package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterSlotFullException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CharacterCommandService {

    private final MemberCharacterRepository memberCharacterRepository;

    /**
     * 캐릭터를 배치할 층수를 자동으로 결정
     * 우선순위: 2층 → 3층 → 1층
     * 각 층당 최대 2개까지 배치 가능
     *
     * @param memberId 유저 ID
     * @return 배치할 층수 (1, 2, 3)
     * @throws CharacterSlotFullException 모든 층이 가득 찬 경우
     */
    public Integer assignFloor(Long memberId) {
        // 2층 확인
        if (memberCharacterRepository.countByMemberIdAndFloor(memberId, 2) < 2) {
            return 2;
        }
        // 3층 확인
        if (memberCharacterRepository.countByMemberIdAndFloor(memberId, 3) < 2) {
            return 3;
        }
        // 1층 확인
        if (memberCharacterRepository.countByMemberIdAndFloor(memberId, 1) < 2) {
            return 1;
        }
        // 모든 층이 가득 참
        throw new CharacterSlotFullException();
    }

    /**
     * 캐릭터를 유저에게 지급 (보상, 구매 등에서 공통 사용)
     * 중복 체크 및 자동 층수 배치 포함
     *
     * @param member 유저
     * @param character 캐릭터
     * @return 생성된 MemberCharacter
     */
    public MemberCharacter grantCharacter(Member member, Character character) {
        // 중복 체크
        boolean alreadyOwned = memberCharacterRepository.existsByMemberIdAndCharacterId(
                member.getId(), character.getId());

        if (alreadyOwned) {
            log.warn("Member {} already owns character {}, skipping", member.getId(), character.getId());
            return null;
        }

        // 자동 층수 배치
        Integer floor = assignFloor(member.getId());

        // MemberCharacter 생성 및 저장
        MemberCharacter memberCharacter = MemberCharacter.builder()
                .member(member)
                .character(character)
                .floor(floor)
                .build();

        memberCharacterRepository.save(memberCharacter);
        log.info("Granted character {} to member {} (floor: {})",
                 character.getName(), member.getId(), floor);

        return memberCharacter;
    }
}
