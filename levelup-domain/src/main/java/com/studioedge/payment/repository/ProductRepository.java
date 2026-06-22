package com.studioedge.payment.repository;

import com.studioedge.payment.entity.Product;
import com.studioedge.payment.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIsActiveTrue();

    List<Product> findAllByTypeAndIsActiveTrue(ProductType type);

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Optional<Product> findByTypeAndIsActiveTrue(ProductType type);
}
