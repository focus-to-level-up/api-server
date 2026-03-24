package com.studioedge.member.component;

import com.studioedge.member.entity.Member;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member findOne(Long memberId) {
        log.info("Finding member with ID: {}", memberId);
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }
}
