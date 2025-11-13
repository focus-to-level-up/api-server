package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dto.request.CharacterPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterSlotFullException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterPurchaseService {

    private final CharacterRepository characterRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 캐릭터 구매
     * 1. 중복 구매 체크
     * 2. 다이아 차감
     * 3. 자동 층수 배치
     * 4. MemberCharacter 생성
     */
    public MemberCharacterResponse purchaseCharacter(Long memberId, CharacterPurchaseRequest request) {
        // 1. 중복 구매 체크
        if (memberCharacterRepository.existsByMemberIdAndCharacterId(memberId, request.characterId())) {
            throw new CharacterAlreadyPurchasedException();
        }

        // 2. 캐릭터 조회
        Character character = characterRepository.findById(request.characterId())
                .orElseThrow(CharacterNotFoundException::new);

        // 3. Member 및 MemberInfo 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 4. 다이아 차감 (내부에서 검증)
        memberInfo.decreaseDiamond(character.getPrice());

        // 5. 자동 층수 배치 (2층 → 3층 → 1층 순서, 각 층 최대 2개)
        Integer floor = assignFloor(memberId);

        // 6. MemberCharacter 생성
        MemberCharacter memberCharacter = MemberCharacter.builder()
                .member(member)
                .character(character)
                .floor(floor)
                .build();

        memberCharacterRepository.save(memberCharacter);

        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 캐릭터를 배치할 층수를 자동으로 결정
     * 우선순위: 2층 → 3층 → 1층
     * 각 층당 최대 2개까지 배치 가능
     * 모든 층이 가득 차면 예외 발생
     */
    private Integer assignFloor(Long memberId) {
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
}
