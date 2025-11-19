package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long> {

    /**
     * 유저의 미수령 우편 조회 (만료 제외)
     */
    @Query("SELECT m FROM Mail m WHERE m.receiver.id = :memberId " +
           "AND m.isReceived = false " +
           "AND m.expiredAt >= :today " +
           "ORDER BY m.createdAt DESC")
    List<Mail> findActiveMailsByMemberId(@Param("memberId") Long memberId,
                                          @Param("today") LocalDate today);

    /**
     * 유저의 전체 우편 조회 (수령 여부 무관, 만료 제외)
     */
    @Query("SELECT m FROM Mail m WHERE m.receiver.id = :memberId " +
           "AND m.expiredAt >= :today " +
           "ORDER BY m.createdAt DESC")
    List<Mail> findAllMailsByMemberId(@Param("memberId") Long memberId,
                                       @Param("today") LocalDate today);

    /**
     * 만료된 우편 조회 (배치용)
     */
    @Query("SELECT m FROM Mail m WHERE m.expiredAt < CURRENT_DATE")
    Page<Mail> findExpiredMails(Pageable pageable);

    /**
     * 특정 결제 로그와 연결된 우편 조회 (환불 시 사용)
     */
    List<Mail> findByPaymentLogId(Long paymentLogId);
}
