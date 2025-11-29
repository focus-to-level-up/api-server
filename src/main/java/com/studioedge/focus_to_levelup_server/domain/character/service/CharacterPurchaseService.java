package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dto.request.CharacterPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterAsset;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterSlotFullException;
import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
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
    private final MemberAssetRepository memberAssetRepository;
    private final CharacterCommandService characterCommandService;

    /**
     * 캐릭터 구매
     * 1. 중복 구매 체크
     * 2. 다이아 차감
     * 3. 자동 층수 배치
     * 4. MemberCharacter 생성
     * 5. 캐릭터 관련 Asset 지급 (프로필 이미지/테두리)
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

        // 5. 캐릭터 지급 (공통 서비스 사용)
        Integer floor = characterCommandService.assignFloor(memberId);
        MemberCharacter memberCharacter = MemberCharacter.builder()
                .member(member)
                .character(character)
                .floor(floor)
                .build();

        memberCharacterRepository.save(memberCharacter);

        // 6. 캐릭터에 연결된 Asset 지급 (프로필 이미지/테두리)
        grantCharacterAssets(member, character);

        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 캐릭터에 연결된 Asset을 유저에게 지급
     * - 이미 보유한 Asset은 중복 지급하지 않음
     */
    private void grantCharacterAssets(Member member, Character character) {
        for (CharacterAsset characterAsset : character.getCharacterAssets()) {
            Long assetId = characterAsset.getAsset().getId();

            // 이미 보유하지 않은 경우에만 지급
            if (!memberAssetRepository.existsByMemberIdAndAssetId(member.getId(), assetId)) {
                MemberAsset memberAsset = MemberAsset.builder()
                        .member(member)
                        .asset(characterAsset.getAsset())
                        .build();
                memberAssetRepository.save(memberAsset);
            }
        }
    }
}
