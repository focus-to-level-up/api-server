package com.studioedge.event.repository;

import com.studioedge.event.entity.PhoneNumberVerification;
import com.studioedge.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneNumberVerificationRepository extends JpaRepository<PhoneNumberVerification, Long> {

    /**
     * 해시된 전화번호로 인증 정보 조회
     */
    Optional<PhoneNumberVerification> findByHashedPhoneNumber(String hashedPhoneNumber);

    /**
     * 회원으로 인증 정보 조회
     */
    Optional<PhoneNumberVerification> findByMember(Member member);

    /**
     * 이벤트 종료 후 모든 전화번호 인증 정보 일괄 삭제
     * 주의: 수동 실행 필요 (스케줄러 또는 관리자 API 호출)
     */
    @Modifying
    @Query("DELETE FROM PhoneNumberVerification p WHERE p.purpose = 'PRE_REGISTRATION'")
    int deleteAllPreRegistrationVerifications();

    /**
     * 해시된 전화번호로 인증 정보 존재 여부 확인
     */
    boolean existsByHashedPhoneNumber(String hashedPhoneNumber);
}
