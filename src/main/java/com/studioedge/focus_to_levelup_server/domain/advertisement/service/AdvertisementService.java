package com.studioedge.focus_to_levelup_server.domain.advertisement.service;

import com.studioedge.focus_to_levelup_server.domain.advertisement.dao.AdvertisementRepository;
import com.studioedge.focus_to_levelup_server.domain.advertisement.dto.AdvertisementResponse;
import com.studioedge.focus_to_levelup_server.domain.advertisement.entity.Advertisement;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    /**
     * 앱 실행 시 노출할 광고 1개 조회
     */
    @Transactional
    public AdvertisementResponse getAdvertisement(Member member) {
        // 1. 유저의 Sub 카테고리 확인
        MemberInfo memberInfo = member.getMemberInfo();
        if (memberInfo == null || memberInfo.getCategorySub() == null) {
            throw new InvalidMemberException();
        }
        CategorySubType subCategory = memberInfo.getCategorySub();

        // 2. 해당 카테고리에서 노출 횟수가 가장 적은 광고 조회 (균등 노출)
        Optional<Advertisement> adOptional = advertisementRepository
                .findFirstByCategorySubsContainsAndIsActiveTrueOrderByViewCountAsc(subCategory);

        // 3. 광고가 없으면 null 반환 (클라이언트에서 노출 안 함 처리)
        if (adOptional.isEmpty()) {
            return null;
        }

        Advertisement advertisement = adOptional.get();

        // 4. 노출 수(View Count) 증가 (DB 직접 업데이트로 동시성 방어)
        advertisementRepository.incrementViewCount(advertisement.getId());

        // 5. 응답 반환
        return AdvertisementResponse.from(advertisement);
    }

    /**
     * 광고 클릭 시 호출
     */
    @Transactional
    public void clickAdvertisement(Long advertisementId) {
        advertisementRepository.incrementClickCount(advertisementId);
    }
}
