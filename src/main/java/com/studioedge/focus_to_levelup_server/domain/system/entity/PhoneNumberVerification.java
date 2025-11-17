package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사전예약 전화번호 인증 정보
 * 개인정보 보호를 위해 Member 테이블과 분리
 * 이벤트 종료 후 일괄 삭제 예정
 */
@Entity
@Table(name = "phone_number_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumberVerification extends BaseEntity {

    // 사전예약 이벤트 종료일 (2025-12-31 23:59:59로 가정, 실제 날짜는 변경 가능)
    private static final LocalDateTime PRE_REGISTRATION_EVENT_END_DATE = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime deleteScheduledAt; // 삭제 예정 일시 (이벤트 종료일)

    @Column(nullable = false)
    private Boolean isVerified = true; // 사전예약 확인 완료 여부

    @Column(nullable = false)
    private String purpose = "PRE_REGISTRATION"; // 사용 목적 (추후 확장 가능)

    @Builder
    public PhoneNumberVerification(Member member, String phoneNumber, LocalDateTime deleteScheduledAt, Boolean isVerified, String purpose) {
        this.member = member;
        this.phoneNumber = phoneNumber;
        this.deleteScheduledAt = deleteScheduledAt;
        this.isVerified = isVerified != null ? isVerified : true;
        this.purpose = purpose != null ? purpose : "PRE_REGISTRATION";
    }

    /**
     * 사전예약용 전화번호 인증 생성 (이벤트 종료 시 삭제)
     */
    public static PhoneNumberVerification createForPreRegistration(Member member, String phoneNumber) {
        return PhoneNumberVerification.builder()
                .member(member)
                .phoneNumber(phoneNumber)
                .deleteScheduledAt(PRE_REGISTRATION_EVENT_END_DATE)
                .isVerified(true)
                .purpose("PRE_REGISTRATION")
                .build();
    }
}
