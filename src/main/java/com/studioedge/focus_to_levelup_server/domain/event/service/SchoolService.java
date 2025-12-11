package com.studioedge.focus_to_levelup_server.domain.event.service;

import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.dto.SchoolResponse;
import com.studioedge.focus_to_levelup_server.domain.event.exception.EventUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchoolService {
    private final MemberInfoRepository memberInfoRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public Page<SchoolResponse> getRankingList(Member member, Pageable pageable) {
        MemberInfo memberInfo = memberInfoRepository.findByMember(member).orElseThrow(InvalidMemberException::new);
        if (!AppConstants.SCHOOL_CATEGORIES.contains(memberInfo.getCategoryMain())) {
            throw new EventUnAuthorizedException();
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "totalLevel")
        );
        return schoolRepository.findAll(sortedPageable)
                .map(school -> SchoolResponse.of(school, member.getMemberInfo().getSchool()));
    }
}
