package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.AllowedApp;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {
    List<AllowedApp> findAllByMember(Member member);
}
