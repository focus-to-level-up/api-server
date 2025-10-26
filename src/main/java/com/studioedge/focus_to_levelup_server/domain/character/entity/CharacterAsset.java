package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "character_assets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CharacterAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_asset_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, length = 2048, nullable = false)
    private String assetUrl;
}
