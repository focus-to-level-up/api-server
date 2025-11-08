package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 캐릭터 목록 응답
 */
@Builder
public record CharacterListResponse(
        List<CharacterResponse> characters
) {
    public static CharacterListResponse from(List<CharacterResponse> characters) {
        return CharacterListResponse.builder()
                .characters(characters)
                .build();
    }
}