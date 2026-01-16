package com.studioedge.focus_to_levelup_server.domain.promotion.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "referral", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"invitee_id"}) // 한 사람은 한 번만 초대코드 입력 가능
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Referral extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private Member inviter; // 초대 한 사람 (보상 받는 사람)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    private Member invitee; // 초대 받은 사람 (코드 입력한 사람)

    @Builder
    public Referral(Member inviter, Member invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
    }
}
