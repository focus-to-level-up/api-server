package com.studioedge.focus.repository;

import com.studioedge.focus.entity.Subject;
import com.studioedge.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findAllByMemberId(Long memberId);

    Optional<Subject> findByIdAndDeleteAtIsNull(Long id);

    List<Subject> findAllByMemberAndName(Member member, String name);

    List<Subject> findAllByMemberAndDeleteAtIsNull(Member member);

}
