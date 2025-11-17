package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dao.CouponLogRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.CouponRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.CouponRedeemResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Coupon;
import com.studioedge.focus_to_levelup_server.domain.system.entity.CouponLog;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.domain.system.exception.CouponAlreadyUsedException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.CouponExpiredException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.CouponNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponCommandService {

    private final CouponRepository couponRepository;
    private final CouponLogRepository couponLogRepository;
    private final MailRepository mailRepository;

    /**
     * 쿠폰 사용 및 우편 발송
     */
    @Transactional
    public CouponRedeemResponse redeemCoupon(Member member, String couponCode) {
        // 1. 쿠폰 조회
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(CouponNotFoundException::new);

        // 2. 쿠폰 만료 확인
        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException();
        }

        // 3. 중복 사용 확인
        if (couponLogRepository.existsByMemberIdAndCouponId(member.getId(), coupon.getId())) {
            throw new CouponAlreadyUsedException();
        }

        // 4. CouponLog 생성 (사용 내역 저장)
        CouponLog couponLog = CouponLog.builder()
                .member(member)
                .coupon(coupon)
                .build();
        couponLogRepository.save(couponLog);

        // 5. 우편 발송
        Mail mail = createMailFromCoupon(member, coupon);
        mailRepository.save(mail);

        // 6. 응답 반환
        return CouponRedeemResponse.of(
                coupon.getCouponCode(),
                coupon.getRewardType(),
                coupon.getReward()
        );
    }

    /**
     * 쿠폰에서 우편 생성
     */
    private Mail createMailFromCoupon(Member member, Coupon coupon) {
        MailType mailType = determineMailType(coupon.getRewardType());
        Integer reward = (coupon.getRewardType() == RewardType.DIAMOND) ? coupon.getReward() : 0;

        // 구독권 타입인 경우 description에 JSON 메타데이터 생성
        String description = coupon.getDescription();
        if (coupon.getRewardType() == RewardType.ETC && coupon.getSubscriptionType() != null) {
            description = String.format(
                "{\"subscriptionType\": \"%s\", \"durationDays\": %d}",
                coupon.getSubscriptionType().name(),
                coupon.getSubscriptionDurationDays()
            );
        }

        return Mail.builder()
                .receiver(member)
                .senderName("운영자")
                .type(mailType)
                .title("쿠폰 사용 보상")
                .description(description)
                .reward(reward)
                .expiredAt(LocalDate.now().plusDays(7)) // 7일 후 만료
                .build();
    }

    /**
     * RewardType에 따른 MailType 결정
     */
    private MailType determineMailType(RewardType rewardType) {
        return switch (rewardType) {
            case DIAMOND, GOLD -> MailType.EVENT;
            case CHARACTER -> MailType.CHARACTER_REWARD; // 캐릭터 보상
            case ETC -> MailType.SUBSCRIPTION; // 구독권 등 기타 보상
        };
    }
}