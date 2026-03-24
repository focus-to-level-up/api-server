package com.studioedge.advertisement.repository;

import com.studioedge.advertisement.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    Optional<Advertisement> findFirstByCategorySubsContainsAndIsActiveTrueOrderByViewCountAsc(CategorySubType category);
}
