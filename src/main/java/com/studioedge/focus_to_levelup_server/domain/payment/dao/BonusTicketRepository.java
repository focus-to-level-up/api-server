package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.GiftTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BonusTicketRepository extends JpaRepository<GiftTicket, Long> {
    void deleteAllByMemberId(Long memberId);

    Optional<GiftTicket> findByMemberId(Long memberId);
    /**
     * 미사용 보너스 티켓 개수 조회
     */
    long countByMemberIdAndIsUsedFalse(Long memberId);

    /**
     * 미사용 보너스 티켓 목록 조회
     */
    List<GiftTicket> findByMemberIdAndIsUsedFalse(Long memberId);
}
