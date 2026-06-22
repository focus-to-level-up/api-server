package com.studioedge.domain.system.dto.response;

import com.studioedge.system.entity.Asset;
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
