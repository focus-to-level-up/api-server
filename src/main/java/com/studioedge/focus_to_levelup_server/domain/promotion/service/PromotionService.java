package com.studioedge.focus_to_levelup_server.domain.promotion.service;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.promotion.dao.ReferralRepository;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.ReferralInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.RegisterCodeRequest;
import com.studioedge.focus_to_levelup_server.domain.promotion.dto.RouletteSpinResponse;
import com.studioedge.focus_to_levelup_server.domain.promotion.entity.Referral;
import com.studioedge.focus_to_levelup_server.domain.promotion.enums.RouletteReward;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.AlreadyRegisterReferralCodeException;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.ReferralCodeNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.promotion.exception.SelfReferralCodeException;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ReferralRepository referralRepository;
    private final MailRepository mailRepository;

    private final SecureRandom random = new SecureRandom();

    /**
     * 레퍼럴 및 룰렛 정보 조회
     */
    public ReferralInfoResponse getPromotionInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 1. 코드가 없으면 생성 (최초 1회)
        if (member.getReferralCode() == null) {
            String newCode = generateUniqueReferralCode();
            member.updateReferralCode(newCode);
        }

        // 2. 내가 이미 등록했는지 확인
        boolean isRegistered = referralRepository.existsByInviteeId(memberId);

        return ReferralInfoResponse.builder()
                .myReferralCode(member.getReferralCode())
                .ticketCount(memberInfo.getRouletteTicketCount())
                .isRegistered(isRegistered)
                .build();
    }

    /**
     * 추천인 코드 등록
     */
    public void registerCode(Long memberId, RegisterCodeRequest request) {
        Member invitee = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 1. 이미 등록했는지 검증 (중복 등록 방지)
        if (referralRepository.existsByInviteeId(memberId)) {
            throw new AlreadyRegisterReferralCodeException();
        }

        // 2. 대상 코드 유효성 검증
        Member inviter = memberRepository.findByReferralCode(request.referralCode())
                .orElseThrow(ReferralCodeNotFoundException::new);

        // 3. 자기 자신 추천 방지
        if (inviter.getId().equals(invitee.getId())) {
            throw new SelfReferralCodeException();
        }

        // 4. 관계 저장
        Referral referral = Referral.builder()
                .inviter(inviter)
                .invitee(invitee)
                .build();

        try {
            referralRepository.save(referral);
        } catch (DataIntegrityViolationException e) {
            // 동시에 요청이 들어와서 DB Unique 제약조건에 걸린 경우
            throw new AlreadyRegisterReferralCodeException();
        }

        // 5. 초대자(B)에게 룰렛 티켓 지급
        MemberInfo inviterInfo = memberInfoRepository.findByMemberId(inviter.getId())
                .orElseThrow(InvalidMemberException::new);
        inviterInfo.addRouletteTicket(1);
    }

    /**
     * 룰렛 돌리기
     */
    public RouletteSpinResponse spinRoulette(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 1. 티켓 소모
        memberInfo.useRouletteTicket();

        // 2. 확률에 따른 보상 추첨
        RouletteReward reward = drawReward();

        // 3. 우편함으로 보상 발송
        Member member = memberRepository.getReferenceById(memberId);
        Mail mail = createRouletteMail(member, reward);
        mailRepository.save(mail);

        return RouletteSpinResponse.of(reward, memberInfo.getRouletteTicketCount());
    }

    // --- Helper Methods ---

    private String generateUniqueReferralCode() {
        String code;
        do {
            // 6자리 대문자+숫자 랜덤 생성
            code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        } while (memberRepository.existsByReferralCode(code));
        return code;
    }

    private RouletteReward drawReward() {
        int randomVal = random.nextInt(100); // 0 ~ 99
        int currentProb = 0;

        for (RouletteReward reward : RouletteReward.values()) {
            currentProb += reward.getProbability();
            if (randomVal < currentProb) {
                return reward;
            }
        }
        return RouletteReward.GOLD_1000; // Fallback
    }
    /**
     * 룰렛 보상 메일 생성
     */
    private Mail createRouletteMail(Member member, RouletteReward rouletteReward) {
        // 기본 빌더 생성
        Mail.MailBuilder builder = Mail.builder()
                .receiver(member)
                .senderName("운영자")
                .title("친구 초대 룰렛 보상")
                .description("친구 초대 룰렛 이벤트 보상 지급")
                .popupTitle("룰렛 보상")
                .popupContent("축하합니다!\n[" + rouletteReward.getDescription() + "]을(를) 획득하셨습니다.")
                .expiredAt(LocalDate.now().plusDays(30))
                .reward(rouletteReward.getAmount()); // 대표 수량

        // 보상 타입에 따른 MailType 및 필드 설정
        switch (rouletteReward.getType()) {
            case DIAMOND -> {
                builder.type(MailType.EVENT); // 또는 ADMIN_REWARD (다이아 지급용)
                builder.diamondAmount(rouletteReward.getAmount());
            }
            case GOLD -> {
                builder.type(MailType.ADMIN_REWARD); // 골드 지급 처리가 포함된 타입
                builder.goldAmount(rouletteReward.getAmount());
            }
            case CHARACTER -> {
                // 사전예약 보상 로직(handleCharacterSelectionTicketMail)과 동일하게 구성
                builder.type(MailType.CHARACTER_SELECTION_TICKET);
                builder.title("에픽 캐릭터 선택권");
                builder.description("룰렛 당첨! 원하는 에픽 캐릭터를 선택하세요.");
                builder.popupTitle("캐릭터 선택권");
                builder.popupContent("EPIC 등급의 캐릭터 중\n원하는 캐릭터를 선택하세요!");

                // MailCommandService에서 검증하는 필드 (allowedRarity)
                builder.allowedRarity("EPIC");
            }
            default -> throw new IllegalArgumentException("지원하지 않는 룰렛 보상 타입입니다.");
        }

        return builder.build();
    }
}
