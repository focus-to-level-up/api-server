package com.studioedge.domain.payment.service;

import com.studioedge.payment.repository.ProductRepository;
import com.studioedge.domain.payment.dto.response.ProductListResponse;
import com.studioedge.domain.payment.dto.response.ProductSummary;
import com.studioedge.payment.entity.Product;
import com.studioedge.payment.exception.ProductNotFoundException;
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
     * 내부용: Product 엔티티 조회
     */
    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
    }
}
