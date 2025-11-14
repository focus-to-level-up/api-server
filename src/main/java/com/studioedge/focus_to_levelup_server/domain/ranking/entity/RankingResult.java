package com.studioedge.focus_to_levelup_server.domain.ranking.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "ranking_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private Tier tier;

    @Column(name = "user_rank", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Integer totalCount; // 총인원?

    @Column(nullable = false)
    private Integer reward;

    @Builder
    public RankingResult(League league, Member member, Tier tier,
                         Integer ranking, Integer totalCount, Integer reward)
    {
        this.league = league;
        this.member = member;
        this.tier = tier;
        this.rank = ranking;
        this.totalCount = totalCount;
        this.reward = reward;
    }
}
