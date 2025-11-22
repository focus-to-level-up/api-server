package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.system.enums.MonsterImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Monster monster;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MonsterImageType type;

    @Column(name = "image_url", length = 2048, nullable = false)
    private String imageUrl;
}
