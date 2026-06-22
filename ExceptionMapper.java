package com.studioedge.global.exception;

import com.studioedge.focus_to_levelup_server.domain.admin.exception.AdminAccessDeniedException;
import com.studioedge.focus_to_levelup_server.domain.admin.exception.AdminNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.admin.exception.SuperAdminRequiredException;
import com.studioedge.focus_to_levelup_server.domain.advertisement.exception.AdvertisementNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.attendance.exception.AttendanceAlreadyCheckedException;
import com.studioedge.focus_to_levelup_server.domain.auth.exception.*;
import com.studioedge.focus_to_levelup_server.domain.character.exception.*;
import com.studioedge.focus_to_levelup_server.domain.event.exception.EventUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.event.exception.SchoolNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.*;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.*;
import com.studioedge.focus_to_levelup_server.domain.member.exception.*;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.*;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.AlreadyRegisterReferralCodeException;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.ReferralCodeNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.SelfReferralCodeException;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.RankingExcludeException;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.RankingNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.stat.exception.StatMonthNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.store.exception.*;
import com.studioedge.focus_to_levelup_server.domain.system.exception.*;
import com.studioedge.focus_to_levelup_server.global.fcm.exception.EmptyFcmTokenListException;
import com.studioedge.focus_to_levelup_server.global.fcm.exception.FcmSendException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {
        setUpAdminException();
        setUpAuthException();
        setUpStoreException();
        setUpMemberException();
        setUpCharacterException();
        setUpPaymentException();
        setUpFocusException();
        setUpRankingException();
        setUpEventException();
        setUpMailException();
        setUpCouponException();
        setUpGuildException();
        setUpSystemException();
        setUpStatException();
        setUpEventException();
        setUpFcmException();
        setUpAttendanceException();
        setUpPromotionException();
    }

    public static ExceptionSituation getSituationOf(Exception exception) {
        return mapper.get(exception.getClass());
    }

    /**
     * Admin 관련 예외 등록
     */
    private static void setUpAdminException() {
        mapper.put(AdminAccessDeniedException.class,
                ExceptionSituation.of("관리자 권한이 없습니다.", HttpStatus.FORBIDDEN));
        mapper.put(SuperAdminRequiredException.class,
                ExceptionSituation.of("슈퍼 관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN));
        mapper.put(AdminNotFoundException.class,
                ExceptionSituation.of("관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
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
     * FCM 관련 예외 등록
     */
    private static void setUpFcmException() {
        mapper.put(FcmSendException.class,
                ExceptionSituation.of("FCM 푸시 알림 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        mapper.put(EmptyFcmTokenListException.class,
                ExceptionSituation.of("FCM 토큰 리스트가 비어있습니다.", HttpStatus.BAD_REQUEST));
    }
}
