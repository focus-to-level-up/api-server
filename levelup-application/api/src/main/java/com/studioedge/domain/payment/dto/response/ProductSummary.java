package com.studioedge.domain.payment.dto.response;

import com.studioedge.payment.entity.Product;
import com.studioedge.payment.enums.ProductType;

public record ProductSummary(
        Long productId,
        String name,
        ProductType type,
        Integer diamondReward,
        String price,
        String description
) {
    public static ProductSummary from(Product product) {
        return new ProductSummary(
                product.getId(),
                product.getName(),
                product.getType(),
                product.getDiamondReward(),
                product.getPrice().toString(),
                product.getDescription()
        );
    }
}
