package com.studioedge.infra.redis.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisAdvertisementCacheClient implements AdvertisementCacheClient {

    private static final String AD_EXPOSURE_KEY_PREFIX = "ad:exposure:member:";
    private static final String VIEWED_VALUE = "true";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean hasViewed(Long memberId) {
        return redisTemplate.opsForValue().get(getExposureKey(memberId)) != null;
    }

    @Override
    public void markViewed(Long memberId, Duration ttl) {
        redisTemplate.opsForValue().set(getExposureKey(memberId), VIEWED_VALUE, ttl);
    }

    private String getExposureKey(Long memberId) {
        return AD_EXPOSURE_KEY_PREFIX + memberId;
    }
}
