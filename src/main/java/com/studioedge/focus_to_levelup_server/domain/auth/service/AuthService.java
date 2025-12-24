package com.studioedge.focus_to_levelup_server.domain.auth.service;

import com.studioedge.focus_to_levelup_server.domain.auth.dto.*;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.global.jwt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    // 토큰 만료 시간 (밀리초)
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final AppleService appleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final GoogleService googleService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse signIn(LoginRequest request, SocialType socialType) {
        // 1. 각 플랫폼 Service에서 소셜 ID 조회
        String socialId = switch (socialType) {
            case APPLE -> appleService.getSocialIdFromIdentityToken(request.getIdentityToken());
            case KAKAO -> kakaoService.signIn(request.getIdentityToken());
            case NAVER -> naverService.getSocialIdFromAccessToken(request.getIdentityToken());
            case GOOGLE -> googleService.getSocialIdFromIdToken(request.getIdentityToken());
        };

        // 2. 사용자 조회
        Member member = memberRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseThrow(UserNotRegisteredException::new);

        // 3. 탈퇴한 사용자 체크
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new WithdrawnMemberException();
        }

        // 4. FCM 토큰 업데이트
        if (request.getFcmToken() != null) {
            member.setFcmToken(request.getFcmToken());
        }

        // 5. JWT 토큰 생성 및 저장
        Token token = generateToken(member.getId());
        member.setRefreshToken(token.getRefreshToken());

        // 6. 프로필 완성 여부 확인
        boolean isProfileCompleted = member.getNickname() != null;

        return LoginResponse.of(token, isProfileCompleted);
    }

    /**
     * 회원가입 (분기점)
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request, SocialType socialType) {
        return switch (socialType) {
            case APPLE -> signUpApple(request);
            case KAKAO -> signUpKakao(request);
            case NAVER -> signUpNaver(request);
            case GOOGLE -> signUpGoogle(request);
        };
    }

    /**
     * Kakao 회원가입
     * Authorization Code 방식과 Token 방식(Flutter SDK용) 모두 지원
     */
    @Transactional
    public SignUpResponse signUpKakao(SignUpRequest request) {
        KakaoService.KakaoSignUpResult result;

        // 1. KakaoService에서 회원가입 처리
        if (request.getAccessToken() != null && request.getRefreshToken() != null) {
            // Token 방식 (Flutter SDK 등 클라이언트에서 직접 토큰 발급)
            result = kakaoService.signUpWithTokens(request.getAccessToken(), request.getRefreshToken());
        } else if (request.getAuthorizationCode() != null) {
            // Authorization Code 방식 (서버에서 토큰 교환)
            result = kakaoService.signUp(request.getAuthorizationCode());
        } else {
            throw new IllegalArgumentException("Kakao 회원가입에는 authorizationCode 또는 (accessToken + refreshToken)이 필요합니다.");
        }

        // 2. 회원 생성 또는 조회
        Member member = findOrCreateMember(SocialType.KAKAO, result.socialId(), request.getFcmToken());

        // 3. Kakao Refresh Token 저장
        member.setKakaoRefreshToken(result.refreshToken());

        // 4. JWT 토큰 생성 및 저장
        Token token = generateToken(member.getId());
        member.setRefreshToken(token.getRefreshToken());

        return SignUpResponse.of(token);
    }

    /**
     * Apple 회원가입
     */
    @Transactional
    public SignUpResponse signUpApple(SignUpRequest request) {
        // 1. Identity Token 검증 및 소셜 ID 추출
        String socialId = appleService.getSocialIdFromIdentityToken(request.getIdentityToken());

        // 2. Authorization Code로 Refresh Token 교환
        String refreshToken = appleService.getAppleTokenFromAuthorizationCode(
                request.getAuthorizationCode()
        ).getRefreshToken();

        // 3. 회원 생성 또는 조회
        Member member = findOrCreateMember(SocialType.APPLE, socialId, request.getFcmToken());

        // 4. Apple Refresh Token 저장
        member.setAppleRefreshToken(refreshToken);

        // 5. JWT 토큰 생성 및 저장
        Token token = generateToken(member.getId());
        member.setRefreshToken(token.getRefreshToken());

        return SignUpResponse.of(token);
    }

    /**
     * Naver 회원가입
     * Authorization Code 방식과 Token 방식(Flutter SDK용) 모두 지원
     */
    @Transactional
    public SignUpResponse signUpNaver(SignUpRequest request) {
        String accessToken;
        String refreshToken;

        // 1. Token 획득 (두 가지 방식 지원)
        if (request.getAccessToken() != null && request.getRefreshToken() != null) {
            // Token 방식 (Flutter SDK 등 클라이언트에서 직접 토큰 발급)
            accessToken = request.getAccessToken();
            refreshToken = request.getRefreshToken();
        } else if (request.getAuthorizationCode() != null) {
            // Authorization Code 방식 (서버에서 토큰 교환)
            String state = request.getState() != null ? request.getState() : "STATE";
            NaverTokenResponse tokenResponse = naverService.getNaverTokenFromAuthorizationCode(
                    request.getAuthorizationCode(),
                    state
            );
            accessToken = tokenResponse.getAccessToken();
            refreshToken = tokenResponse.getRefreshToken();
        } else {
            throw new IllegalArgumentException("Naver 회원가입에는 authorizationCode 또는 (accessToken + refreshToken)이 필요합니다.");
        }

        // 2. Access Token으로 사용자 정보 조회
        String socialId = naverService.getSocialIdFromAccessToken(accessToken);

        // 3. 회원 생성 또는 조회
        Member member = findOrCreateMember(SocialType.NAVER, socialId, request.getFcmToken());

        // 4. Naver Refresh Token 저장
        member.setNaverRefreshToken(refreshToken);

        // 5. JWT 토큰 생성 및 저장
        Token token = generateToken(member.getId());
        member.setRefreshToken(token.getRefreshToken());

        return SignUpResponse.of(token);
    }

    /**
     * Google 회원가입
     */
    @Transactional
    public SignUpResponse signUpGoogle(SignUpRequest request) {
        // 1. Authorization Code로 Token 교환 (ID Token + Refresh Token)
        GoogleTokenResponse tokenResponse = googleService.getGoogleTokenFromAuthorizationCode(
                request.getAuthorizationCode()
        );

        // 2. ID Token 검증 및 소셜 ID 추출
        String socialId = googleService.getSocialIdFromIdToken(tokenResponse.getIdToken());

        // 3. 회원 생성 또는 조회
        Member member = findOrCreateMember(SocialType.GOOGLE, socialId, request.getFcmToken());

        // 4. Google Refresh Token 저장
        member.setGoogleRefreshToken(tokenResponse.getRefreshToken());

        // 5. JWT 토큰 생성 및 저장
        Token token = generateToken(member.getId());
        member.setRefreshToken(token.getRefreshToken());

        return SignUpResponse.of(token);
    }

    /**
     * 회원 생성 또는 조회 (공통 로직)
     */
    private Member findOrCreateMember(SocialType socialType, String socialId, String fcmToken) {
        Member member = memberRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElse(null);

        if (member == null) {
            System.out.println("socialId = " + socialId);
            // 신규 회원 생성
            member = Member.builder()
                    .socialType(socialType)
                    .socialId(socialId)
                    .fcmToken(fcmToken)
                    .status(MemberStatus.PENDING)
                    .build();
            memberRepository.save(member);
            log.info("New member created: memberId={}, socialType={}", member.getId(), socialType);
        } else {
            // 기존 회원 정보 업데이트
            if (member.getStatus() == MemberStatus.WITHDRAWN) {
                member.reactivate();
                log.info("Withdrawn member reactivated: memberId={}", member.getId());
            }
            if (fcmToken != null) {
                member.setFcmToken(fcmToken);
            }
            log.info("Existing member updated: memberId={}", member.getId());
        }

        return member;
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

        // 5. 새로운 Access Token + Refresh Token 생성 (RTR 방식)
        Token newToken = generateToken(member.getId());

        // 6. 새로운 Refresh Token을 DB에 저장 (기존 토큰 무효화)
        member.setRefreshToken(newToken.getRefreshToken());

        return TokenRefreshResponse.of(newToken.getAccessToken(), newToken.getRefreshToken());
    }

    /**
     * 회원탈퇴
     */
    @Transactional
    public void resign(Long memberId, SocialType socialType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));

        // 1. 소셜 로그인별 Token revoke
        switch (socialType) {
            case APPLE -> appleService.revokeAppleToken(member);
            case KAKAO -> kakaoService.unlinkKakaoAccount(member);
            case NAVER -> naverService.revokeNaverToken(member);
            case GOOGLE -> googleService.revokeGoogleToken(member);
        }

        // 2. 회원 탈퇴 처리
        member.withdraw();

        log.info("Member resigned: memberId={}, socialType={}", memberId, socialType);
    }

    /**
     * 하트비트 (마지막 로그인 시간 갱신)
     */
    @Transactional
    public HeartbeatResponse heartbeat(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));

        // 마지막 로그인 시간 갱신
        LocalDateTime now = LocalDateTime.now();
        member.updateLastLoginDateTime(now);

        log.info("Heartbeat updated: memberId={}, lastLoginDateTime={}", memberId, now);

        return HeartbeatResponse.of(now);
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
                accessTokenExpiration,
                TokenType.ACCESS
        );

        String refreshToken = jwtTokenProvider.generateToken(
                authentication,
                refreshTokenExpiration,
                TokenType.REFRESH
        );

        return Token.of(accessToken, refreshToken);
    }
}
