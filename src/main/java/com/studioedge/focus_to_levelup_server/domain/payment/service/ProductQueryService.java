package com.studioedge.focus_to_levelup_server.domain.payment.service;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.response.ProductDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.response.ProductListResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.response.ProductSummary;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.ProductNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;

    /**
     * 활성화된 모든 상품 조회
     */
    public ProductListResponse findAllActiveProducts() {
        List<Product> products = productRepository.findAllByIsActiveTrue();
        List<ProductSummary> productSummaries = products.stream()
                .map(ProductSummary::from)
                .toList();
        return ProductListResponse.of(productSummaries);
    }

    /**
     * 상품 상세 조회
     */
    public ProductDetailResponse findProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductDetailResponse.from(product);
    }

    /**
     * 내부용: Product 엔티티 조회
     */
    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
    }
}
