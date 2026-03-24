package com.studioedge.domain.advertisement.response;

import com.studioedge.advertisement.entity.Advertisement;

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
