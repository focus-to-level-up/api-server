package com.studioedge.focus_to_levelup_server.domain.member.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.event.dao.SchoolRepository;
import com.studioedge.focus_to_levelup_server.domain.event.entity.School;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberAssetRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dto.*;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.exception.*;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.INITIAL_SUBJECT_COLORS;
import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

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
    private final RankingRepository rankingRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final SubjectRepository subjectRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final LeagueRepository leagueRepository;

    @Override
    @Transactional
    public void completeSignUp(Member member, CompleteSignUpRequest request) {
        validateSignUp(request);

        saveInitialCharacter(member);
        saveInitialSubjects(member);
        List<MemberAsset> memberAssets = saveInitialMemberAsset(member);
        MemberSetting memberSetting = saveMemberSetting(member);
        MemberInfo memberInfo = saveMemberInfo(member, memberAssets, request);
        memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new)
                .completeSignUp(request.nickname(), memberInfo, memberSetting);
        registerRanking(member, request.categoryMain());
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
        LocalDate today = getServiceDate();
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        SubscriptionState state = getSubscriptionState(memberId);
        String ranking = getMemberRanking(memberId);

        // 1. 전체 활성 유저 수 조회 (분모) - DB에서 숫자 하나만 가져옴 (매우 빠름)
        long totalUsers = memberRepository.countByStatus(MemberStatus.ACTIVE);

        // 2. 나의 오늘 공부 시간 조회 (기록 없으면 0초)
        int mySeconds = dailyGoalRepository.findFocusTimeByMemberIdAndDate(memberId, today)
                .orElse(0);
        float topPercent;

        if (mySeconds == 0) {
            // 공부 시간이 0초면 무조건 하위 100%로 고정
            topPercent = 100.0f;
        } else {
            // 3. 나보다 공부를 더 많이 한 사람 수 조회 (DB Count)
            long betterCount = dailyGoalRepository.countByDateAndFocusTimeGreaterThan(today, mySeconds);

            // 4. 내 등수 계산
            long myRank = betterCount + 1;

            // 5. 퍼센트 계산
            // 예: 100명 중 1등 -> 1.0%
            topPercent = (float) myRank / (float) totalUsers * 100;
        }

        return GetProfileResponse.of(
                memberInfo.getMember(),
                memberInfo,
                ranking,
                state.type(),
                state.isBoosted(),
                topPercent
        );
    }

    @Override
    @Transactional
    public void updateNickname(Member member, UpdateNicknameRequest request) {
        LocalDateTime updatedAt = member.getNicknameUpdatedAt();
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new NicknameUpdateException();
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("해당 닉네임은 이미 존재합니다.");
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

        List<GuildMember> memberWithGuilds = guildMemberRepository.findAllByMemberIdWithGuild(member.getId());
        for (GuildMember guildMember : memberWithGuilds) {
            if (guildMember.getRole().equals(GuildRole.LEADER)) {
                guildMember.getGuild().updateCategory(request.categorySub());
            }
        }
    }

    @Override
    @Transactional
    public void updateSchool(Member member, UpdateSchoolRequest request) {
        MemberInfo memberInfo = memberInfoRepository.findByMember(member)
                .orElseThrow(InvalidMemberException::new);
        CategoryMainType mainType = memberInfo.getCategoryMain();
        CategorySubType subType = memberInfo.getCategorySub();
        LocalDateTime updatedAt = memberInfo.getSchoolUpdatedAt();

        if (!subType.getMainType().equals(mainType)) {
            throw new InvalidSignUpException();
        }
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new CategoryUpdateException();
        }

        memberInfo.updateSchool(request);
        if (AppConstants.SCHOOL_CATEGORIES.contains(mainType) &&
                !subType.equals(CategorySubType.N_SU) &&
                request.schoolName() != null) {
            schoolRepository.findByName(request.schoolName())
                    .orElseGet(() -> {
                        School newSchool = School.builder()
                                .name(request.schoolName())
                                .categoryMain(mainType)
                                .build();
                        return schoolRepository.save(newSchool);
                    });
        }
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

        if (AppConstants.SCHOOL_CATEGORIES.contains(mainCategory) &&
            !request.categorySub().equals(CategorySubType.N_SU) &&
            request.schoolName() != null) {
            // 1. 입력한 학교가 존재하는지 확인
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

    private MemberSetting saveMemberSetting(Member member) {
       return memberSettingRepository.save(
                MemberSetting.builder()
                        .member(member)
                        .build()
        );
    }

    private MemberInfo saveMemberInfo(Member member, List<MemberAsset> memberAssets,
                                      CompleteSignUpRequest request) {
        return memberInfoRepository.save(
                CompleteSignUpRequest.from(
                        member,
                        memberAssets,
                        request
                )
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
        return subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .map(sub -> {
                    boolean isPremium = (sub.getType() == SubscriptionType.PREMIUM);
                    return new SubscriptionState(sub.getType(), isPremium);
                })
                .orElse(new SubscriptionState(SubscriptionType.NONE, false));
    }

    private String getMemberRanking(Long memberId) {
        Optional<Ranking> ranking = rankingRepository.findByMemberId(memberId);
        if (ranking.isEmpty()) {
            return "-";
        }
        else return ranking.get().getTier().toString();
    }

    private void saveInitialSubjects(Member member) {
        List<Subject> subjects = new ArrayList<>();
        for (int i=0; i<3; i++) {
            subjects.add(
                    Subject.builder()
                            .member(member)
                            .name("과목" + (i + 1))
                            .color(INITIAL_SUBJECT_COLORS[i])
                            .build()
            );
        }
        subjectRepository.saveAll(subjects);
    }

    private void registerRanking(Member member, CategoryMainType mainType) {
        League league = leagueRepository.findSmallestBronzeLeagueForCategory(mainType)
                .orElseThrow(LeagueNotFoundException::new);
        Ranking ranking = Ranking.builder()
                .league(league)
                .member(member)
                .tier(Tier.BRONZE)
                .build();
        rankingRepository.save(ranking);
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

    // ----------------------------- PRIVATE CLASS ---------------------------------
    private record SubscriptionState (SubscriptionType type, boolean isBoosted) {}
}
