package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.google.gson.Gson;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.NaverTokenResponse;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.NaverUserInfo;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.InvalidSocialTokenException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NaverService {

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;
    private final Gson gson;

    /**
     * Access Token으로 사용자 정보 조회 및 소셜 ID 추출
     */
    public String getSocialIdFromAccessToken(String accessToken) {
        try {
            String response = webClient.get()
                    .uri(NAVER_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to get Naver user info, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            NaverUserInfo userInfo = gson.fromJson(response, NaverUserInfo.class);

            if (!"00".equals(userInfo.getResultCode())) {
                log.error("Naver API error: {}", userInfo.getMessage());
                throw new InvalidSocialTokenException();
            }

            return userInfo.getResponse().getId();

        } catch (Exception e) {
            log.error("Failed to get Naver user info from access token", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * Authorization Code로 Naver Token 획득
     */
    public NaverTokenResponse getNaverTokenFromAuthorizationCode(String authorizationCode, String state) {
        try {
            String url = NAVER_TOKEN_URL +
                    "?grant_type=authorization_code" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&redirect_uri=" + redirectUri +
                    "&code=" + authorizationCode +
                    "&state=" + state;

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to get Naver token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            return gson.fromJson(response, NaverTokenResponse.class);

        } catch (Exception e) {
            log.error("Failed to get Naver token from authorization code", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * 회원탈퇴 시 Naver Token revoke
     * Refresh Token으로 새 Access Token 발급 후 revoke
     */
    @Transactional
    public void revokeNaverToken(Member member) {
        if (member.getNaverRefreshToken() == null) {
            log.warn("Member {} has no Naver refresh token to revoke", member.getId());
            return;
        }

        try {
            // 1. Refresh Token으로 새 Access Token 발급
            String accessToken = refreshAccessToken(member.getNaverRefreshToken());

            // 2. Access Token revoke
            String revokeUrl = NAVER_TOKEN_URL +
                    "?grant_type=delete" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&access_token=" + accessToken;

            String response = webClient.get()
                    .uri(revokeUrl)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to revoke Naver token, status: {}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully revoked Naver token for member {}", member.getId());

        } catch (Exception e) {
            log.error("Failed to revoke Naver token", e);
        }
    }

    /**
     * Refresh Token으로 새 Access Token 발급
     */
    private String refreshAccessToken(String refreshToken) {
        try {
            String url = NAVER_TOKEN_URL +
                    "?grant_type=refresh_token" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&refresh_token=" + refreshToken;

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to refresh Naver token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            NaverTokenResponse tokenResponse = gson.fromJson(response, NaverTokenResponse.class);
            return tokenResponse.getAccessToken();

        } catch (Exception e) {
            log.error("Failed to refresh Naver access token", e);
            throw new InvalidSocialTokenException();
        }
    }
}
