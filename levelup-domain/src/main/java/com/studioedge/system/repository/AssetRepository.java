package com.studioedge.system.repository;

import com.studioedge.system.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findAllByNameIn(List<String> names);

    Optional<Asset> findByName(String name);
}
