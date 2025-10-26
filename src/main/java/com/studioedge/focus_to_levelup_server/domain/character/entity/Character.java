package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @OneToMany(mappedBy = "character", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharacterImage> characterImages = new ArrayList<>();

    @OneToMany(mappedBy = "character", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharacterAsset> characterAssets = new ArrayList<>();

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 500, nullable = false)
    private String description;
}
