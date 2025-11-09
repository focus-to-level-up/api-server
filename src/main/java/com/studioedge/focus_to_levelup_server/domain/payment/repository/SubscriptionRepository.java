package com.studioedge.focus_to_levelup_server.domain.payment.repository;

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
     * 유저의 구독 조회 (활성/비활성 무관)
     */
    Optional<Subscription> findByMemberId(Long memberId);

    /**
     * 유저의 활성 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.member.id = :memberId AND s.isActive = true")
    Optional<Subscription> findByMemberIdAndIsActiveTrue(@Param("memberId") Long memberId);

    /**
     * 유저의 모든 구독 이력 조회 (최신순)
     */
    @Query("SELECT s FROM Subscription s WHERE s.member.id = :memberId ORDER BY s.startDate DESC")
    List<Subscription> findAllByMemberIdOrderByStartDateDesc(@Param("memberId") Long memberId);

    /**
     * 유저가 활성 구독을 가지고 있는지 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.member.id = :memberId AND s.isActive = true")
    boolean existsByMemberIdAndIsActiveTrue(@Param("memberId") Long memberId);

    /**
     * 만료된 구독 조회 (배치 작업용)
     */
    @Query("SELECT s FROM Subscription s WHERE s.isActive = true AND s.endDate < :today")
    List<Subscription> findAllExpired(@Param("today") LocalDate today);

    /**
     * 특정 타입의 활성 구독 수 조회 (통계용)
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.type = :type AND s.isActive = true")
    long countByTypeAndIsActiveTrue(@Param("type") SubscriptionType type);
}