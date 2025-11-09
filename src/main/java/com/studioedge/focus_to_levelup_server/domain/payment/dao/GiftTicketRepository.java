package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.GiftTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftTicketRepository extends JpaRepository<GiftTicket, Long> {
    List<GiftTicket> findAllByMemberIdAndIsUsedFalse(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
