package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.GiftTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftTicketRepository extends JpaRepository<GiftTicket, Long> {
    void deleteAllByMemberId(Long memberId);
}
