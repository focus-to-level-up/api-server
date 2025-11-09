package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.BonusTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusTicketRepository extends JpaRepository<BonusTicket, Long> {
    List<BonusTicket> findAllByMemberIdAndIsActiveFalse(Long memberId);

    void deleteAllByMemberId(Long memberId);
}