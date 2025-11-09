package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 회원의 구독권 조회 (단건)
     */
    Optional<Subscription> findByMemberId(Long memberId);

    /**
     * 회원의 모든 활성화된 구독권 조회
     */
    List<Subscription> findAllByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(Long memberId);

    /**
     * 회원의 특정 타입 활성화된 구독권 조회
     */
    Optional<Subscription> findByMemberIdAndTypeAndIsActiveTrue(Long memberId, SubscriptionType type);

    /**
     * 회원의 특정 타입 구독권 존재 여부 (활성화 상태만)
     */
    boolean existsByMemberIdAndTypeAndIsActiveTrue(Long memberId, SubscriptionType type);

    /**
     * 만료된 구독권 조회 (배치 작업용)
     */
    @Query("SELECT s FROM Subscription s WHERE s.isActive = true AND s.endDate < :today")
    List<Subscription> findExpiredSubscriptions(@Param("today") LocalDate today);

    /**
     * 회원의 유효한 구독권 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscription s " +
            "WHERE s.member.id = :memberId AND s.isActive = true AND s.endDate >= :today")
    boolean hasValidSubscription(@Param("memberId") Long memberId, @Param("today") LocalDate today);

    /**
     * 회원의 모든 구독권 조회 (최신순)
     */
    List<Subscription> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 회원의 모든 구독권 삭제 (회원 탈퇴 시)
     */
    void deleteAllByMemberId(Long memberId);
}
