package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.google.gson.Gson;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.KakaoTokenResponse;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.KakaoUserInfo;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.InvalidSocialTokenException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KakaoService {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    private final WebClient webClient;
    private final Gson gson;

    /**
     * Access Token으로 사용자 정보 조회 및 소셜 ID 추출
     */
    public String getSocialIdFromAccessToken(String accessToken) {
        try {
            String response = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to get Kakao user info, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            KakaoUserInfo userInfo = gson.fromJson(response, KakaoUserInfo.class);
            return String.valueOf(userInfo.getId());

        } catch (Exception e) {
            log.error("Failed to get Kakao user info from access token", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * Authorization Code로 Kakao Token 획득
     */
    public KakaoTokenResponse getKakaoTokenFromAuthorizationCode(String authorizationCode) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", authorizationCode);

            String response = webClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to get Kakao token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            return gson.fromJson(response, KakaoTokenResponse.class);

        } catch (Exception e) {
            log.error("Failed to get Kakao token from authorization code", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * 회원탈퇴 시 Kakao 연결 해제
     * Refresh Token으로 새 Access Token 발급 후 unlink
     */
    @Transactional
    public void unlinkKakaoAccount(Member member) {
        if (member.getKakaoRefreshToken() == null) {
            log.warn("Member {} has no Kakao refresh token to unlink", member.getId());
            return;
        }

        try {
            // 1. Refresh Token으로 새 Access Token 발급
            String accessToken = refreshAccessToken(member.getKakaoRefreshToken());

            // 2. Access Token으로 Unlink
            webClient.post()
                    .uri(KAKAO_UNLINK_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to unlink Kakao account, status: {}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully unlinked Kakao account for member {}", member.getId());

        } catch (Exception e) {
            log.error("Failed to unlink Kakao account", e);
        }
    }

    /**
     * Refresh Token으로 새 Access Token 발급
     */
    private String refreshAccessToken(String refreshToken) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("refresh_token", refreshToken);

            String response = webClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to refresh Kakao token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            KakaoTokenResponse tokenResponse = gson.fromJson(response, KakaoTokenResponse.class);
            return tokenResponse.getAccessToken();

        } catch (Exception e) {
            log.error("Failed to refresh Kakao access token", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * Kakao 회원가입
     * Authorization Code → Access Token + Refresh Token 교환 → 사용자 정보 조회
     * @return socialId와 refreshToken을 담은 DTO
     */
    public KakaoSignUpResult signUp(String authorizationCode) {
        // 1. Authorization Code로 Access Token + Refresh Token 교환
        KakaoTokenResponse tokenResponse = getKakaoTokenFromAuthorizationCode(authorizationCode);

        // 2. Access Token으로 사용자 정보 조회
        String socialId = getSocialIdFromAccessToken(tokenResponse.getAccessToken());

        return new KakaoSignUpResult(socialId, tokenResponse.getRefreshToken());
    }

    /**
     * Kakao 로그인
     * Access Token으로 사용자 정보 조회
     */
    public String signIn(String accessToken) {
        return getSocialIdFromAccessToken(accessToken);
    }

    /**
     * Kakao 회원가입 결과
     */
    public record KakaoSignUpResult(String socialId, String refreshToken) {}
}
