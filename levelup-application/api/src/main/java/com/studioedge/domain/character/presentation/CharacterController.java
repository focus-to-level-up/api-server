package com.studioedge.domain.character.presentation;

import com.studioedge.character.enums.Rarity;
import com.studioedge.domain.character.business.MemberCharacterCommandService;
import com.studioedge.domain.character.business.CharacterQueryService;
import com.studioedge.domain.character.request.CharacterPurchaseRequest;
import com.studioedge.domain.character.response.CharacterListResponse;
import com.studioedge.domain.character.response.CharacterResponse;
import com.studioedge.domain.character.response.MemberCharacterResponse;
import com.studioedge.global.response.HttpResponseUtil;
import com.studioedge.member.entity.Member;
import com.studioedge.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Character", description = "캐릭터 API")
@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterQueryService characterQueryService;
    private final MemberCharacterCommandService memberCharacterCommandService;

    @Operation(summary = "캐릭터 목록 조회", description = "전체 또는 등급별 캐릭터 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<CharacterListResponse>> getCharacters(
            @Parameter(description = "캐릭터 등급 (null이면 전체 조회)", required = false)
            @RequestParam(required = false) Rarity rarity
    ) {
        CharacterListResponse response = characterQueryService.getCharacters(rarity);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "캐릭터 상세 조회", description = "캐릭터 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{characterId}")
    public ResponseEntity<CommonResponse<CharacterResponse>> getCharacterById(
            @Parameter(description = "캐릭터 ID", required = true)
            @PathVariable Long characterId
    ) {
        CharacterResponse response = characterQueryService.getCharacterById(characterId);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "캐릭터 구매", description = "다이아를 사용하여 캐릭터를 구매합니다.")
    @PostMapping("/purchase")
    public ResponseEntity<CommonResponse<MemberCharacterResponse>> purchaseCharacter(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody CharacterPurchaseRequest request
    ) {
        MemberCharacterResponse response = memberCharacterCommandService.purchaseCharacter(member.getId(), request);
        return HttpResponseUtil.created(response);
    }
}
