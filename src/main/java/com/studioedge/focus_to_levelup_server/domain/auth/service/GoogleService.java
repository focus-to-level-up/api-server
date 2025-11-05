package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.google.gson.Gson;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.GooglePublicKey;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.GooglePublicKeys;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.GoogleTokenResponse;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.InvalidSocialTokenException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleService {

    private static final String GOOGLE_PUBLIC_KEYS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;
    private final Gson gson;

    /**
     * Google ID Token 검증 및 소셜 ID 추출
     */
    public String getSocialIdFromIdToken(String idToken) {
        try {
            // 1. Google 공개 키 목록 조회
            GooglePublicKeys googlePublicKeys = getGooglePublicKeys();

            // 2. ID Token 헤더 파싱
            String headerOfIdToken = idToken.substring(0, idToken.indexOf("."));
            Map<String, String> header = gson.fromJson(
                    new String(Base64.getUrlDecoder().decode(headerOfIdToken)),
                    Map.class
            );

            // 3. 매칭되는 공개 키 찾기
            GooglePublicKey matchedKey = googlePublicKeys.getKeys().stream()
                    .filter(key -> key.getKid().equals(header.get("kid")) && key.getAlg().equals(header.get("alg")))
                    .findFirst()
                    .orElseThrow(InvalidSocialTokenException::new);

            // 4. 공개 키로 ID Token 검증
            PublicKey publicKey = getPublicKey(matchedKey);
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            // 5. Audience 검증
            String audience = claims.getAudience().iterator().next();
            if (!clientId.equals(audience)) {
                log.error("Invalid Google ID Token audience: {}", audience);
                throw new InvalidSocialTokenException();
            }

            // 6. Social ID (sub claim) 반환
            log.info("Successfully verified Google ID Token for user: {}", claims.getSubject());
            return claims.getSubject();

        } catch (Exception e) {
            log.error("Failed to verify Google ID Token", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * Authorization Code로 Google Token 획득
     */
    public GoogleTokenResponse getGoogleTokenFromAuthorizationCode(String authorizationCode) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", authorizationCode);

            String response = webClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to get Google token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            return gson.fromJson(response, GoogleTokenResponse.class);

        } catch (Exception e) {
            log.error("Failed to get Google token from authorization code", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * 회원탈퇴 시 Google Token revoke
     * Refresh Token으로 새 Access Token 발급 후 revoke
     */
    @Transactional
    public void revokeGoogleToken(Member member) {
        if (member.getGoogleRefreshToken() == null) {
            log.warn("Member {} has no Google refresh token to revoke", member.getId());
            return;
        }

        try {
            // 1. Refresh Token으로 새 Access Token 발급
            String accessToken = refreshAccessToken(member.getGoogleRefreshToken());

            // 2. Access Token revoke
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("token", accessToken);

            webClient.post()
                    .uri(GOOGLE_REVOKE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to revoke Google token, status: {}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully revoked Google token for member {}", member.getId());

        } catch (Exception e) {
            log.error("Failed to revoke Google token", e);
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
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to refresh Google token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidSocialTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            GoogleTokenResponse tokenResponse = gson.fromJson(response, GoogleTokenResponse.class);
            return tokenResponse.getAccessToken();

        } catch (Exception e) {
            log.error("Failed to refresh Google access token", e);
            throw new InvalidSocialTokenException();
        }
    }

    /**
     * Google 공개 키 목록 조회
     */
    private GooglePublicKeys getGooglePublicKeys() {
        String response = webClient.get()
                .uri(GOOGLE_PUBLIC_KEYS_URL)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return gson.fromJson(response, GooglePublicKeys.class);
    }

    /**
     * Google Public Key로부터 RSA PublicKey 생성
     */
    private PublicKey getPublicKey(GooglePublicKey googlePublicKey) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(googlePublicKey.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(googlePublicKey.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new InvalidSocialTokenException();
        }
    }
}
