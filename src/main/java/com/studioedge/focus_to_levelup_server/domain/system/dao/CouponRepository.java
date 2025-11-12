package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * 쿠폰 코드로 조회
     */
    Optional<Coupon> findByCouponCode(String couponCode);

    /**
     * 활성화된 쿠폰 조회 (만료 제외)
     */
    @Query("SELECT c FROM Coupon c WHERE c.expiredAt >= :now")
    List<Coupon> findActiveCoupons(@Param("now") LocalDateTime now);
}
