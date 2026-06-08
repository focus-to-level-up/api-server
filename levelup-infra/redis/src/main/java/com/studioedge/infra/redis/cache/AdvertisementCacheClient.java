package com.studioedge.infra.redis.cache;

import java.time.Duration;

public interface AdvertisementCacheClient {

    boolean hasViewed(Long memberId);

    void markViewed(Long memberId, Duration ttl);
}
