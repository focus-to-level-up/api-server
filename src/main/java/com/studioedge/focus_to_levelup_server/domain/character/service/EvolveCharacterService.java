package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterAsset;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterEvolveException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterUnauthorizedException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.MemberCharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvolveCharacterService {
    private final MemberInfoRepository memberInfoRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final MemberAssetRepository memberAssetRepository;
    /**
     * 캐릭터 진화
     */
    @Transactional
    public void evolveCharacter(Long memberId, Long memberCharacterId, boolean doFastEvolution) {
        // 1. 맴버 캐릭터 조회
        MemberCharacter memberCharacter = memberCharacterRepository.findByIdWithAssets(memberCharacterId)
                .orElseThrow(MemberCharacterNotFoundException::new);

        // 2. 맴버 캐릭터 소유 여부 판단
        if (!memberCharacter.getMember().getId().equals(memberId)) {
            throw new CharacterUnauthorizedException();
        }

        // 3. 캐릭터 최종 진화인지 판단
        if (memberCharacter.getEvolution() >= 3) {
            throw new CharacterEvolveException();
        }

        // 4. 캐릭터가 진화할 수 있는 친밀도인지 판단
        int requiredLevel = getRequiredLevelForNextEvolution(memberCharacter);
        if (memberCharacter.getCurrentLevel() < requiredLevel) {
            if (doFastEvolution) {
                int requiredDiamond = (requiredLevel - memberCharacter.getCurrentLevel()) * 20;
                MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId).orElseThrow(InvalidMemberException::new);
                memberInfo.decreaseDiamond(requiredDiamond);
                memberCharacter.jumpToLevel(requiredLevel);
            } else {
                throw new CharacterEvolveException();
            }
        }

        // 6. [최적화] 진화 보상 에셋 지급 (메모리 필터링)
        int evolution = memberCharacter.evolve();
        String targetNameKeyword = evolution + "단계";
        Asset rewardAsset = memberCharacter.getCharacter().getCharacterAssets().stream()
                .map(CharacterAsset::getAsset)
                .filter(asset ->
                        asset.getType() == AssetType.CHARACTER_PROFILE_IMAGE && // 프로필 이미지만
                                asset.getName().contains(targetNameKeyword)             // 현재 진화 단계 이름 포함
                )
                .findFirst()
                .orElse(null);

        // 7. 보상 지급 (해당하는 에셋이 있고, 아직 없다면 저장)
        if (rewardAsset != null) {
            if (!memberAssetRepository.existsByMemberIdAndAssetId(memberId, rewardAsset.getId())) {
                MemberAsset memberAsset = MemberAsset.builder()
                        .member(memberCharacter.getMember())
                        .asset(rewardAsset)
                        .build();
                memberAssetRepository.save(memberAsset);
            }
        }
    }

    private int getRequiredLevelForNextEvolution(MemberCharacter memberCharacter) {
        Rarity rarity = memberCharacter.getCharacter().getRarity();
        Integer evolution = memberCharacter.getEvolution();

        return switch (rarity) {
            case RARE -> (evolution == 1) ? 400 : 800;
            case EPIC -> (evolution == 1) ? 800 : 1600;
            case UNIQUE -> (evolution == 1) ? 1600 : 3200;
            default -> throw new CharacterEvolveException();
        };
    }
}
