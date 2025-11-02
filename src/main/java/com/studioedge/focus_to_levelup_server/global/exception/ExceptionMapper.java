package com.studioedge.focus_to_levelup_server.global.exception;

import com.studioedge.focus_to_levelup_server.domain.auth.exception.*;
import com.studioedge.focus_to_levelup_server.domain.store.exception.*;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {
        setUpAuthException();
        setUpStoreException();
        // TODO: 도메인별 예외 추가 등록
        // setUpMemberException();
    }

    public static ExceptionSituation getSituationOf(Exception exception) {
        return mapper.get(exception.getClass());
    }

    /**
     * Auth 관련 예외 등록
     */
    private static void setUpAuthException() {
        mapper.put(UserNotRegisteredException.class,
                ExceptionSituation.of("등록되지 않은 사용자입니다. 회원가입이 필요합니다.", HttpStatus.UNAUTHORIZED));
        mapper.put(WithdrawnMemberException.class,
                ExceptionSituation.of("탈퇴한 회원입니다. 재가입이 필요합니다.", HttpStatus.FORBIDDEN));
        mapper.put(InvalidTokenTypeException.class,
                ExceptionSituation.of("잘못된 토큰 타입입니다. Refresh Token이 필요합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(TokenMismatchException.class,
                ExceptionSituation.of("Refresh Token이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED));
        mapper.put(RefreshTokenExpiredException.class,
                ExceptionSituation.of("Refresh Token이 만료되었습니다. 재로그인이 필요합니다.", HttpStatus.UNAUTHORIZED));
        mapper.put(InvalidSocialTokenException.class,
                ExceptionSituation.of("유효하지 않은 소셜 로그인 토큰입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(InvalidAppleTokenException.class,
                ExceptionSituation.of("유효하지 않은 Apple Identity Token입니다.", HttpStatus.BAD_REQUEST));
    }

    /**
     * Store 관련 예외 등록
     */
    private static void setUpStoreException() {
        mapper.put(ItemNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 아이템입니다.", HttpStatus.NOT_FOUND));
        mapper.put(ItemAlreadyPurchasedException.class,
                ExceptionSituation.of("이미 구매한 아이템입니다.", HttpStatus.CONFLICT));
        mapper.put(InvalidItemOptionException.class,
                ExceptionSituation.of("유효하지 않은 아이템 옵션입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(InsufficientGoldException.class,
                ExceptionSituation.of("골드가 부족합니다.", HttpStatus.BAD_REQUEST));
    }
}
