package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSettingRepository extends JpaRepository<MemberSetting, Long> {
    MemberSetting findByMember(Member member);
}
