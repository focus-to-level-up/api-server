package com.studioedge.focus.entity;

import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    private String name;

    @Column(nullable = false)
    private String color;

    private LocalDateTime deleteAt; // soft delete

    @Builder
    public Subject(String name, Member member, String color) {
        this.name = name;
        this.member = member;
        this.color = color;
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
        this.deleteAt = null;
    }

    public void delete() {
        this.deleteAt = LocalDateTime.now();
    }
}
