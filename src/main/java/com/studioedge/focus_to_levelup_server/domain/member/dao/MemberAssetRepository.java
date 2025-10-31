package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAssetRepository extends JpaRepository<MemberAsset, Long> {
    Page<MemberAsset> findByMember(Member member, Pageable pageable);
}
