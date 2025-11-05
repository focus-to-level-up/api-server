package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByMemberId(Long memberId);
}
