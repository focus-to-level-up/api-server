package com.studioedge.advertisement.component;

import com.studioedge.advertisement.entity.Advertisement;
import com.studioedge.advertisement.exception.AdvertisementNotFoundException;
import com.studioedge.advertisement.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdvertisementReader {
    private final AdvertisementRepository advertisementRepository;

    public Advertisement findOne(Long id) {
        log.info("Advertisement findOne: id={}", id);
        return advertisementRepository.findById(id)
                .orElseThrow(AdvertisementNotFoundException::new);
    }
}
