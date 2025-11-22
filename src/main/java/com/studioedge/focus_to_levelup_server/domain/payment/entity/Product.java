package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 999, nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(name = "diamond_reward")
    private Integer diamondReward;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private Boolean isActive = true;

    @Builder
    public Product(String name, String description, BigDecimal price, ProductType type,
                   Integer diamondReward, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.diamondReward = diamondReward;
        this.isActive = isActive != null ? isActive : true;
    }
}
