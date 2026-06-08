package com.studioedge.domain.advertisement.business;

import com.studioedge.advertisement.component.AdvertisementReader;
import com.studioedge.advertisement.component.AdvertisementWriter;
import com.studioedge.advertisement.entity.Advertisement;
import com.studioedge.common.enums.CategorySubType;
import com.studioedge.domain.advertisement.response.AdvertisementResponse;
import com.studioedge.infra.redis.cache.AdvertisementCacheClient;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.exception.MemberInfoInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private static final Duration AD_COOLDOWN = Duration.ofHours(3);

    private final AdvertisementReader advertisementReader;
    private final AdvertisementWriter advertisementWriter;
    private final AdvertisementCacheClient advertisementCacheClient;

    /**
     * 앱 실행 시 노출할 광고 1개 조회
     */
    @Transactional
    public AdvertisementResponse getAdvertisement(Member member) {
        if (advertisementCacheClient.hasViewed(member.getId())) {
            return null;
        }

        // 1. 유저의 Sub 카테고리 확인
        MemberInfo memberInfo = member.getMemberInfo();
        if (memberInfo == null || memberInfo.getCategorySub() == null) {
            throw new MemberInfoInvalidException();
        }
        CategorySubType subCategory = memberInfo.getCategorySub();

        // 2. 해당 카테고리에서 노출 횟수가 가장 적은 광고 조회 (균등 노출)
        Optional<Advertisement> adOptional = advertisementReader
                .findFirstActiveByCategorySubOrderByViewCount(subCategory);

        // 3. 광고가 없으면 null 반환 (클라이언트에서 노출 안 함 처리)
        if (adOptional.isEmpty()) {
            return null;
        }

        Advertisement advertisement = adOptional.get();

        // 4. 노출 수(View Count) 증가 (DB 직접 업데이트로 동시성 방어)
        advertisementWriter.increaseViewCount(advertisement.getId());

        advertisementCacheClient.markViewed(member.getId(), AD_COOLDOWN);

        // 5. 응답 반환
        return AdvertisementResponse.from(advertisement);
    }

    /**
     * 광고 클릭 시 호출
     */
    @Transactional
    public void clickAdvertisement(Long advertisementId) {
        advertisementWriter.increaseClickCount(advertisementId);
    }
}
