package com.studioedge.domain.character.business;

import com.studioedge.character.component.MemberCharacterReader;
import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.enums.Rarity;
import com.studioedge.domain.character.response.MemberCharacterListResponse;
import com.studioedge.domain.character.response.MemberCharacterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCharacterQueryService {

    private final MemberCharacterReader memberCharacterReader;

    /**
     * 대표 캐릭터 조회
     */
    public MemberCharacterResponse getDefaultCharacter(Long memberId) {
        MemberCharacter memberCharacter = memberCharacterReader.findDefaultCharacter(memberId);
        return MemberCharacterResponse.from(memberCharacter);
    }

    /**
     * 보유 캐릭터 목록 조회 (등급별 필터링 가능)
     * @param rarity null이면 전체 조회, 값이 있으면 등급별 조회
     */
    public MemberCharacterListResponse getAllMemberCharacters(Long memberId, Rarity rarity) {
        List<MemberCharacter> memberCharacters = memberCharacterReader.findAllByMemberId(memberId);

        if (rarity != null) {
            memberCharacters = memberCharacters.stream()
                    .filter(mc -> mc.getCharacter().getRarity() == rarity)
                    .toList();
        }

        return MemberCharacterListResponse.from(memberCharacters);
    }
}
