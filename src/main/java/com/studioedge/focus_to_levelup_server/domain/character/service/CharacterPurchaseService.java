package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dto.request.CharacterPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
<<<<<<< Updated upstream
import com.studioedge.focus_to_levelup_server.domain.character.repository.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
=======
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterSlotFullException;
import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
>>>>>>> Stashed changes
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
     * 3. MemberCharacter 생성
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

        // 5. MemberCharacter 생성
        MemberCharacter memberCharacter = MemberCharacter.builder()
                .member(member)
                .character(character)
                .floor(request.floor())
                .build();

        memberCharacterRepository.save(memberCharacter);

        return MemberCharacterResponse.from(memberCharacter);
    }
}
