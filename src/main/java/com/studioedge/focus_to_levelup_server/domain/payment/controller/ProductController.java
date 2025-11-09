package com.studioedge.focus_to_levelup_server.domain.payment.controller;

import com.studioedge.focus_to_levelup_server.domain.payment.dto.response.ProductDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.response.ProductListResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.service.ProductQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 조회 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @Operation(summary = "상품 목록 조회", description = "활성화된 모든 인앱 결제 상품을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<ProductListResponse>> getAllProducts() {
        ProductListResponse response = productQueryService.findAllActiveProducts();
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductDetailResponse>> getProductDetail(
            @PathVariable Long productId
    ) {
        ProductDetailResponse response = productQueryService.findProductDetail(productId);
        return HttpResponseUtil.ok(response);
    }
}
