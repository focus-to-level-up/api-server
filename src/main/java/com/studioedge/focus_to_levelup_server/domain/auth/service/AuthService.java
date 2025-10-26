package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.studioedge.focus_to_levelup_server.domain.auth.dto.*;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.entity.SocialType;
import com.studioedge.focus_to_levelup_server.domain.member.repository.MemberRepository;
import com.studioedge.focus_to_levelup_server.global.jwt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // 토큰 만료 시간 (밀리초)
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000L;    // 24시간
    private static final long REFRESH_TOKEN_EXPIRATION = 1209600000L; // 14일

    private final AppleService appleService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 (Apple)
     */
    @Transactional
    public LoginResponse signIn(LoginRequest request, SocialType socialType) {
        // 1. Apple Identity Token 검증 및 소셜 ID 추출
        String socialId = getSocialIdBySocialType(request.getIdentityToken(), socialType);

        // 2. 사용자 조회
        Member member = memberRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseThrow(UserNotRegisteredException::new);

        // 3. FCM 토큰 업데이트 (있는 경우)
        if (request.getFcmToken() != null) {
            member.setFcmToken(request.getFcmToken());
        }

        // 4. JWT 토큰 생성
        Token token = generateToken(member.getMemberId());

        // 5. Refresh Token DB 저장
        member.setRefreshToken(token.getRefreshToken());

        // 6. 프로필 완성 여부 확인 (닉네임이 있으면 완성된 것으로 간주)
        boolean isProfileCompleted = member.getNickname() != null;

        return LoginResponse.of(token, isProfileCompleted);
    }

    /**
     * 회원가입 (Apple)
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request, SocialType socialType) {
        // 1. Authorization Code로 Apple Token 획득 (Apple Refresh Token 포함)
        AppleTokenResponse appleTokenResponse = getAppleTokenBySocialType(
                request.getAuthorizationCode(),
                socialType
        );

        // 2. Identity Token 검증 및 소셜 ID 추출
        String socialId = getSocialIdBySocialType(request.getIdentityToken(), socialType);

        // 3. 기존 회원 여부 확인
        Member member = memberRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElse(null);

        if (member == null) {
            // 신규 회원 생성
            member = Member.builder()
                    .socialType(socialType)
                    .socialId(socialId)
                    .fcmToken(request.getFcmToken())
                    .status(MemberStatus.ACTIVE)
                    .build();
            memberRepository.save(member);
            log.info("New member created: memberId={}, socialType={}", member.getMemberId(), socialType);
        } else {
            // 기존 회원 정보 업데이트 (탈퇴 후 재가입 케이스 등)
            if (member.getStatus() == MemberStatus.WITHDRAWN) {
                // 탈퇴 상태였다면 활성화
                member = Member.builder()
                        .socialType(socialType)
                        .socialId(socialId)
                        .fcmToken(request.getFcmToken())
                        .status(MemberStatus.ACTIVE)
                        .build();
                memberRepository.save(member);
            }
            if (request.getFcmToken() != null) {
                member.setFcmToken(request.getFcmToken());
            }
            log.info("Existing member updated: memberId={}", member.getMemberId());
        }

        // 4. Apple Refresh Token 저장 (회원탈퇴 시 필요)
        member.setRefreshToken(appleTokenResponse.getRefreshToken());

        // 5. JWT 토큰 생성
        Token token = generateToken(member.getMemberId());

        // 6. JWT Refresh Token으로 교체 (Apple Refresh Token과 구분)
        member.setRefreshToken(token.getRefreshToken());

        return SignUpResponse.of(token);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenRefreshResponse refresh(String refreshToken) {
        // 1. Refresh Token 타입 검증
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenTypeException();
        }

        // 2. Refresh Token 유효성 검증
        JwtValidationType validationType = jwtTokenProvider.validateToken(refreshToken);
        if (validationType != JwtValidationType.VALID_JWT) {
            throw new RefreshTokenExpiredException();
        }

        // 3. 사용자 조회
        Long memberId = jwtTokenProvider.getMemberIdFromJwt(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));

        // 4. DB의 Refresh Token과 일치 여부 확인
        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new TokenMismatchException();
        }

        // 5. 새로운 Access Token 생성
        UserAuthentication authentication = new UserAuthentication(
                memberId,
                null,
                Collections.emptyList()
        );
        String newAccessToken = jwtTokenProvider.generateToken(
                authentication,
                ACCESS_TOKEN_EXPIRATION,
                TokenType.ACCESS
        );

        return TokenRefreshResponse.of(newAccessToken);
    }

    /**
     * 회원탈퇴
     */
    @Transactional
    public void resign(Long memberId, SocialType socialType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));

        // 1. Apple Token revoke
        if (socialType == SocialType.APPLE) {
            appleService.revokeAppleToken(member);
        }

        // 2. 회원 탈퇴 처리
        member.withdraw();

        log.info("Member resigned: memberId={}, socialType={}", memberId, socialType);
    }

    /**
     * 소셜 타입별 소셜 ID 추출
     */
    private String getSocialIdBySocialType(String identityToken, SocialType socialType) {
        return switch (socialType) {
            case APPLE -> appleService.getSocialIdFromIdentityToken(identityToken);
            // TODO: 다른 소셜 로그인 추가
            // case KAKAO -> kakaoService.getSocialIdFromToken(identityToken);
            // case NAVER -> naverService.getSocialIdFromToken(identityToken);
            // case GOOGLE -> googleService.getSocialIdFromToken(identityToken);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 타입입니다: " + socialType);
        };
    }

    /**
     * 소셜 타입별 Token 획득
     */
    private AppleTokenResponse getAppleTokenBySocialType(String authorizationCode, SocialType socialType) {
        return switch (socialType) {
            case APPLE -> appleService.getAppleTokenFromAuthorizationCode(authorizationCode);
            // TODO: 다른 소셜 로그인 추가
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 타입입니다: " + socialType);
        };
    }

    /**
     * JWT 토큰 생성 (Access + Refresh)
     */
    private Token generateToken(Long memberId) {
        UserAuthentication authentication = new UserAuthentication(
                memberId,
                null,
                Collections.emptyList()
        );

        String accessToken = jwtTokenProvider.generateToken(
                authentication,
                ACCESS_TOKEN_EXPIRATION,
                TokenType.ACCESS
        );

        String refreshToken = jwtTokenProvider.generateToken(
                authentication,
                REFRESH_TOKEN_EXPIRATION,
                TokenType.REFRESH
        );

        return Token.of(accessToken, refreshToken);
    }
}
