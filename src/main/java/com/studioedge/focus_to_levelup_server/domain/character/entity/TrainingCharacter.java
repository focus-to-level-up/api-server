package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_characters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TrainingCharacter extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_character_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_character_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MemberCharacter character;

    @Column(nullable = false)
    private Integer reward;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceived = false;

    @Builder
    public TrainingCharacter(MemberCharacter character, Integer reward, Integer floor,
                             LocalDateTime startDateTime, LocalDateTime endDateTime)
    {
        this.character = character;
        this.reward = reward;
        this.floor = floor;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
