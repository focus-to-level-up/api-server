package com.studioedge.focus_to_levelup_server.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "revenuecat")
@Getter
@Setter
public class RevenueCatConfig {

    private boolean enabled = true;
    private ApiKey apiKey = new ApiKey();
    private String webhookAuthHeader;
    private String baseUrl = "https://api.revenuecat.com/v1";

    @Getter
    @Setter
    public static class ApiKey {
        private String apple;
        private String google;
    }

    /**
     * 플랫폼별 API Key 조회
     */
    public String getApiKeyForPlatform(String platform) {
        if ("APP_STORE".equalsIgnoreCase(platform) || "APPLE".equalsIgnoreCase(platform)) {
            return apiKey.getApple();
        } else if ("PLAY_STORE".equalsIgnoreCase(platform) || "GOOGLE".equalsIgnoreCase(platform)) {
            return apiKey.getGoogle();
        }
        return apiKey.getApple(); // 기본값
    }
}
