package com.studioedge.promotion.entity;

import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_usage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public CouponLog(Member member, Coupon coupon) {
        this.member = member;
        this.coupon = coupon;
    }
}
