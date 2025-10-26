package com.studioedge.focus_to_levelup_server.global.jwt;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey key;
    private final MemberRepository memberRepository;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(Authentication authentication, long expiration, TokenType tokenType) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expiration);

        final Claims claims = Jwts.claims()
                .subject(authentication.getPrincipal().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .add("type", tokenType.getValue())
                .build();

        return Jwts.builder()
                .header().type("JWT").and()
                .claims(claims)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Access Token 전용 검증
     */
    public JwtValidationType validateAccessToken(String token) {
        try {
            final Claims claims = getBody(token);

            // 토큰 타입 확인
            String tokenType = claims.get("type", String.class);
            if (!TokenType.ACCESS.getValue().equals(tokenType)) {
                return JwtValidationType.INVALID_TOKEN_TYPE;
            }

            return JwtValidationType.VALID_JWT;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty", ex);
            return JwtValidationType.EMPTY_JWT;
        } catch (SignatureException ex) {
            log.error("JWT signature does not match", ex);
            return JwtValidationType.INVALID_JWT_TOKEN;
        }
    }

    /**
     * 일반 토큰 검증 (Refresh Token 포함)
     */
    public JwtValidationType validateToken(String token) {
        try {
            getBody(token);
            return JwtValidationType.VALID_JWT;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty", ex);
            return JwtValidationType.EMPTY_JWT;
        } catch (SignatureException ex) {
            log.error("JWT signature does not match", ex);
            return JwtValidationType.INVALID_JWT_TOKEN;
        }
    }

    /**
     * Refresh Token 여부 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            final Claims claims = getBody(token);
            String tokenType = claims.get("type", String.class);
            return TokenType.REFRESH.getValue().equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 Member ID 추출
     */
    public Long getMemberIdFromJwt(String token) {
        Claims claims = getBody(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * JWT 토큰에서 Member 엔티티 조회
     */
    public Member getMember(String token) {
        Long memberId = getMemberIdFromJwt(token);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));
    }

    /**
     * JWT Claims 추출
     */
    private Claims getBody(final String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
