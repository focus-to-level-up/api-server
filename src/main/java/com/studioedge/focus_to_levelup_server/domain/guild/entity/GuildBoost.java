package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "guild_boosts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GuildBoost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_boost_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private LocalDate startDate; // 부스트 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 부스트 종료일

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isActive = true; // 활성화 여부

    @Builder
    public GuildBoost(Guild guild, Member member, LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.guild = guild;
        this.member = member;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive != null ? isActive : true;
    }

    // 비즈니스 메서드
    public void deactivate() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
}
