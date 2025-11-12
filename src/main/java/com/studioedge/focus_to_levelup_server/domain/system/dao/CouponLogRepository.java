package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.CouponLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponLogRepository extends JpaRepository<CouponLog, Long> {

    /**
     * 특정 유저가 특정 쿠폰을 사용했는지 확인
     */
    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    /**
     * 유저의 쿠폰 사용 내역 조회
     */
    List<CouponLog> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
