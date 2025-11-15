package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "character_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CharacterImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @Column(nullable = false)
    private Integer evolution;

    @Column(name = "image_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CharacterImageType imageType;

    @Column(name = "image_url", length = 2048, nullable = false)
    private String imageUrl;
}
