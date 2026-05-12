package com.studioedge.domain.character.response;

import com.studioedge.character.entity.MemberCharacter;
import lombok.Builder;

import java.util.List;

/**
 * 유저가 보유한 캐릭터 목록 응답
 */
@Builder
public record MemberCharacterListResponse(
        List<MemberCharacterResponse> characters
) {
    public static MemberCharacterListResponse from(List<MemberCharacter> memberCharacters) {
        List<MemberCharacterResponse> memberCharacterResponses = memberCharacters.stream()
                .map(MemberCharacterResponse::from)
                .toList();

        return MemberCharacterListResponse.builder()
                .characters(memberCharacterResponses)
                .build();
    }
}
