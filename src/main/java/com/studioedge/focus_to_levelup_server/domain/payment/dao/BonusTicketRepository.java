package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.BonusTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BonusTicketRepository extends JpaRepository<BonusTicket, Long> {
    void deleteAllByMemberId(Long memberId);

    Optional<BonusTicket> findByMemberId(Long memberId);
    /**
     * 미사용(비활성) 보너스 티켓 개수 조회
     */
    long countByMemberIdAndIsActiveFalse(Long memberId);

    /**
     * 미사용(비활성) 보너스 티켓 목록 조회
     */
    List<BonusTicket> findByMemberIdAndIsActiveFalse(Long memberId);
}
