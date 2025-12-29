package com.studioedge.focus_to_levelup_server.domain.advertisement.dao;

import com.studioedge.focus_to_levelup_server.domain.advertisement.entity.Advertisement;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    /**
     * 균등 노출 로직:
     * 해당 카테고리(Sub)의 광고 중 '활성화' 상태인 것을 찾되,
     * '조회수(viewCount)'가 가장 낮은 순서로 정렬하여 1개만 가져옴.
     */
    Optional<Advertisement> findFirstByCategorySubsContainsAndIsActiveTrueOrderByViewCountAsc(CategorySubType category);

    /**
     * 조회수 증가 (Atomic Update)
     * 동시성 이슈 해결을 위해 DB에서 직접 +1 연산 수행
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Advertisement a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 클릭수 증가 (Atomic Update)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Advertisement a SET a.clickCount = a.clickCount + 1 WHERE a.id = :id")
    void incrementClickCount(@Param("id") Long id);
}
