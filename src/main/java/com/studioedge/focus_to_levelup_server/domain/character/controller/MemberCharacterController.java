package com.studioedge.focus_to_levelup_server.domain.character.controller;

import com.studioedge.focus_to_levelup_server.domain.character.dto.request.SetDefaultCharacterRequest;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterListResponse;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.MemberCharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.service.MemberCharacterService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Character", description = "보유 캐릭터 API")
@RestController
@RequestMapping("/api/members/me/characters")
@RequiredArgsConstructor
public class MemberCharacterController {

    private final MemberCharacterService memberCharacterService;

    @Operation(summary = "보유 캐릭터 목록 조회", description = "내가 보유한 캐릭터를 조회합니다. 등급별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<MemberCharacterListResponse>> getAllMemberCharacters(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "캐릭터 등급 (null이면 전체 조회)", required = false)
            @RequestParam(required = false) Rarity rarity
    ) {
        MemberCharacterListResponse response = memberCharacterService.getAllMemberCharacters(member.getId(), rarity);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "대표 캐릭터 조회", description = "현재 설정된 대표 캐릭터를 조회합니다.")
    @GetMapping("/default")
    public ResponseEntity<CommonResponse<MemberCharacterResponse>> getDefaultCharacter(
            @AuthenticationPrincipal Member member
    ) {
        MemberCharacterResponse response = memberCharacterService.getDefaultCharacter(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "대표 캐릭터 설정", description = "보유한 캐릭터 중 하나를 대표 캐릭터로 설정합니다.")
    @PutMapping("/default")
    public ResponseEntity<CommonResponse<MemberCharacterResponse>> setDefaultCharacter(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody SetDefaultCharacterRequest request
    ) {
        MemberCharacterResponse response = memberCharacterService.setDefaultCharacter(member.getId(), request);
        return HttpResponseUtil.updated(response);
    }
}