package com.studioedge.focus_to_levelup_server.domain.promotion.dao;

import com.studioedge.focus_to_levelup_server.domain.promotion.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    boolean existsByInviteeId(Long inviteeId);
}
