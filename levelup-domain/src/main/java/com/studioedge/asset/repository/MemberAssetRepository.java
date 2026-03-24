package com.studioedge.asset.repository;

import com.studioedge.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberAssetRepository extends JpaRepository<MemberAsset, Long> {

    @Query(value = "SELECT ma FROM MemberAsset ma JOIN FETCH ma.asset WHERE ma.member = :member",
            countQuery = "SELECT COUNT(ma) FROM MemberAsset ma WHERE ma.member = :member")
    Page<MemberAsset> findByMember(Member member, Pageable pageable);

    boolean existsByMemberIdAndAssetId(Long memberId, Long assetId);
}
