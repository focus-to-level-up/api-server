package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dto.request.SetDefaultCharacterRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterListResponse;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.InvalidDefaultEvolutionException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.MemberCharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCharacterService {

    private final MemberCharacterRepository memberCharacterRepository;

    /**
     * 보유 캐릭터 목록 조회 (등급별 필터링 가능)
     * @param rarity null이면 전체 조회, 값이 있으면 등급별 조회
     */
    public MemberCharacterListResponse getAllMemberCharacters(Long memberId, Rarity rarity) {
        List<MemberCharacter> memberCharacters = memberCharacterRepository.findAllByMemberIdWithCharacter(memberId);

        // 등급 필터링
        if (rarity != null) {
            memberCharacters = memberCharacters.stream()
                    .filter(mc -> mc.getCharacter().getRarity() == rarity)
                    .toList();
        }

        List<MemberCharacterResponse> responses = memberCharacters.stream()
                .map(MemberCharacterResponse::from)
                .toList();
        return MemberCharacterListResponse.from(responses);
    }

    /**
     * 대표 캐릭터 조회
     */
    public MemberCharacterResponse getDefaultCharacter(Long memberId) {
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .orElseThrow(MemberCharacterNotFoundException::new);
        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 대표 캐릭터 설정
     * 1. 기존 대표 캐릭터 해제
     * 2. 새로운 대표 캐릭터 설정
     * 3. defaultEvolution 검증 및 설정
     */
    @Transactional
    public MemberCharacterResponse setDefaultCharacter(Long memberId, SetDefaultCharacterRequest request) {
        // 1. 기존 대표 캐릭터 해제
        memberCharacterRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .ifPresent(MemberCharacter::unsetAsDefault);

        // 2. 새로운 대표 캐릭터 조회
        MemberCharacter memberCharacter = memberCharacterRepository
                .findByMemberIdAndCharacterId(memberId, request.characterId())
                .orElseThrow(MemberCharacterNotFoundException::new);

        // 3. defaultEvolution 검증 (현재 진화 단계보다 큰 값은 설정 불가)
        if (request.defaultEvolution() > memberCharacter.getEvolution()) {
            throw new InvalidDefaultEvolutionException();
        }

        // 4. 대표 캐릭터로 설정
        memberCharacter.setAsDefault(request.defaultEvolution());

        return MemberCharacterResponse.from(memberCharacter);
    }
}
