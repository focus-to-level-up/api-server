package com.studioedge.focus_to_levelup_server.domain.advertisement.dto;

import com.studioedge.focus_to_levelup_server.domain.advertisement.entity.Advertisement;

public record AdvertisementResponse(
        Long advertisementId,
        String imageUrl,
        String link
) {
    public static AdvertisementResponse from(Advertisement advertisement) {
        return new AdvertisementResponse(
                advertisement.getId(),
                advertisement.getImageUrl(),
                advertisement.getLink()
        );
    }
}
