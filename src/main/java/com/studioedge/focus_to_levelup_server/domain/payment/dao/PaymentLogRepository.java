package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
    boolean existsByProductTransactionId(String productTransactionId);

    Optional<PaymentLog> findByProductTransactionId(String productTransactionId);

    List<PaymentLog> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT COUNT(pl) > 0 FROM PaymentLog pl " +
            "WHERE pl.member.id = :memberId " +
            "AND pl.product.type = :productType " +
            "AND pl.status = :status " +
            "AND pl.createdAt >= :startDate " +
            "AND pl.createdAt < :endDate")
    boolean existsByMemberIdAndProductTypeAndStatusAndCreatedAtBetween(
            @Param("memberId") Long memberId,
            @Param("productType") ProductType productType,
            @Param("status") PurchaseStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
