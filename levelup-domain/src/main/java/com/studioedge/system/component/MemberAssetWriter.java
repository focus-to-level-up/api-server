package com.studioedge.system.component;

import com.studioedge.system.repository.MemberAssetRepository;
import com.studioedge.system.entity.MemberAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberAssetWriter {

    private final MemberAssetRepository memberAssetRepository;

    public MemberAsset save(MemberAsset memberAsset) {
        log.info("Saving member asset new");
        return memberAssetRepository.save(memberAsset);
    }
}
