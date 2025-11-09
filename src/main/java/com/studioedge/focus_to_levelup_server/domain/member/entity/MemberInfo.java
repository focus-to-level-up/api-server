package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.character.exception.InsufficientDiamondException;
import com.studioedge.focus_to_levelup_server.domain.member.dto.UpdateCategoryRequest;
import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
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
    private Integer trainingReward = 0; // 훈련 보상 누적량 (다이아)

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

    public void claimTrainingReward() {
        this.diamond += this.trainingReward;
        this.trainingReward = 0;
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
}
