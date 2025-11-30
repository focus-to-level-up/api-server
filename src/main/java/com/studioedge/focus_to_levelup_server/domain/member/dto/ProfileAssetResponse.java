package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ProfileAssetResponse(
        @Schema(description = "에셋 pk", example = "1")
        Long assetId,
        @Schema(description = "맴버 에셋 pk", example = "3")
        Long memberAssetId,
        @Schema(description = "에셋 타입", example = "CHARACTER_IMAGE")
        AssetType type,
        @Schema(description = "에셋 이름", example = "기사 프로필 이미지")
        String name,
        @Schema(description = "에셋 이미지 url", example = "https://lvup-image.s3.ap-northeast-2.amazonaws.com/1629780000000.jpg")
        String assetUrl
) {
    public static ProfileAssetResponse of(MemberAsset memberAsset) {
        return ProfileAssetResponse.builder()
                .assetId(memberAsset.getAsset().getId())
                .memberAssetId(memberAsset.getId())
                .type(memberAsset.getAsset().getType())
                .name(memberAsset.getAsset().getName())
                .assetUrl(memberAsset.getAsset().getAssetUrl())
                .build();
    }
}
