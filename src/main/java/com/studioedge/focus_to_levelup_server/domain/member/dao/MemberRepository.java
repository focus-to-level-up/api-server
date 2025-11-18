package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByNickname(String nickname);

    Page<Member> findAllByIsFocusingIsTrue(Pageable pageable);
    Optional<Member> findByNickname(String nickname);
}
