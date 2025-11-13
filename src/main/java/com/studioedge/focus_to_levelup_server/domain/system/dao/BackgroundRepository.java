package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackgroundRepository extends JpaRepository<Background, Long> {
    Optional<Background> findByName(String name);
}
