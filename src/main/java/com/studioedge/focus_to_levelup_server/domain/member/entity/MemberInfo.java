package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.character.exception.InsufficientDiamondException;
import com.studioedge.focus_to_levelup_server.domain.member.dto.UpdateCategoryRequest;
import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.store.exception.InsufficientGoldException;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_infos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_image_id", "profile_border_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryMainType categoryMain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorySubType categorySub;

    // 유저 생성할 때 null.
    // 수정하고 싶다면, (null || 1달이 지났을 경우) 업데이트 가능.
    private LocalDateTime categoryUpdatedAt;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer totalLevel = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer totalExp = 0;

    private Tier highestTier;

    @Column(nullable = false)
    @ColumnDefault("'없음'")
    private String belonging;

    private String profileMessage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id") // Asset ID 참조
    private MemberAsset profileImage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_border_id") // Asset ID 참조
    private MemberAsset profileBorder;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer gold = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer diamond = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer trainingReward = 0; // 훈련 보상 누적량 (분×시급 단위, 수령 시 60으로 나눠 다이아 지급)

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer bonusTicketCount = 0; // 보너스 티켓 보유 개수 (주간 보상 10% 증가)

    @Builder
    public MemberInfo(Member member, Integer age, Gender gender, CategoryMainType categoryMain,
                      CategorySubType categorySub, String belonging, String profileMessage,
                      MemberAsset profileImage, MemberAsset profileBorder)
    {
        this.member = member;
        this.age = age;
        this.gender = gender;
        this.categoryMain = categoryMain;
        this.categorySub = categorySub;
        this.belonging = belonging;
        this.profileMessage = profileMessage;
        this.profileImage = profileImage;
        this.profileBorder = profileBorder;
    }

    // 비즈니스 메서드
    public void addTrainingReward(Integer reward) {
        this.trainingReward += reward;
    }

    /**
     * 훈련 보상 수령 (분×시급 → 다이아 변환)
     * @return 수령한 다이아 수량
     */
    public int claimTrainingReward() {
        int diamondReward = this.trainingReward / 60;
        int remainingMinutes = this.trainingReward % 60;
        this.diamond += diamondReward;
        this.trainingReward = remainingMinutes;
        return diamondReward;
    }

    public void decreaseGold(Integer amount) {
        if (this.gold < amount) {
            throw new InsufficientGoldException();
        }
        this.gold -= amount;
    }

    public void addGold(Integer amount) {
        this.gold += amount;
    }

    public void addDiamond(Integer amount) {
        this.diamond += amount;
    }

    public void decreaseDiamond(Integer amount) {
        if (this.diamond < amount) {
            throw new InsufficientDiamondException();
        }
        this.diamond -= amount;
    }

    public void updateCategory(UpdateCategoryRequest request) {
        this.categoryMain = request.categoryMain();
        this.categorySub = request.categorySub();
        this.categoryUpdatedAt = LocalDateTime.now();
    }

    public void updateProfile(MemberAsset updateImage, MemberAsset updateBorder, String profileMessage) {
        if (updateImage.getAsset().getType() != AssetType.CHARACTER_PROFILE_IMAGE) {
            throw new IllegalArgumentException("제공된 에셋이 프로필 이미지가 아닙니다.");
        }
        if (updateBorder.getAsset().getType() != AssetType.CHARACTER_PROFILE_BORDER) {
            throw new IllegalArgumentException("제공된 에셋이 테두리가 아닙니다.");
        }
        this.profileMessage = profileMessage;
        this.profileImage = updateImage;
        this.profileBorder = updateBorder;
    }

    // 테스트용 메서드
    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public void setDiamond(Integer diamond) {
        this.diamond = diamond;
    }

    // 총 레벨 업
    public void totalLevelUp(Integer exp) {
        this.totalExp += exp;
        if (this.totalExp >= 600) {
            this.totalLevel += (this.totalExp / 600);
            this.totalExp %= 600;
        }
    }

    public void addTotalLevel(Integer level) {
        this.totalLevel += level;
    }

    // 보너스 티켓 추가
    public void addBonusTicket(Integer count) {
        this.bonusTicketCount += count;
    }

    // 보너스 티켓 사용
    public void useBonusTicket() {
        if (this.bonusTicketCount <= 0) {
            throw new IllegalStateException("보유한 보너스 티켓이 없습니다.");
        }
        this.bonusTicketCount -= 1;
    }

    // 보너스 티켓 차감 (환불 시 사용)
    public void decreaseBonusTicket(Integer count) {
        if (this.bonusTicketCount < count) {
            throw new IllegalStateException("보유한 보너스 티켓이 부족합니다.");
        }
        this.bonusTicketCount -= count;
    }

    /**
     * 보너스 티켓 강제 차감 (환불 정책용 - 음수 허용)
     * @param count 차감할 개수
     * @return 차감 후 보너스 티켓 수 (음수 가능)
     */
    public int forceDecreaseBonusTicket(Integer count) {
        this.bonusTicketCount -= count;
        return this.bonusTicketCount;
    }

    /**
     * 다이아 강제 차감 (환불 정책용 - 음수 허용)
     * @param amount 차감할 개수
     * @return 차감 후 다이아 수 (음수 가능)
     */
    public int forceDecreaseDiamond(Integer amount) {
        this.diamond -= amount;
        return this.diamond;
    }

    /**
     * 다이아 차감 (선물 환불용 - 음수 불허, 최소 0까지만)
     * @param amount 차감할 개수
     * @return 실제 차감된 개수
     */
    public int decreaseDiamondToZero(Integer amount) {
        int actualDecrease = Math.min(this.diamond, amount);
        this.diamond -= actualDecrease;
        return actualDecrease;
    }

    /**
     * 보너스 티켓 차감 (선물 환불용 - 음수 불허, 최소 0까지만)
     * @param count 차감할 개수
     * @return 실제 차감된 개수
     */
    public int decreaseBonusTicketToZero(Integer count) {
        int actualDecrease = Math.min(this.bonusTicketCount, count);
        this.bonusTicketCount -= actualDecrease;
        return actualDecrease;
    }
}
