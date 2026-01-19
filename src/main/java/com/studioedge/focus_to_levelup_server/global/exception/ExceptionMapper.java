package com.studioedge.focus_to_levelup_server.global.exception;

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
        mapper.put(MemberItemNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 아이템이거나 권한이 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(ItemNotCompletedException.class,
                ExceptionSituation.of("아직 달성하지 않은 아이템입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(RewardAlreadyReceivedException.class,
                ExceptionSituation.of("이미 보상을 받은 아이템입니다.", HttpStatus.CONFLICT));
    }

    /**
     * Member 관련 예외 등록
     */
    private static void setUpMemberException() {
        mapper.put(MemberNotFoundException.class,
                ExceptionSituation.of("해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(NicknameDuplicatedException.class,
                ExceptionSituation.of("해당 닉네임은 이미 존재합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(NicknameUpdateException.class,
                ExceptionSituation.of("닉네임은 변경일을 기준으로 1달 이후에 변경 가능합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(InvalidMemberException.class,
                ExceptionSituation.of("회원님의 정보가 존재하지 않습니다. 탈퇴후 계정을 새로 생성해야합니다.", HttpStatus.NOT_FOUND));
        mapper.put(CategoryUpdateException.class,
                ExceptionSituation.of("카테고리는 변경일을 기준으로 1달 이후에 변경 가능합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(SchoolUpdateException.class,
                ExceptionSituation.of("학교정보는 변경일을 기준으로 1달 이후에 변경 가능합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(AssetUnauthorizedException.class,
                ExceptionSituation.of("현재 에셋을 사용할 수 있는 권한이 없습니다.", HttpStatus.FORBIDDEN));
        mapper.put(InvalidSignUpException.class,
                ExceptionSituation.of("가입정보를 정확히 확인하여 전송해주시길 바랍니다.", HttpStatus.BAD_REQUEST));
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
        mapper.put(CharacterSlotFullException.class,
                ExceptionSituation.of("훈련장 슬롯이 가득 찼습니다. 캐릭터를 배치할 수 없습니다. (최대 9개)", HttpStatus.BAD_REQUEST));
        mapper.put(CharacterUnauthorizedException.class,
                ExceptionSituation.of("소유하고 있는 캐릭터가 아닙니다.", HttpStatus.FORBIDDEN));
        mapper.put(CharacterEvolveException.class,
                ExceptionSituation.of("현재 캐릭터는 진화 조건을 충족하지 않습니다.", HttpStatus.BAD_REQUEST));
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

        // 구독권 타입 불일치 예외
        mapper.put(SubscriptionTypeMismatchException.class,
                ExceptionSituation.of("현재 활성화된 구독권과 다른 종류의 구독권은 수령할 수 없습니다.", HttpStatus.BAD_REQUEST));

        // RevenueCat Webhook 관련 예외
        mapper.put(WebhookAuthenticationException.class,
                ExceptionSituation.of("Webhook 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED));
        mapper.put(DuplicateWebhookEventException.class,
                ExceptionSituation.of("이미 처리된 Webhook 이벤트입니다.", HttpStatus.OK));
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
                ExceptionSituation.of("해당 과목에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN));

        mapper.put(TodoNotFoundException.class,
                ExceptionSituation.of("해당 할일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(TodoUnAuthorizedException.class,
                ExceptionSituation.of("해당 할일에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN));
        mapper.put(PlannerNotFoundException.class,
                ExceptionSituation.of("해당 날짜의 플래너를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        mapper.put(AllowedAppNotFoundException.class,
                ExceptionSituation.of("해당 허용가능 앱을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Stat 관련 예외 등록
     */
    private static void setUpStatException() {
        mapper.put(StatMonthNotFoundException.class,
                ExceptionSituation.of("해당 월의 통계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Ranking 관련 예외 등록
     */
    private static void setUpRankingException() {
        mapper.put(RankingNotFoundException.class,
                ExceptionSituation.of("랭킹에 포함되어있지 않습니다. 랭킹은 첫 생성날의 다음주부터 참여됩니다.", HttpStatus.NOT_FOUND));
        mapper.put(RankingExcludeException.class,
                ExceptionSituation.of("사용자는 2회 경고로 인해 랭킹에서 제외되었습니다. 2주간 정지됩니다.", HttpStatus.NOT_FOUND));
        mapper.put(LeagueNotFoundException.class,
                ExceptionSituation.of("해당되는 리그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    /**
     * Event(School) 관련 예외 등록
     * */
    private static void setUpEventException() {
        mapper.put(SchoolNotFoundException.class,
                ExceptionSituation.of("입력한 학교가 존재하지 않습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(EventUnAuthorizedException.class,
                ExceptionSituation.of("이벤트에 참여할 권한이 없습니다.", HttpStatus.FORBIDDEN));
    }

    /**
     * Attendance 관련 예외 등록
     */
    private static void setUpAttendanceException() {
        mapper.put(AttendanceAlreadyCheckedException.class,
                ExceptionSituation.of("이미 오늘 출석체크를 완료했습니다.", HttpStatus.FORBIDDEN));
    }

    /**
     * Promotion 관련 예외 등록
     */
    private static void setUpPromotionException() {
        mapper.put(AlreadyRegisterReferralCodeException.class,
                ExceptionSituation.of("이미 추천인 코드를 등록하였습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(ReferralCodeNotFoundException.class,
                ExceptionSituation.of("존재하지 않는 추천인 코드입니다.", HttpStatus.NOT_FOUND));
        mapper.put(SelfReferralCodeException.class,
                ExceptionSituation.of("자기 자신의 추천인 코드는 등록할 수 없습니다.", HttpStatus.BAD_REQUEST));
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
        mapper.put(InvalidCharacterSelectionException.class,
                ExceptionSituation.of("잘못된 캐릭터 선택입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(ReceiverNotFoundException.class,
                ExceptionSituation.of("선물을 받을 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
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

    /**
     * Guild 관련 예외 등록
     */
    private static void setUpGuildException() {
        // 404 NOT_FOUND
        mapper.put(GuildNotFoundException.class,
                ExceptionSituation.of("길드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(NotGuildMemberException.class,
                ExceptionSituation.of("길드원이 아닙니다.", HttpStatus.NOT_FOUND));

        // 403 FORBIDDEN
        mapper.put(InsufficientGuildPermissionException.class,
                ExceptionSituation.of("길드 권한이 없습니다.", HttpStatus.FORBIDDEN));

        // 400 BAD_REQUEST
        mapper.put(GuildFullException.class,
                ExceptionSituation.of("길드 정원이 가득 찼습니다. (최대 20명)", HttpStatus.BAD_REQUEST));
        mapper.put(InvalidGuildPasswordException.class,
                ExceptionSituation.of("길드 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(AlreadyJoinedGuildException.class,
                ExceptionSituation.of("이미 가입한 길드입니다.", HttpStatus.BAD_REQUEST));
        mapper.put(MaxGuildMembershipExceededException.class,
                ExceptionSituation.of("최대 길드 가입 수를 초과했습니다. (최대 10개)", HttpStatus.BAD_REQUEST));
        mapper.put(MaxBoostLimitExceededException.class,
                ExceptionSituation.of("부스트 한도를 초과했습니다. (유저: 2개, 길드: 10명)", HttpStatus.BAD_REQUEST));
        mapper.put(AlreadyBoostedException.class,
                ExceptionSituation.of("이미 해당 길드에 부스트 중입니다.", HttpStatus.CONFLICT));
        mapper.put(CannotDeleteGuildWithMembersException.class,
                ExceptionSituation.of("길드원이 있는 길드는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(LeaderCannotLeaveException.class,
                ExceptionSituation.of("길드장은 먼저 권한을 위임해야 탈퇴할 수 있습니다.", HttpStatus.FORBIDDEN));
        mapper.put(FocusRequestCooldownException.class,
                ExceptionSituation.of("같은 길드원에게 1시간 내 재요청할 수 없습니다.", HttpStatus.TOO_MANY_REQUESTS));
        mapper.put(GuildRoleUnAuthorizedException.class,
                ExceptionSituation.of("길드장을 위임할 권리가 없습니다.", HttpStatus.TOO_MANY_REQUESTS));
    }

    /**
     * 시스템 관련 예외 등록
     */
    private static void setUpSystemException() {
        mapper.put(BackgroundNotFoundException.class,
                ExceptionSituation.of("배경을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(WeeklyRewardAlreadyReceivedException.class,
                ExceptionSituation.of("주간 보상을 이미 수령하였거나 받을 수 있는 주간보상이 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(WeeklyRewardNotFoundException.class,
                ExceptionSituation.of("받을 수 있는 주간보상이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
        mapper.put(InsufficientBonusTicketException.class,
                ExceptionSituation.of("보유한 보너스 티켓이 부족합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(AdvertisementNotFoundException.class,
                ExceptionSituation.of("해당 광고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
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
