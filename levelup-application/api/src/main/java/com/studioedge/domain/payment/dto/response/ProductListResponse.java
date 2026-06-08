package com.studioedge.domain.payment.dto.response;

import com.studioedge.domain.payment.dto.response.ProductSummary;

import java.util.List;

public record ProductListResponse(
        List<ProductSummary> products
) {
    public static ProductListResponse of(List<ProductSummary> products) {
        return new ProductListResponse(products);
    }
}
