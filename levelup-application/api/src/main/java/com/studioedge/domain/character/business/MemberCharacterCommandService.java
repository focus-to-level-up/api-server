package com.studioedge.domain.character.business;

import com.studioedge.character.component.CharacterReader;
import com.studioedge.character.component.MemberCharacterReader;
import com.studioedge.character.component.MemberCharacterValidator;
import com.studioedge.character.component.MemberCharacterWriter;
import com.studioedge.character.entity.Character;
import com.studioedge.character.entity.CharacterAsset;
import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.exception.*;
import com.studioedge.common.enums.AssetType;
import com.studioedge.domain.character.request.CharacterPurchaseRequest;
import com.studioedge.domain.character.request.SetDefaultCharacterRequest;
import com.studioedge.domain.character.response.MemberCharacterResponse;
import com.studioedge.member.component.MemberReader;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.system.component.MemberAssetReader;
import com.studioedge.system.component.MemberAssetWriter;
import com.studioedge.system.entity.Asset;
import com.studioedge.system.entity.MemberAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCharacterCommandService {

    private final CharacterReader characterReader;
    private final MemberCharacterReader memberCharacterReader;
    private final MemberCharacterValidator memberCharacterValidator;
    private final MemberCharacterWriter memberCharacterWriter;
    private final MemberReader memberReader;
    private final MemberAssetReader memberAssetReader;
    private final MemberAssetWriter memberAssetWriter;

    private static final int MAX_EVOLUTION_STAGE = 3;

    /**
     * 캐릭터 구매
     */
    public MemberCharacterResponse purchaseCharacter(Long memberId, CharacterPurchaseRequest request) {
        // 1. 중복 구매 검증
        if (memberCharacterReader.exist(memberId, request.characterId())) {
            throw new CharacterAlreadyPurchasedException();
        }

        // 2. 캐릭터와 회원정보 조회
        Character character = characterReader.findOne(request.characterId());
        Member member = memberReader.findOne(memberId);
        MemberInfo memberInfo = member.getMemberInfo();

        // 3. 다이아 차감
        memberInfo.decreaseDiamond(character.getPrice());

        // 4. 캐릭터 지급
        MemberCharacter memberCharacter = grantCharacter(member, character);

        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 대표 캐릭터 설정
     */
    public MemberCharacterResponse setDefaultCharacter(Long memberId, SetDefaultCharacterRequest request) {
        // 1. 기존 대표 캐릭터 해제
        try {
            MemberCharacter defaultCharacter = memberCharacterReader.findDefaultCharacter(memberId);
            defaultCharacter.unsetAsDefault();
        } catch (MemberCharacterNotFoundException e) {
            log.warn("Member {} does not have a default character. Proceeding to set new default.", memberId);
        }

        // 2. 새로운 대표 캐릭터 조회
        MemberCharacter memberCharacter = memberCharacterReader.findOne(memberId, request.characterId());

        // 3. 진화 단계 검증 및 설정 (현재 진화 단계보다 큰 값은 설정 불가)
        if (request.defaultEvolution() > memberCharacter.getEvolution()) {
            throw new InvalidDefaultEvolutionException();
        }
        memberCharacter.setAsDefault(request.defaultEvolution());

        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 캐릭터 진화
     */
    public void evolveCharacter(Long memberId, Long memberCharacterId, boolean doFastEvolution) {
        // 1. 맴버 캐릭터 조회
        MemberCharacter memberCharacter = memberCharacterReader.findOne(memberCharacterId);

        // 2. 캐릭터 소유 여부 판단
        if (!memberCharacter.getMember().getId().equals(memberId)) {
            throw new CharacterUnauthorizedException();
        }

        // 3. 캐릭터 최종 진화인지 판단
        if (memberCharacter.getEvolution() >= MAX_EVOLUTION_STAGE) {
            throw new CharacterEvolveException();
        }

        // 4. 캐릭터가 진화할 수 있는 친밀도인지 판단
        int requiredLevel = memberCharacter.getRequiredLevelForNextEvolution();
        if (memberCharacter.getCurrentLevel() < requiredLevel) {
            if (doFastEvolution) {
                int requiredDiamond = (requiredLevel - memberCharacter.getCurrentLevel()) * 20;
                Member member = memberReader.findOne(memberId);
                MemberInfo memberInfo = member.getMemberInfo();
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
                                asset.getName().contains(targetNameKeyword)     // 현재 진화 단계 이름 포함
                )
                .findFirst()
                .orElse(null);

        // 7. 보상 지급 (해당하는 에셋이 있고, 아직 없다면 저장)
        if (rewardAsset != null) {
            if (!memberAssetReader.exist(memberId, rewardAsset.getId())) {
                MemberAsset memberAsset = MemberAsset.builder()
                        .member(memberCharacter.getMember())
                        .asset(rewardAsset)
                        .build();
                memberAssetWriter.save(memberAsset);
            }
        }
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
        boolean alreadyOwned = memberCharacterReader.exist(member.getId(), character.getId());

        if (alreadyOwned) {
            log.warn("Member {} already owns character {}, skipping", member.getId(), character.getId());
            return null;
        }

        // 자동 층수 배치
        Integer floor = memberCharacterValidator.assignFloor(member.getId());

        // MemberCharacter 생성 및 저장
        MemberCharacter memberCharacter = MemberCharacter.builder()
                .member(member)
                .character(character)
                .floor(floor)
                .build();

        memberCharacterWriter.save(memberCharacter);
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
        if (!memberAssetReader.exist(member.getId(), asset.getId())) {
            MemberAsset memberAsset = MemberAsset.builder()
                    .member(member)
                    .asset(asset)
                    .build();
            memberAssetWriter.save(memberAsset);
            log.info("Granted asset '{}' to member {}", asset.getName(), member.getId());
        }
    }
}
