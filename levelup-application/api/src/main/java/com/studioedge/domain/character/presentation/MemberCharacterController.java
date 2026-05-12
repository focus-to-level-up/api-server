package com.studioedge.domain.character.presentation;

import com.studioedge.character.enums.Rarity;
import com.studioedge.domain.character.business.MemberCharacterCommandService;
import com.studioedge.domain.character.business.MemberCharacterQueryService;
import com.studioedge.domain.character.business.TrainingRewardCommandService;
import com.studioedge.domain.character.business.TrainingRewardQueryService;
import com.studioedge.domain.character.request.SetDefaultCharacterRequest;
import com.studioedge.domain.character.response.ClaimTrainingRewardResponse;
import com.studioedge.domain.character.response.MemberCharacterListResponse;
import com.studioedge.domain.character.response.MemberCharacterResponse;
import com.studioedge.domain.character.response.TrainingRewardResponse;
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

@Tag(name = "Member Character", description = "보유 캐릭터 API")
@RestController
@RequestMapping("/api/v1/members/me/characters")
@RequiredArgsConstructor
public class MemberCharacterController {

    private final MemberCharacterQueryService memberCharacterQueryService;
    private final MemberCharacterCommandService memberCharacterCommandService;
    private final TrainingRewardCommandService trainingRewardCommandService;
    private final TrainingRewardQueryService trainingRewardQueryService;

    @Operation(summary = "보유 캐릭터 목록 조회", description = "내가 보유한 캐릭터를 조회합니다. 등급별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<MemberCharacterListResponse>> getAllMemberCharacters(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "캐릭터 등급 (null이면 전체 조회)", required = false)
            @RequestParam(required = false) Rarity rarity
    ) {
        MemberCharacterListResponse response = memberCharacterQueryService.getAllMemberCharacters(member.getId(), rarity);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "대표 캐릭터 조회", description = "현재 설정된 대표 캐릭터를 조회합니다.")
    @GetMapping("/default")
    public ResponseEntity<CommonResponse<MemberCharacterResponse>> getDefaultCharacter(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "맴버 pk")
            @RequestParam(required = false) Long memberId
    ) {
        MemberCharacterResponse response = memberCharacterQueryService.getDefaultCharacter(memberId == null ? member.getId() : memberId);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "캐릭터 진화", description = "진화시키고자하는 캐릭터를 진화시킵니다.")
    @PostMapping("/evolution/{memberCharacterId}")
    public ResponseEntity<CommonResponse<Void>> evolveCharacter(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "진화시키고자하는 캐릭터 pk", required = true)
            @PathVariable Long memberCharacterId,
            @Parameter(description = "가속하기 여부", required = true)
            @RequestParam Boolean doFastEvolution
    ) {
        memberCharacterCommandService.evolveCharacter(member.getId(), memberCharacterId, doFastEvolution);
        return HttpResponseUtil.ok(null);
    }

    @Operation(summary = "대표 캐릭터 설정", description = "보유한 캐릭터 중 하나를 대표 캐릭터로 설정합니다.")
    @PutMapping("/default")
    public ResponseEntity<CommonResponse<MemberCharacterResponse>> setDefaultCharacter(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody SetDefaultCharacterRequest request
    ) {
        MemberCharacterResponse response = memberCharacterCommandService.setDefaultCharacter(member.getId(), request);
        return HttpResponseUtil.updated(response);
    }

    @Operation(summary = "훈련 보상 조회", description = "현재 적립된 훈련 보상을 조회합니다.")
    @GetMapping("/training-reward")
    public ResponseEntity<CommonResponse<TrainingRewardResponse>> getTrainingReward(
            @AuthenticationPrincipal Member member
    ) {
        int accumulatedReward = trainingRewardQueryService.getAccumulatedReward(member.getId());
        return HttpResponseUtil.ok(TrainingRewardResponse.of(accumulatedReward));
    }

    @Operation(summary = "훈련 보상 수령", description = "적립된 훈련 보상을 다이아로 수령합니다. 60분×시급 = 1다이아")
    @PostMapping("/training-reward/claim")
    public ResponseEntity<CommonResponse<ClaimTrainingRewardResponse>> claimTrainingReward(
            @AuthenticationPrincipal Member member
    ) {
        int claimedDiamond = trainingRewardCommandService.claimTrainingReward(member.getId());
        int remainingReward = trainingRewardQueryService.getAccumulatedReward(member.getId());
        return HttpResponseUtil.ok(ClaimTrainingRewardResponse.of(claimedDiamond, remainingReward));
    }
}
