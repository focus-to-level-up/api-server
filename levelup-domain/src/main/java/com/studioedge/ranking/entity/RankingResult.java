package com.studioedge.ranking.entity;

import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
import com.studioedge.ranking.enums.Tier;
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

    @Column(nullable = false)
    private Integer ranking;

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
        this.ranking = ranking;
        this.totalCount = totalCount;
        this.reward = reward;
    }
}
