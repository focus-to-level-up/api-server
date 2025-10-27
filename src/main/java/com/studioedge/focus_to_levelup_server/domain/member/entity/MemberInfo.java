package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryMainType categoryMain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorySubType categorySub;

    // 유저 생성 시점에는 null -> (null || 1달이 지났을 경우) 업데이트 가능.
    private LocalDateTime categoryUpdatedAt;

    @Column(nullable = false)
    @ColumnDefault("없음")
    private String belonging;

    private String profileMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_asset_id") // Asset ID 참조
    private MemberAsset profileImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_asset_id") // Asset ID 참조
    private MemberAsset profileBorder;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int gold;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int diamond;
}
