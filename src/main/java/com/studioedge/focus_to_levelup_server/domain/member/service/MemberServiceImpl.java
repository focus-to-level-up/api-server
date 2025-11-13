package com.studioedge.focus_to_levelup_server.domain.member.service;

import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.repository.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.entity.School;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.member.exception.*;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.repository.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.AssetRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.ReportLogRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberAssetRepository memberAssetRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final SchoolRepository schoolRepository;
    private final AssetRepository assetRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReportLogRepository reportLogRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional
    public void completeSignUp(Member member, CompleteSignUpRequest request) {
        validateSignUp(request);

        saveMemberSetting(member);
        saveInitialCharacter(member);
        List<MemberAsset> memberAssets = saveInitialMemberAsset(member);
        MemberInfo memberInfo = memberInfoRepository
                .save(CompleteSignUpRequest.from(member, memberAssets, request));
        memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new)
                .completeSignUp(request.nickname(), memberInfo);
    }

    @Override
    public void reportMember(Member reportFrom, Long memberId, ReportMemberRequest request) {
        Member reportTo = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        reportLogRepository.save(ReportMemberRequest.from(reportFrom, reportTo, request));
    }

    @Override
    @Transactional
    public void updateMemberProfile(Member member, UpdateProfileRequest request) {
        MemberInfo memberInfo = memberInfoRepository.findByMember(member)
                .orElseThrow(InvalidMemberException::new);
        MemberAsset newImage = memberAssetRepository.findById(request.profileImageId())
                .orElseThrow(AssetUnauthorizedException::new);
        MemberAsset newBorder = memberAssetRepository.findById(request.profileBorderId())
                .orElseThrow(AssetUnauthorizedException::new);

        validateMemberAsset(member, newImage, newBorder);
        memberInfo.updateProfile(newImage, newBorder, request.profileMessage());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileAssetResponse> getMemberAsset(Member member, Pageable pageable) {
        Page<MemberAsset> memberAssets = memberAssetRepository.findByMember(member, pageable);
        List<ProfileAssetResponse> responses = memberAssets.stream()
                .map(ProfileAssetResponse::of)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, memberAssets.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public GetProfileResponse getMemberProfile(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        SubscriptionState state = getSubscriptionState(memberId);
        return GetProfileResponse.of(
                memberInfo.getMember(),
                memberInfo,
                state.type(),
                state.isBoosted()
        );
    }

    @Override
    @Transactional
    public void updateNickname(Member member, UpdateNicknameRequest request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("해당 닉네임은 이미 존재합니다.");
        }
        LocalDateTime updatedAt = member.getNicknameUpdatedAt();
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new NicknameUpdateException();
        }
        Member me = memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        me.updateNickname(request.nickname());
    }

    @Override
    @Transactional
    public void updateCategory(Member member, UpdateCategoryRequest request) {
        if (!request.categorySub().getMainType().equals(request.categoryMain())) {
            throw new IllegalArgumentException("카테고리의 상하관계가 일치하지 않습니다.");
        }
        MemberInfo memberInfo = memberInfoRepository.findByMember(member)
                .orElseThrow(InvalidMemberException::new);
        LocalDateTime updatedAt = memberInfo.getCategoryUpdatedAt();
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new CategoryUpdateException();
        }
        memberInfo.updateCategory(request);
    }

    @Override
    @Transactional
    public void updateMemberSetting(Member member, MemberSettingDto request) {
        MemberSetting memberSetting = memberSettingRepository.findByMemberId(member.getId())
                .orElseThrow(InvalidMemberException::new);
        memberSetting.updateSetting(request);
    }

    @Override
    public MemberSettingDto getMemberSetting(Member member) {
        MemberSetting memberSetting = memberSettingRepository.findByMemberId(member.getId())
                .orElseThrow(InvalidMemberException::new);
        return MemberSettingDto.of(memberSetting);
    }

    // ----------------------------- PRIVATE METHOD ---------------------------------

    private void validateSignUp(CompleteSignUpRequest request) {
        CategoryMainType mainCategory = request.categoryMain();
        CategorySubType subCategory = request.categorySub();

        if (AppConstants.SCHOOL_CATEGORIES.contains(mainCategory)) {
            // 1-1. 초/중/고 카테고리인데 학교 이름이 없는 경우
            if (request.schoolName() == null || request.schoolName().isBlank()) {
                throw new InvalidSignUpException();
            }
            // 1-2. 입력한 학교가 존재하는지 확인
            // @TODO: 프론트와 합의 후, 학교 객체에 관해서 결정해야합니다.
            schoolRepository.findByName(request.schoolName())
                    .orElseGet(() -> {
                        School newSchool = School.builder()
                                .name(request.schoolName())
                                .categoryMain(mainCategory)
                                .build();
                        return schoolRepository.save(newSchool);
                    });
        }
        // 2. 카테고리 상-하위 관계 검사
        if (subCategory.getMainType() != mainCategory) {
            throw new InvalidSignUpException();
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("해당 닉네임은 이미 존재합니다.");
        }
    }

    private void validateMemberAsset(Member member, MemberAsset updateImage, MemberAsset updateBorder) {
        if (!updateImage.getMember().getId().equals(member.getId())) {
            throw new AssetUnauthorizedException();
        }
        if (!updateBorder.getMember().getId().equals(member.getId())) {
            throw new AssetUnauthorizedException();
        }
    }

    private void saveMemberSetting(Member member) {
        memberSettingRepository.save(
                MemberSetting.builder()
                        .member(member)
                        .build()
        );
    }

    private List<MemberAsset> saveInitialMemberAsset(Member member) {
        List<Asset> initialAssets = assetRepository.findAllByNameIn(AppConstants.DEFAULT_ASSET_NAMES);
        List<MemberAsset> memberAssets = new ArrayList<>();
        for (Asset asset : initialAssets) {
            memberAssets.add(MemberAsset.builder()
                    .member(member)
                    .asset(asset)
                    .build()
            );
        }
        return memberAssetRepository.saveAll(memberAssets);
    }

    private void saveInitialCharacter(Member member) {
        // 이미 대표 캐릭터가 있으면 예외
        if (memberCharacterRepository.existsByMemberIdAndIsDefaultTrue(member.getId())) {
            throw new IllegalStateException("이미 대표 캐릭터가 설정되어 있습니다.");
        }

        Character defaultCharacter = characterRepository.findByName(AppConstants.DEFAULT_CHARACTER_NAME)
                .orElseThrow(CharacterNotFoundException::new);

        MemberCharacter memberCharacter = MemberCharacter.builder()
                .character(defaultCharacter)
                .member(member)
                .floor(1)  // 초기 층수
                .build();

        // 양동동을 대표 캐릭터로 설정 (진화 단계 1)
        memberCharacter.setAsDefault(1);

        // 대표 캐릭터로 설정 후 저장
        memberCharacterRepository.save(memberCharacter);
    }

    private SubscriptionState getSubscriptionState(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId)
                .map(sub -> {
                    boolean isPremium = (sub.getType() == SubscriptionType.PREMIUM);
                    return new SubscriptionState(sub.getType(), isPremium);
                })
                .orElse(new SubscriptionState(SubscriptionType.NONE, false));
    }

    @Override
    @Transactional(readOnly = true)
    public MemberCurrencyResponse getMemberCurrency(Member member) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                .orElseThrow(InvalidMemberException::new);

        return MemberCurrencyResponse.builder()
                .level(member.getCurrentLevel())
                .gold(memberInfo.getGold())
                .diamond(memberInfo.getDiamond())
                .build();
    }

    @Override
    @Transactional
    public void updateCurrency(Long memberId, Integer gold, Integer diamond) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        if (gold != null) {
            memberInfo.setGold(gold);
        }
        if (diamond != null) {
            memberInfo.setDiamond(diamond);
        }
    }

    // ----------------------------- PRIVATE CLASS ---------------------------------
    private record SubscriptionState (SubscriptionType type, boolean isBoosted) {}
}
