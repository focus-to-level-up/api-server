package com.studioedge.focus_to_levelup_server.domain.payment.dto.response;

import java.util.List;

public record ProductListResponse(
        List<ProductSummary> products
) {
    public static ProductListResponse of(List<ProductSummary> products) {
        return new ProductListResponse(products);
    }
}
