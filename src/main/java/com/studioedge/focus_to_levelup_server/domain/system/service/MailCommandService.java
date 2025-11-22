package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.service.CharacterCommandService;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.dao.AssetRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.AssetRewardInfo;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.CharacterRewardInfo;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.MailAcceptResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.SubscriptionInfo;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.domain.system.exception.InvalidMailMetadataException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailAlreadyReceivedException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailExpiredException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.UnauthorizedMailAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailCommandService {

    private final MailRepository mailRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CharacterRepository characterRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final AssetRepository assetRepository;
    private final MemberAssetRepository memberAssetRepository;
    private final CharacterCommandService characterCommandService;
    private final ObjectMapper objectMapper;

    /**
     * 우편 수락 및 보상 지급
     * @param characterId 캐릭터 선택권 우편의 경우 선택할 캐릭터 ID (선택적)
     */
    @Transactional
    public MailAcceptResponse acceptMail(Long memberId, Long mailId, Long characterId) {
        // 1. 우편 조회 및 검증
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(MailNotFoundException::new);

        // 소유권 확인
        if (!mail.isOwnedBy(memberId)) {
            throw new UnauthorizedMailAccessException();
        }

        // 이미 수령했는지 확인
        if (mail.getIsReceived()) {
            throw new MailAlreadyReceivedException();
        }

        // 만료되었는지 확인
        if (mail.isExpired()) {
            throw new MailExpiredException();
        }

        // 2. MailType에 따른 보상 지급
        return switch (mail.getType()) {
            case GIFT_SUBSCRIPTION -> handleSubscriptionMail(mail, memberId);
            case GIFT_BONUS_TICKET -> handlePurchaseMail(mail, memberId);
            case CHARACTER_REWARD -> handleCharacterMail(mail, memberId);
            case CHARACTER_SELECTION_TICKET -> handleCharacterSelectionTicketMail(mail, memberId, characterId);
            case PROFILE_BORDER -> handleBorderMail(mail, memberId);
            case EVENT, GUILD_WEEKLY, TIER_PROMOTION, SEASON_END -> handleDiamondMail(mail, memberId);
            case COUPON -> handleCouponMail(mail, memberId);
        };
    }

    /**
     * 쿠폰 우편 처리 (임시 구현 - 추후 쿠폰 타입에 따라 세분화)
     * TODO: 쿠폰 보상 타입에 따라 구독권/캐릭터 등 다양한 보상 처리
     */
    private MailAcceptResponse handleCouponMail(Mail mail, Long memberId) {
        // 현재는 다이아 보상만 처리 (기본 구현)
        return handleDiamondMail(mail, memberId);
    }

    /**
     * 구독권 우편 처리
     */
    private MailAcceptResponse handleSubscriptionMail(Mail mail, Long memberId) {
        // description에서 JSON 파싱
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());

        String subscriptionTypeStr = (String) metadata.get("subscriptionType");
        Integer durationDays = (Integer) metadata.get("durationDays");
        Integer giftCount = metadata.containsKey("giftCount") ? (Integer) metadata.get("giftCount") : 0;

        SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr);

        // MemberInfo 조회 (보너스 티켓 지급용)
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 보너스 티켓 지급 (metadata에 bonusTicketCount가 있으면 지급)
        if (metadata.containsKey("bonusTicketCount")) {
            Object bonusTicketObj = metadata.get("bonusTicketCount");
            if (bonusTicketObj instanceof Number) {
                int bonusTicketCount = ((Number) bonusTicketObj).intValue();
                if (bonusTicketCount > 0) {
                    memberInfo.addBonusTicket(bonusTicketCount);
                    log.info("Rewarded {} bonus tickets to member {} from subscription gift",
                            bonusTicketCount, memberId);
                }
            }
        }

        // Subscription 엔티티 생성
        Subscription subscription = Subscription.builder()
                .member(mail.getReceiver())
                .type(subscriptionType)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(durationDays))
                .isActive(true)
                .isAutoRenew(false) // 우편으로 받은 구독권은 자동 갱신 안 됨
                .source(SubscriptionSource.GIFT)
                .giftedByMemberId(null) // 운영자 선물
                .build();

        subscriptionRepository.save(subscription);

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofSubscription(
                mail.getId(),
                mail.getTitle(),
                SubscriptionInfo.from(subscription)
        );
    }

    /**
     * 다이아 우편 처리
     */
    private MailAcceptResponse handleDiamondMail(Mail mail, Long memberId) {
        // MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 다이아 지급
        int diamondReward = mail.getReward();
        memberInfo.addDiamond(diamondReward);

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofDiamond(
                mail.getId(),
                mail.getTitle(),
                diamondReward
        );
    }

    /**
     * 테두리 우편 처리
     */
    private MailAcceptResponse handleBorderMail(Mail mail, Long memberId) {
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());
        String assetName = (String) metadata.get("assetName");

        if (assetName == null) {
            throw new IllegalArgumentException("우편에 에셋 정보가 존재하지 않습니다.");
        }

        // Asset 조회
        Asset asset = assetRepository.findByName(assetName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 에셋입니다: " + assetName));

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 이미 보유 중인지 확인 후 지급
        if (!memberAssetRepository.existsByMemberIdAndAssetId(memberId, asset.getId())) {
            MemberAsset memberAsset = MemberAsset.builder()
                    .member(member)
                    .asset(asset)
                    .build();
            memberAssetRepository.save(memberAsset);
            log.info("Granted asset '{}' to member {}", assetName, memberId);
        } else {
            log.info("Member {} already owns asset '{}'. Skipping grant.", memberId, assetName);
        }

        // 5. 수령 처리
        mail.markAsReceived();

        // 6. 응답 반환
        return MailAcceptResponse.ofAsset(
                mail.getId(),
                mail.getTitle(),
                AssetRewardInfo.from(asset)
        );
    }

    /**
     * 구매 보상 우편 처리 (다이아 + 보너스 티켓)
     */
    private MailAcceptResponse handlePurchaseMail(Mail mail, Long memberId) {
        // MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 다이아 지급
        int diamondReward = mail.getReward();
        if (diamondReward > 0) {
            memberInfo.addDiamond(diamondReward);
        }

        // 보너스 티켓 지급 (description에서 JSON 파싱)
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());
        int bonusTicketCount = 0;
        if (metadata.containsKey("bonusTicketCount")) {
            Object bonusTicketObj = metadata.get("bonusTicketCount");
            if (!(bonusTicketObj instanceof Number)) {
                throw new InvalidMailMetadataException();
            }
            bonusTicketCount = ((Number) bonusTicketObj).intValue();
        }

        if (bonusTicketCount > 0) {
            memberInfo.addBonusTicket(bonusTicketCount);
            log.info("Rewarded {} bonus tickets to member {}", bonusTicketCount, memberId);
        }

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofDiamond(
                mail.getId(),
                mail.getTitle(),
                diamondReward
        );
    }

    /**
     * 캐릭터 우편 처리 (사전예약 보상, 이벤트 보상 등)
     */
    private MailAcceptResponse handleCharacterMail(Mail mail, Long memberId) {
        // description에서 JSON 파싱
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());

        Long characterId = metadata.containsKey("characterId")
                ? ((Number) metadata.get("characterId")).longValue()
                : null;

        if (characterId == null) {
            log.error("Character ID not found in mail description: {}", mail.getDescription());
            throw new IllegalArgumentException("캐릭터 정보가 올바르지 않습니다.");
        }

        // Character 조회
        Character character = characterRepository.findById(characterId)
                .orElseThrow(CharacterNotFoundException::new);

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 캐릭터 지급 (공통 서비스 사용 - 중복 체크 및 자동 층수 배치 포함)
        MemberCharacter memberCharacter = characterCommandService.grantCharacter(member, character);

        if (memberCharacter == null) {
            log.warn("Member {} already owns character {}, skipping reward", memberId, characterId);
        }

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofCharacter(
                mail.getId(),
                mail.getTitle(),
                CharacterRewardInfo.from(character)
        );
    }

    /**
     * 캐릭터 선택권 우편 처리
     * @param characterId 선택할 캐릭터 ID (필수)
     */
    private MailAcceptResponse handleCharacterSelectionTicketMail(Mail mail, Long memberId, Long characterId) {
        // characterId 필수 검증
        if (characterId == null) {
            throw new IllegalArgumentException("캐릭터 선택권 우편은 캐릭터를 선택해야 합니다.");
        }

        // description에서 허용된 등급 파싱
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());
        String allowedRarityStr = (String) metadata.get("rarity");

        if (allowedRarityStr == null) {
            log.error("Rarity not found in mail description: {}", mail.getDescription());
            throw new IllegalArgumentException("캐릭터 선택권 정보가 올바르지 않습니다.");
        }

        // 선택한 캐릭터 조회
        Character selectedCharacter = characterRepository.findById(characterId)
                .orElseThrow(CharacterNotFoundException::new);

        // 등급 검증
        if (!selectedCharacter.getRarity().name().equals(allowedRarityStr)) {
            throw new IllegalArgumentException(
                    String.format("이 선택권으로는 %s 등급 캐릭터만 선택할 수 있습니다.", allowedRarityStr)
            );
        }

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 중복 보유 검증
        boolean alreadyOwned = memberCharacterRepository.existsByMemberIdAndCharacterId(
                memberId, characterId);
        if (alreadyOwned) {
            throw new IllegalArgumentException("이미 보유한 캐릭터입니다. 다른 캐릭터를 선택해주세요.");
        }

        // 캐릭터 지급 (공통 서비스 사용 - 중복 체크 및 자동 층수 배치 포함)
        MemberCharacter memberCharacter = characterCommandService.grantCharacter(member, selectedCharacter);

        // 우편 수령 처리
        mail.markAsReceived();

        log.info("Character selection ticket used: member {} selected character {} ({})",
                memberId, selectedCharacter.getName(), selectedCharacter.getRarity());

        return MailAcceptResponse.ofCharacter(
                mail.getId(),
                mail.getTitle(),
                CharacterRewardInfo.from(selectedCharacter)
        );
    }

    /**
     * Mail description JSON 파싱
     */
    private Map<String, Object> parseMailDescription(String description) {
        try {
            return objectMapper.readValue(description, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse mail description JSON: {}", description, e);
            throw new IllegalArgumentException("우편 메타데이터 파싱에 실패했습니다.");
        }
    }
}
