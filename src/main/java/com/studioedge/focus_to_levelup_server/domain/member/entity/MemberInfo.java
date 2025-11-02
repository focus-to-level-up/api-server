package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.domain.store.exception.InsufficientGoldException;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
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
@Table(name = "member_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
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
    private String belonging = "없음";

    private String profileMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id") // Asset ID 참조
    private MemberAsset profileImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "border_id") // Asset ID 참조
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
                      MemberAsset profileImage, MemberAsset profileBorder, Integer gold, Integer diamond)
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
        this.gold = gold;
        this.diamond = diamond;
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
}
