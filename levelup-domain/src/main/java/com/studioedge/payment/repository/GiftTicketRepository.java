package com.studioedge.payment.repository;

import com.studioedge.payment.entity.GiftTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftTicketRepository extends JpaRepository<GiftTicket, Long> {
    void deleteAllByMemberId(Long memberId);
}
