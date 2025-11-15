package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.system.enums.BackgroundImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backgrounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Background {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "background_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // 배경 이미지 타입(ex: FOCUS(집중할때), TRAINING(훈련장)) -> 향후 캐릭터마다 배경이 바뀌거나 할때를 위해서
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BackgroundImageType type;

    @Column(name = "image_url", length = 2048, nullable = false)
    private String imageUrl;
}
