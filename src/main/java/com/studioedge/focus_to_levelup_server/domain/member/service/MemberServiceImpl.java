package com.studioedge.focus_to_levelup_server.domain.member.service;

import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.exception.SchoolNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.member.exception.*;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.study.dao.AllowedAppRepository;
import com.studioedge.focus_to_levelup_server.domain.study.entity.AllowedApp;
import com.studioedge.focus_to_levelup_server.domain.system.dao.AssetRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.ReportLogRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Asset;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final Set<CategoryMainType> SCHOOL_CATEGORIES = Set.of(
            CategoryMainType.ELEMENTARY_SCHOOL,
            CategoryMainType.MIDDLE_SCHOOL,
            CategoryMainType.HIGH_SCHOOL
    );

    // 수정 필요함
    public static final List<String> DEFAULT_PROFILE_NAME = List.of(
            "기본 프로필 이미지",
            "기본 테두리"
    );

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberAssetRepository memberAssetRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final AllowedAppRepository allowedAppRepository;
    private final SchoolRepository schoolRepository;
    private final AssetRepository assetRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReportLogRepository reportLogRepository;

    @Override
    @Transactional
    public void completeSignUp(Member member, CompleteSignUpRequest request) {
        validateSignUp(request);
        List<MemberAsset> memberAssets = saveInitialMemberAsset(member);
        saveMemberSetting(member);
        memberInfoRepository.save(CompleteSignUpRequest.from(member, memberAssets, request));
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

    /*
    * @TODO: N+1 문제 해결해야합니다. -> fetch join
    * */
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
        LocalDateTime updatedAt = member.getNicknameUpdatedAt();
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new NicknameUpdateException();
        }
        member.updateNickname(request.nickname());
    }

    @Override
    @Transactional
    public void updateCategory(Member member, UpdateCategoryRequest request) {
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
    public void updateAlarmSetting(Member member) {
        MemberSetting memberSetting = memberSettingRepository.findById(member.getId())
                .orElseThrow(InvalidMemberException::new);
        memberSetting.updateAlarmSetting();
    }

    @Override
    @Transactional
    public void updateAllowedApps(Member member, List<UpdateAllowedAppsRequest> requests) {
        List<AllowedApp> allowedApps = allowedAppRepository.findAllByMember(member);
        allowedAppRepository.deleteAll(allowedApps);
        List<AllowedApp> newAllowedApps = requests.stream().map(
                request -> UpdateAllowedAppsRequest.from(member, request)
        ).collect(Collectors.toList());
        allowedAppRepository.saveAll(newAllowedApps);
    }

    // ----------------------------- PRIVATE METHOD ---------------------------------

    private void validateSignUp(CompleteSignUpRequest request) {
        CategoryMainType mainCategory = request.categoryMain();
        CategorySubType subCategory = request.categorySub();

        if (SCHOOL_CATEGORIES.contains(mainCategory)) {
            // 1-1. 초/중/고 카테고리인데 학교 이름이 없는 경우
            if (request.schoolName() == null || request.schoolName().isBlank()) {
                throw new InvalidSignUpException();
            }
            // 1-2. 입력한 학교가 존재하는지 확인
            schoolRepository.findByName(request.schoolName())
                    .orElseThrow(SchoolNotFoundException::new);
        }
        // 2. 카테고리 상-하위 관계 검사
        if (subCategory.getMainType() != mainCategory) {
            throw new InvalidSignUpException();
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
        List<Asset> initialAssets = assetRepository.findAllByNameIn(DEFAULT_PROFILE_NAME);
        List<MemberAsset> memberAssets = new ArrayList<>();
        for(Asset asset : initialAssets) {
            memberAssets.add(MemberAsset.builder()
                    .member(member)
                    .asset(asset)
                    .build()
            );
        }
        return memberAssetRepository.saveAll(memberAssets);
    }

    private SubscriptionState getSubscriptionState(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId)
                .map(sub -> {
                    boolean isPremium = (sub.getType() == SubscriptionType.PREMIUM);
                    return new SubscriptionState(sub.getType(), isPremium);
                })
                .orElse(new SubscriptionState(SubscriptionType.NONE, false)); // 구독 정보가 없으면 기본값 반환
    }

    // ----------------------------- PRIVATE CLASS ---------------------------------
    private record SubscriptionState (SubscriptionType type, boolean isBoosted) {}
}
