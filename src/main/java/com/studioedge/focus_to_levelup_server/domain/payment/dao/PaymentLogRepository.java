package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
    boolean existsByProductTransactionId(String productTransactionId);

    Optional<PaymentLog> findByProductTransactionId(String productTransactionId);

    List<PaymentLog> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
