package com.studioedge.system.repository;

import com.studioedge.system.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackgroundRepository extends JpaRepository<Background, Long> {
    Optional<Background> findByName(String name);
}
