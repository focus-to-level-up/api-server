package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.google.gson.Gson;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.ApplePublicKey;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.ApplePublicKeys;
import com.studioedge.focus_to_levelup_server.domain.auth.dto.AppleTokenResponse;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.InvalidAppleTokenException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AppleService {

    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.private-key}")
    private String privateKey;

    private final WebClient webClient;
    private final Gson gson;

    /**
     * Apple Identity Token 검증 및 소셜 ID 추출
     */
    public String getSocialIdFromIdentityToken(String identityToken) {
        try {
            // 1. Apple 공개 키 목록 조회
            ApplePublicKeys applePublicKeys = getApplePublicKeys();

            // 2. Identity Token 헤더 파싱
            String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
            Map<String, String> header = gson.fromJson(
                    new String(Base64.getUrlDecoder().decode(headerOfIdentityToken)),
                    Map.class
            );

            // 3. 매칭되는 공개 키 찾기
            ApplePublicKey matchedKey = applePublicKeys.getKeys().stream()
                    .filter(key -> key.getKid().equals(header.get("kid")) && key.getAlg().equals(header.get("alg")))
                    .findFirst()
                    .orElseThrow(InvalidAppleTokenException::new);

            // 4. 공개 키로 Identity Token 검증
            PublicKey publicKey = getPublicKey(matchedKey);
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            // 5. Social ID (sub claim) 반환
            return claims.getSubject();

        } catch (Exception e) {
            log.error("Failed to verify Apple Identity Token", e);
            throw new InvalidAppleTokenException();
        }
    }

    /**
     * Authorization Code로 Apple Token 획득 (회원가입 시 사용)
     */
    public AppleTokenResponse getAppleTokenFromAuthorizationCode(String authorizationCode) {
        try {
            String clientSecret = generateClientSecret();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", authorizationCode);
            params.add("grant_type", "authorization_code");

            String response = webClient.post()
                    .uri(APPLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> {
                        log.error("Failed to get Apple token, status: {}", clientResponse.statusCode());
                        return Mono.error(new InvalidAppleTokenException());
                    })
                    .bodyToMono(String.class)
                    .block();

            return gson.fromJson(response, AppleTokenResponse.class);

        } catch (Exception e) {
            log.error("Failed to get Apple token from authorization code", e);
            throw new InvalidAppleTokenException();
        }
    }

    /**
     * 회원탈퇴 시 Apple Refresh Token revoke
     */
    @Transactional
    public void revokeAppleToken(Member member) {
        if (member.getRefreshToken() == null) {
            log.warn("Member {} has no Apple refresh token to revoke", member.getMemberId());
            return;
        }

        try {
            String clientSecret = generateClientSecret();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("token", member.getRefreshToken());
            params.add("token_type_hint", "refresh_token");

            webClient.post()
                    .uri(APPLE_REVOKE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> {
                        log.error("Failed to revoke Apple token, status: {}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully revoked Apple token for member {}", member.getMemberId());

        } catch (Exception e) {
            log.error("Failed to revoke Apple token", e);
        }
    }

    /**
     * Apple 공개 키 목록 조회
     */
    private ApplePublicKeys getApplePublicKeys() {
        String response = webClient.get()
                .uri(APPLE_PUBLIC_KEYS_URL)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return gson.fromJson(response, ApplePublicKeys.class);
    }

    /**
     * Apple Public Key로부터 RSA PublicKey 생성
     */
    private PublicKey getPublicKey(ApplePublicKey applePublicKey) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new InvalidAppleTokenException();
        }
    }

    /**
     * Client Secret 생성 (JWT 형태)
     */
    private String generateClientSecret() {
        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + 3600000); // 1시간

            return Jwts.builder()
                    .header().keyId(keyId).and()
                    .issuer(teamId)
                    .issuedAt(now)
                    .expiration(expirationDate)
                    .audience().add("https://appleid.apple.com").and()
                    .subject(clientId)
                    .signWith(getPrivateKey(privateKey), Jwts.SIG.ES256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate client secret", e);
            throw new InvalidAppleTokenException();
        }
    }

    /**
     * Private Key 생성
     */
    private PrivateKey getPrivateKey(String privateKeyContent) {
        try {
            // Remove header, footer, and whitespace
            String privateKeyPEM = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failed to load private key", e);
            throw new InvalidAppleTokenException();
        }
    }
}
