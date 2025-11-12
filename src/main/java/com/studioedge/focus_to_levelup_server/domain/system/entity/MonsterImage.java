package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.system.enums.MonsterImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monster_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonsterImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monster_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", nullable = false)
    private Monster monster;

    @Column(nullable = false)
    private MonsterImageType type;

    @Column(length = 2048, nullable = false)
    private String imageUrl;
}
