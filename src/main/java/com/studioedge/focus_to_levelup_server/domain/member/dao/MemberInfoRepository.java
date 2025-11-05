package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findByMember(Member member);

    @Query("SELECT mi FROM MemberInfo mi " +
            "JOIN FETCH mi.member m " +
            "LEFT JOIN FETCH mi.profileImage mpi " +
            "LEFT JOIN FETCH mpi.asset " +
            "LEFT JOIN FETCH mi.profileBorder mbi " +
            "LEFT JOIN FETCH mbi.asset " +
            "WHERE m.id = :memberId")
    Optional<MemberInfo> findByMemberId(Long memberId);
}
