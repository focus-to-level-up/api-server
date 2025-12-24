package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Mail m WHERE m.expiredAt < :now")
    void deleteByExpirationDateBefore(@Param("now") LocalDate now);

    /**
     * 특정 결제 로그와 연결된 우편 조회 (환불 시 사용)
     */
    List<Mail> findByPaymentLogId(Long paymentLogId);

    // 여러 유저의 특정 타입 메일을 날짜 기준으로 조회
    @Query("SELECT m FROM Mail m " +
            "WHERE m.receiver.id IN :receiverIds " +
            "AND m.type = :type " +
            "AND m.createdAt >= :startDate")
    List<Mail> findAllByReceiverIdInAndTypeAndCreatedAtAfter(
            @Param("receiverIds") List<Long> receiverIds,
            @Param("type") MailType type,
            @Param("startDate") LocalDateTime startDate
    );
}
