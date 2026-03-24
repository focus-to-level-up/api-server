package com.studioedge.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
}
