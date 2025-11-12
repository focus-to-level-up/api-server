package com.studioedge.focus_to_levelup_server.global.exception;

import com.studioedge.focus_to_levelup_server.domain.auth.exception.*;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.exception.*;
import com.studioedge.focus_to_levelup_server.domain.event.exception.EventUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.event.exception.SchoolNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.*;
import com.studioedge.focus_to_levelup_server.domain.member.exception.*;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.RankingNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.*;
import com.studioedge.focus_to_levelup_server.domain.store.exception.InsufficientGoldException;
import com.studioedge.focus_to_levelup_server.domain.store.exception.InvalidItemOptionException;
import com.studioedge.focus_to_levelup_server.domain.store.exception.ItemAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.store.exception.ItemNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.*;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {
        setUpAuthException();
        setUpStoreException();
        setUpMemberException();
        setUpCharacterException();
        setUpPaymentException();
        setUpFocusException();
        setUpRankingException();
        setUpMailException();
        setUpCouponException();
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

    /**
     * Member 관련 예외 등록
     */
    private static void setUpMemberException() {
        mapper.put(MemberNotFoundException.class,
                ExceptionSituation.of("해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(NicknameUpdateException.class,
                ExceptionSituation.of("닉네임은 변경일을 기준으로 1달 이후에 변경 가능합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(InvalidMemberException.class,
                ExceptionSituation.of("회원님의 정보가 존재하지 않습니다. 탈퇴후 계정을 새로 생성해야합니다.", HttpStatus.NOT_FOUND));
        mapper.put(CategoryUpdateException.class,
                ExceptionSituation.of("카테고리는 변경일을 기준으로 1달 이후에 변경 가능합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(AssetUnauthorizedException.class,
                ExceptionSituation.of("현재 에셋을 사용할 수 있는 권한이 없습니다.", HttpStatus.UNAUTHORIZED));
        mapper.put(InvalidSignUpException.class,
                ExceptionSituation.of("가입정보를 정확히 확인하여 전송해주시길 바랍니다.", HttpStatus.UNAUTHORIZED));
    }

    /**
     * Character 관련 예외 등록
     */
    private static void setUpCharacterException() {
        mapper.put(CharacterNotFoundException.class,
                ExceptionSituation.of("해당 캐릭터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(CharacterDefaultNotFoundException.class,
                ExceptionSituation.of("대표 캐릭터를 찾을 수 없습니다. 대표캐릭터를 설정해주세요", HttpStatus.NOT_FOUND));
        mapper.put(CharacterAlreadyPurchasedException.class,
                ExceptionSituation.of("이미 보유한 캐릭터입니다.", HttpStatus.CONFLICT));
        mapper.put(InsufficientDiamondException.class,
                ExceptionSituation.of("다이아가 부족합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(InvalidDefaultEvolutionException.class,
                ExceptionSituation.of("유효하지 않은 진화 단계입니다. 보유한 진화 단계만 선택할 수 있습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(MemberCharacterNotFoundException.class,
                ExceptionSituation.of("보유하지 않은 캐릭터입니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Payment 관련 예외 등록
     */
    private static void setUpPaymentException() {
        // 상품 관련 예외
        mapper.put(ProductNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND));
        mapper.put(DuplicatePurchaseException.class,
                ExceptionSituation.of("이미 처리된 결제입니다.", HttpStatus.CONFLICT));
        mapper.put(InvalidReceiptException.class,
                ExceptionSituation.of("유효하지 않은 영수증입니다.", HttpStatus.BAD_REQUEST));

        // 환불 관련 예외
        mapper.put(PurchaseNotFoundException.class,
                ExceptionSituation.of("결제 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(UnauthorizedRefundException.class,
                ExceptionSituation.of("환불 권한이 없습니다.", HttpStatus.FORBIDDEN));
        mapper.put(RefundNotAllowedException.class,
                ExceptionSituation.of("환불이 불가능합니다. (7일 경과 또는 재화 사용)", HttpStatus.BAD_REQUEST));
        mapper.put(InsufficientDiamondForRefundException.class,
                ExceptionSituation.of("환불을 위한 다이아가 부족합니다.", HttpStatus.BAD_REQUEST));

        // 구독권 관련 예외
        mapper.put(SubscriptionNotFoundException.class,
                ExceptionSituation.of("구독권을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(PremiumSubscriptionRequiredException.class,
                ExceptionSituation.of("프리미엄 구독권이 필요합니다.", HttpStatus.FORBIDDEN));

        // 선물 티켓 관련 예외
        mapper.put(NoAvailableGiftTicketException.class,
                ExceptionSituation.of("사용 가능한 선물 티켓이 없습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(TicketAlreadyUsedException.class,
                ExceptionSituation.of("이미 사용된 티켓입니다.", HttpStatus.CONFLICT));
        mapper.put(RecipientAlreadyHasPremiumException.class,
                ExceptionSituation.of("상대방이 이미 프리미엄 구독권을 보유하고 있습니다.", HttpStatus.BAD_REQUEST));
    }

    /**
     * Focus 관련 예외 등록
     */
    private static void setUpFocusException() {
        mapper.put(DailyGoalNotFoundException.class,
                ExceptionSituation.of("일일 목표를 찾을 수 없습니다. 일일 목표를 먼저 설정해주세요.", HttpStatus.NOT_FOUND));
        mapper.put(DailyGoalDuplicatedException.class,
                ExceptionSituation.of("일일 목표를 이미 설정했습니다.", HttpStatus.CONFLICT));
        mapper.put(AlreadyReceivedDailyGoalException.class,
                ExceptionSituation.of("해당 목표는 이미 수령하였습니다.", HttpStatus.CONFLICT));

        mapper.put(SubjectNotFoundException.class,
                ExceptionSituation.of("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(SubjectUnAuthorizedException.class,
                ExceptionSituation.of("해당 과목에 접근할 권한이 없습니다.", HttpStatus.UNAUTHORIZED));

        mapper.put(TodoNotFoundException.class,
                ExceptionSituation.of("해당 할일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(TodoUnAuthorizedException.class,
                ExceptionSituation.of("해당 할일에 접근할 권한이 없습니다.", HttpStatus.UNAUTHORIZED));

        mapper.put(AllowedAppNotFoundException.class,
                ExceptionSituation.of("해당 허용가능 앱을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Ranking 관련 예외 등록
     */
    private static void setUpRankingException() {
        mapper.put(RankingNotFoundException.class,
                ExceptionSituation.of("랭킹에 해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Event(School) 관련 예외 등록
     * */
    private static void setUpEventException() {
        mapper.put(SchoolNotFoundException.class,
                ExceptionSituation.of("입력한 학교가 존재하지 않습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(EventUnAuthorizedException.class,
                ExceptionSituation.of("이벤트에 참여할 권한이 없습니다.", HttpStatus.UNAUTHORIZED));
    }
    /**
     * Mail 관련 예외 등록
     */
    private static void setUpMailException() {
        mapper.put(MailNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 우편입니다.", HttpStatus.NOT_FOUND));
        mapper.put(UnauthorizedMailAccessException.class,
                ExceptionSituation.of("우편에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN));
        mapper.put(MailAlreadyReceivedException.class,
                ExceptionSituation.of("이미 수령한 우편입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(MailExpiredException.class,
                ExceptionSituation.of("만료된 우편입니다.", HttpStatus.BAD_REQUEST));
    }

    /**
     * Coupon 관련 예외 등록
     */
    private static void setUpCouponException() {
        mapper.put(CouponNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 쿠폰입니다.", HttpStatus.NOT_FOUND));
        mapper.put(CouponExpiredException.class,
                ExceptionSituation.of("만료된 쿠폰입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(CouponAlreadyUsedException.class,
                ExceptionSituation.of("이미 사용한 쿠폰입니다.", HttpStatus.BAD_REQUEST));
    }
}
