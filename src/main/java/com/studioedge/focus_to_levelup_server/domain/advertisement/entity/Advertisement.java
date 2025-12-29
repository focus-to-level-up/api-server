package com.studioedge.focus_to_levelup_server.domain.advertisement.entity;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advertisements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long id;

    @ElementCollection(targetClass = CategorySubType.class)
    @CollectionTable(
            name = "advertisement_categories",
            joinColumns = @JoinColumn(name = "advertisement_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category_sub")
    private List<CategorySubType> categorySubs = new ArrayList<>();

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long clickCount = 0L;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long viewCount = 0L;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isActive = true;
}
