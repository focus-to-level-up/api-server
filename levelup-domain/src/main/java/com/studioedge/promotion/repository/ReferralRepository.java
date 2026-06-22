package com.studioedge.promotion.repository;

import com.studioedge.promotion.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    boolean existsByInviteeId(Long inviteeId);
}
