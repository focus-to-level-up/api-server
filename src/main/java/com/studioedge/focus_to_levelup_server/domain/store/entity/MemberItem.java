package com.studioedge.focus_to_levelup_server.domain.store.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "member_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer selection;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    private LocalDate completedDate;

    @Column(nullable = false)
    private Boolean isRewardReceived = false;

    @Builder
    public MemberItem(Member member, Item item, Integer selection) {
        this.member = member;
        this.item = item;
        this.selection = selection;
    }

    // 비즈니스 메서드
    public void complete(LocalDate date) {
        this.isCompleted = true;
        this.completedDate = date;
    }

    public void receiveReward() {
        this.isRewardReceived = true;
    }
}
