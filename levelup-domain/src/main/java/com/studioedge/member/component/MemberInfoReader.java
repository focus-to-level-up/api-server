package com.studioedge.member.component;

import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.exception.MemberInfoInvalidException;
import com.studioedge.member.repository.MemberInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberInfoReader {

    private final MemberInfoRepository memberInfoRepository;

    public MemberInfo findOne(Long memberId) {
        log.info("Finding memberInfo with ID: {}", memberId);
        return memberInfoRepository.findByMemberId(memberId).orElseThrow(MemberInfoInvalidException::new);
    }
}
