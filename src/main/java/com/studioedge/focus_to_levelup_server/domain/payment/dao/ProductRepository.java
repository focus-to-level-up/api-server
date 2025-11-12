package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIsActiveTrue();

    List<Product> findAllByTypeAndIsActiveTrue(ProductType type);

    Optional<Product> findByIdAndIsActiveTrue(Long id);
}
