package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    // pk 개선 가능
    @Column(unique = true, nullable = false)
    private String couponCode;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private RewardType rewardType;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int reward;

    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
