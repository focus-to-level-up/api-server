package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findByMember(Member member);

    Optional<MemberInfo> findByMemberId(Long memberId);
}
