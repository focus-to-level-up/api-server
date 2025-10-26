package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryMainType categoryMain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorySubType categorySub;

    @Column(nullable = false)
    @ColumnDefault()
    private LocalDateTime categoryUpdatedAt;

    @Column(nullable = false)
    @ColumnDefault("없음")
    private String belonging;

    private String profileMessage;


}
