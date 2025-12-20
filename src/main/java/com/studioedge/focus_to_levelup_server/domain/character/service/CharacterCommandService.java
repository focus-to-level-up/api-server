package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterAsset;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterSlotFullException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
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
    private final MemberAssetRepository memberAssetRepository;

    /**
     * 캐릭터를 배치할 위치를 자동으로 결정
     * 위치 1~7
     * 우선순위: 3 → 4 → 1 → 7 → 5 → 2 → 6
     *
     * @param memberId 유저 ID
     * @return 배치할 위치 (1~7)
     * @throws CharacterSlotFullException 모든 슬롯이 가득 찬 경우
     */
    public Integer assignFloor(Long memberId) {
        // 배치 우선순위: 3 → 4 → 1 → 7 → 5 → 2 → 6
        int[] positionPriority = {3, 4, 1, 7, 5, 2, 6};

        for (int position : positionPriority) {
            if (memberCharacterRepository.countByMemberIdAndFloor(memberId, position) == 0) {
                return position;
            }
        }

        // 모든 슬롯이 가득 찬 경우
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

        // 캐릭터 관련 Asset 지급 (프로필 이미지/테두리)
        grantInitialCharacterAssets(member, character);

        return memberCharacter;
    }

    /**
     * 캐릭터 초기 보상 Asset 지급
     * - 프로필 테두리: 무조건 지급
     * - 프로필 이미지: '1단계'만 지급 (2, 3단계는 진화 시 해금)
     */
    public void grantInitialCharacterAssets(Member member, Character character) {
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
            log.info("Granted asset '{}' to member {}", asset.getName(), member.getId());
        }
    }
}
