package com.studioedge.infra.redis.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisGuildCacheClient implements GuildCacheClient {

    private static final String FOCUS_REQUEST_KEY_PREFIX = "focus-request:";
    private static final String REQUESTED_VALUE = "1";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isFocusRequestCooldownActive(Long guildId, Long requesterId, Long targetMemberId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getFocusRequestKey(guildId, requesterId, targetMemberId)));
    }

    @Override
    public void markFocusRequestSent(Long guildId, Long requesterId, Long targetMemberId, Duration ttl) {
        redisTemplate.opsForValue().set(getFocusRequestKey(guildId, requesterId, targetMemberId), REQUESTED_VALUE, ttl);
    }

    private String getFocusRequestKey(Long guildId, Long requesterId, Long targetMemberId) {
        return FOCUS_REQUEST_KEY_PREFIX + guildId + ":" + requesterId + ":" + targetMemberId;
    }
}
