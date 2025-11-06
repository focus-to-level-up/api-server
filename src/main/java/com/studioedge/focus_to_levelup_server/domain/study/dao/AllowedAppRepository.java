package com.studioedge.focus_to_levelup_server.domain.study.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.study.entity.AllowedApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {
    List<AllowedApp> findAllByMember(Member member);

    List<AllowedApp> findAllByMemberId(Long memberId);
}
