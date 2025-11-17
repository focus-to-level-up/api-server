package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import com.studioedge.focus_to_levelup_server.domain.character.repository.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.service.CharacterCommandService;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.PhoneNumberVerificationRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.CharacterRewardInfo;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationCheckResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationData;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationRewardResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.entity.PhoneNumberVerification;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.domain.system.exception.InvalidCharacterSelectionException;
import com.studioedge.focus_to_levelup_server.global.firebase.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreRegistrationService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final CharacterRepository characterRepository;
    private final CharacterCommandService characterCommandService;
    private final MailRepository mailRepository;
    private final PhoneNumberVerificationRepository phoneNumberVerificationRepository;
    private final FirebaseService firebaseService;
    private final ObjectMapper objectMapper;

    private static final int PRE_REGISTRATION_DIAMOND = 3000;
    private static final int PRE_REGISTRATION_SUBSCRIPTION_DAYS = 14; // 2주 체험권

    /**
     * 전화번호로 사전예약 확인 및 저장
     * Firebase Firestore에서 실시간 조회 후 PhoneNumberVerification 테이블에 저장
     */
    @Transactional
    public PreRegistrationCheckResponse checkAndSavePhoneNumber(Long memberId, String phoneNumber) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 1. 전화번호 해시 처리
        String hashedPhoneNumber = hashPhoneNumber(phoneNumber);

        // 2. 전화번호 중복 체크 (다른 회원이 이미 사용 중인지 확인)
        phoneNumberVerificationRepository.findByHashedPhoneNumber(hashedPhoneNumber).ifPresent(existing -> {
            if (!existing.getMember().getId().equals(memberId)) {
                throw new IllegalStateException("이미 다른 계정에서 사용 중인 전화번호입니다.");
            }
        });

        // 2. Firebase에서 사전예약 데이터 조회
        try {
            PreRegistrationData preRegData = firebaseService.getPreRegistrationData(phoneNumber);

            // isReserved=false이면 사전예약 안함
            if (!Boolean.TRUE.equals(preRegData.isReserved())) {
                log.info("[PreRegistration] Phone {} is not reserved (isReserved=false)", phoneNumber);
                return PreRegistrationCheckResponse.notRegistered();
            }

            // 3. 사전예약 확인되면 PhoneNumberVerification 테이블에 저장
            PhoneNumberVerification verification = phoneNumberVerificationRepository
                    .findByMember(member)
                    .orElse(null);

            if (verification == null) {
                // 신규 저장 (해시된 전화번호로 저장)
                verification = PhoneNumberVerification.createForPreRegistration(member, hashedPhoneNumber);
                phoneNumberVerificationRepository.save(verification);
                log.info("[PreRegistration] Saved hashed phone number verification for member {}", memberId);
            } else {
                // 이미 저장된 경우 (같은 회원이 다시 확인)
                log.info("[PreRegistration] Phone number already verified for member {}", memberId);
            }

            // 4. 사전예약 날짜 포맷팅
            String registrationDate = formatTimestamp(preRegData.reservedAt());

            return PreRegistrationCheckResponse.of(
                    true,
                    member.getIsPreRegistrationRewarded(),
                    registrationDate
            );

        } catch (Exception e) {
            log.warn("[PreRegistration] Failed to fetch Firebase data for phone {}: {}", phoneNumber, e.getMessage());
            return PreRegistrationCheckResponse.notRegistered();
        }
    }

    /**
     * 사전예약 보상 지급 (캐릭터 선택 포함)
     */
    @Transactional
    public PreRegistrationRewardResponse claimPreRegistrationReward(Long memberId, Long selectedCharacterId) {
        // 1. Member 조회 및 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 이미 보상 받았는지 확인
        if (member.getIsPreRegistrationRewarded()) {
            throw new IllegalStateException("이미 사전예약 보상을 받으셨습니다.");
        }

        // PhoneNumberVerification 확인 (이미 /check API에서 Firebase 조회 완료)
        PhoneNumberVerification verification = phoneNumberVerificationRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("사전예약 정보가 없습니다. 먼저 /check를 호출하세요."));

        log.info("[PreRegistration] Claiming reward for member {} (phone number hash: {})",
                memberId, verification.getHashedPhoneNumber());

        // 2. 캐릭터 검증 (RARE 등급만 허용)
        Character selectedCharacter = characterRepository.findById(selectedCharacterId)
                .orElseThrow(CharacterNotFoundException::new);

        if (selectedCharacter.getRarity() != Rarity.RARE) {
            throw new InvalidCharacterSelectionException();
        }

        // 3. MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        List<Long> mailIds = new ArrayList<>();

        // 4. 다이아 지급 (우편함)
        Mail diamondMail = createDiamondMail(member, PRE_REGISTRATION_DIAMOND);
        mailRepository.save(diamondMail);
        mailIds.add(diamondMail.getId());

        // 5. 프리미엄 구독권 지급 (우편함)
        Mail subscriptionMail = createSubscriptionMail(member, PRE_REGISTRATION_SUBSCRIPTION_DAYS);
        mailRepository.save(subscriptionMail);
        mailIds.add(subscriptionMail.getId());

        // 6. 캐릭터 지급 (우편함)
        Mail characterMail = createCharacterMail(member, selectedCharacter);
        mailRepository.save(characterMail);
        mailIds.add(characterMail.getId());

        // 7. 보상 수령 완료 플래그 설정
        member.markPreRegistrationRewarded();

        log.info("Pre-registration reward claimed for member {}: {} diamonds, {} days subscription, character {}",
                memberId, PRE_REGISTRATION_DIAMOND, PRE_REGISTRATION_SUBSCRIPTION_DAYS, selectedCharacter.getName());

        return PreRegistrationRewardResponse.of(
                PRE_REGISTRATION_DIAMOND,
                PRE_REGISTRATION_SUBSCRIPTION_DAYS,
                CharacterRewardInfo.from(selectedCharacter),
                mailIds
        );
    }

    /**
     * 다이아 우편 생성
     */
    private Mail createDiamondMail(Member member, int diamondAmount) {
        return Mail.builder()
                .receiver(member)
                .senderName("Focus to Level Up")
                .type(MailType.PRE_REGISTRATION)
                .title("사전예약 보상을 수령하세요")
                .description("사전예약 감사 다이아 지급")
                .popupTitle("사전 예약 보상 다이아 수령")
                .popupContent("Focus to Level Up 사전예약에 참여해 주셔서 감사합니다!")
                .reward(diamondAmount)
                .expiredAt(LocalDate.now().plusDays(28))
                .build();
    }

    /**
     * 구독권 우편 생성
     */
    private Mail createSubscriptionMail(Member member, int durationDays) {
        try {
            String description = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                put("subscriptionType", SubscriptionType.PREMIUM.name());
                put("durationDays", durationDays);
            }});

            return Mail.builder()
                    .receiver(member)
                    .senderName("Focus to Level Up")
                    .type(MailType.PRE_REGISTRATION)
                    .title("사전예약 보상을 수령하세요")
                    .description(description)
                    .popupTitle("사전예약 프리미엄 구독권 수령")
                    .popupContent("사전예약 보상으로 받으신 프리미엄 구독권 2주 체험권을 수령하세요")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(28))
                    .build();
        } catch (Exception e) {
            log.error("Failed to create subscription mail JSON", e);
            throw new IllegalStateException("구독권 우편 생성에 실패했습니다.");
        }
    }

    /**
     * 캐릭터 우편 생성
     */
    private Mail createCharacterMail(Member member, Character character) {
        try {
            String description = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                put("characterId", character.getId());
            }});

            return Mail.builder()
                    .receiver(member)
                    .senderName("Focus to Level Up")
                    .type(MailType.CHARACTER_REWARD)
                    .title("사전예약 보상을 수령하세요")
                    .description(description)
                    .popupTitle("사전예약 레어 캐릭터 수령")
                    .popupContent("사전예약 보상으로 받으신 " + character.getName() + "을(를) 수령하세요!")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(28))
                    .build();
        } catch (Exception e) {
            log.error("Failed to create character mail JSON", e);
            throw new IllegalStateException("캐릭터 우편 생성에 실패했습니다.");
        }
    }

    /**
     * Firebase 타임스탬프를 날짜 문자열로 변환
     * @param timestamp Firebase의 밀리초 타임스탬프
     * @return "yyyy-MM-dd" 형식 문자열
     */
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) {
            return LocalDate.now().toString();
        }

        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 전화번호를 SHA-256 해시로 변환
     * @param phoneNumber 평문 전화번호
     * @return SHA-256 해시 문자열 (64자)
     */
    private String hashPhoneNumber(String phoneNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(phoneNumber.getBytes(StandardCharsets.UTF_8));

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            throw new IllegalStateException("전화번호 해시 처리에 실패했습니다.");
        }
    }
}
