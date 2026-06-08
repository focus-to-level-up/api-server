package com.studioedge.infra.redis.cache;

import java.time.Duration;

public interface GuildCacheClient {

    boolean isFocusRequestCooldownActive(Long guildId, Long requesterId, Long targetMemberId);

    void markFocusRequestSent(Long guildId, Long requesterId, Long targetMemberId, Duration ttl);
}
