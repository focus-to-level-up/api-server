package com.studioedge.system.component;

import com.studioedge.system.repository.MemberAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberAssetReader {

    private final MemberAssetRepository memberAssetRepository;

    public boolean exist(Long memberId, Long assetId) {
        log.info("Check if member with id {} owns asset with id {}", memberId, assetId);
        return memberAssetRepository.existsByMemberIdAndAssetId(memberId, assetId);
    }
}
