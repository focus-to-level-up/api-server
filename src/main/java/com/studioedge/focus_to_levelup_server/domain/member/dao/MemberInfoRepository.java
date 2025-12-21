package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    // === Admin 통계용 쿼리 ===

    /**
     * 카테고리별 유저 수 조회
     */
    @Query("SELECT mi.categorySub, COUNT(mi) FROM MemberInfo mi " +
            "JOIN mi.member m WHERE m.status = 'ACTIVE' " +
            "GROUP BY mi.categorySub")
    List<Object[]> countByCategorySub();

    /**
     * 성별 유저 수 조회
     */
    @Query("SELECT mi.gender, COUNT(mi) FROM MemberInfo mi " +
            "JOIN mi.member m WHERE m.status = 'ACTIVE' " +
            "GROUP BY mi.gender")
    List<Object[]> countByGender();
}
