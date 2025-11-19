package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.BonusTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BonusTicketRepository extends JpaRepository<BonusTicket, Long> {
    void deleteAllByMemberId(Long memberId);

    Optional<BonusTicket> findByMemberId(Long memberId);
}