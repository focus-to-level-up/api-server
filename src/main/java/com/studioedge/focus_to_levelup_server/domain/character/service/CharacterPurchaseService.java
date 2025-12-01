package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dto.request.CharacterPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterAsset;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
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
        grantInitialCharacterAssets(member, character);

        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 캐릭터 초기 구매 보상 Asset 지급
     * - 프로필 테두리: 무조건 지급
     * - 프로필 이미지: '1단계'만 지급 (2, 3단계는 진화 시 해금)
     */
    private void grantInitialCharacterAssets(Member member, Character character) {
        for (CharacterAsset characterAsset : character.getCharacterAssets()) {
            Asset asset = characterAsset.getAsset();
            AssetType type = asset.getType();

            // 1. 프로필 테두리는 지급
            if (type == AssetType.CHARACTER_PROFILE_BORDER) {
                saveMemberAssetIfNotExists(member, asset);
                continue;
            }

            // 2. 프로필 이미지는 이름에 '1단계'가 포함된 것만 지급
            if (type == AssetType.CHARACTER_PROFILE_IMAGE) {
                if (asset.getName().contains("1단계")) {
                    saveMemberAssetIfNotExists(member, asset);
                }
            }
        }
    }

    private void saveMemberAssetIfNotExists(Member member, Asset asset) {
        if (!memberAssetRepository.existsByMemberIdAndAssetId(member.getId(), asset.getId())) {
            MemberAsset memberAsset = MemberAsset.builder()
                    .member(member)
                    .asset(asset)
                    .build();
            memberAssetRepository.save(memberAsset);
        }
    }
}
