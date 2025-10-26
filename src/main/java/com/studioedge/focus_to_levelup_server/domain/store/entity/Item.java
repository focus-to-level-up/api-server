package com.studioedge.focus_to_levelup_server.domain.store.entity;

import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false)
    private int selection;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int rewardExp;
}
