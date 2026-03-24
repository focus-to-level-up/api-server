package com.studioedge.advertisement.component;

import com.studioedge.advertisement.entity.Advertisement;
import com.studioedge.advertisement.exception.AdvertisementNotFoundException;
import com.studioedge.advertisement.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdvertisementWriter {
    private final AdvertisementRepository advertisementRepository;

    public void increaseViewCount(Long advertisementId) {
        log.info("Increase view count for advertisement with id: {}", advertisementId);
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(AdvertisementNotFoundException::new);
        advertisement.increaseViewCount();
    }

    public void increaseClickCount(Long advertisementId) {
        log.info("Increase click count for advertisement with id: {}", advertisementId);
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(AdvertisementNotFoundException::new);
        advertisement.increaseClickCount();
    }
}
