package com.studioedge.focus_to_levelup_server.domain.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_detail_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private Integer parameter;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "reward_level", nullable = false)
    private Integer rewardLevel;
}
