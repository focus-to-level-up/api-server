package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import lombok.Builder;

@Builder
public record AssetRewardInfo(
        Long assetId,
        String name,
        String assetUrl
) {
    public static AssetRewardInfo from(Asset asset) {
        return AssetRewardInfo.builder()
                .assetId(asset.getId())
                .name(asset.getName())
                .assetUrl(asset.getAssetUrl())
                .build();
    }
}
