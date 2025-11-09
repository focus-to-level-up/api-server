package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 유저가 보유한 캐릭터 목록 응답
 */
@Builder
public record MemberCharacterListResponse(
        List<MemberCharacterResponse> characters
) {
    public static MemberCharacterListResponse from(List<MemberCharacterResponse> characters) {
        return MemberCharacterListResponse.builder()
                .characters(characters)
                .build();
    }
}