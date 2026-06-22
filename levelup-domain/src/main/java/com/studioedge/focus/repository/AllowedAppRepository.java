package com.studioedge.focus.repository;

import com.studioedge.focus.entity.AllowedApp;
import com.studioedge.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {
    List<AllowedApp> findAllByMember(Member member);

    List<AllowedApp> findAllByMemberId(Long memberId);
    Optional<AllowedApp> findByMemberIdAndAppIdentifier(Long memberId, String appIdentifier);
}
