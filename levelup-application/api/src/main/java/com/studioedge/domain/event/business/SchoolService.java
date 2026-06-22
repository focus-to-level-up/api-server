package com.studioedge.domain.event.business;

import com.studioedge.event.repository.SchoolRepository;
import com.studioedge.domain.event.response.SchoolResponse;
import com.studioedge.event.exception.EventUnAuthorizedException;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.exception.MemberInfoInvalidException;
import com.studioedge.AppConstants;
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
        MemberInfo memberInfo = memberInfoRepository.findByMember(member).orElseThrow(MemberInfoInvalidException::new);
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
