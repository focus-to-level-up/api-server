package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Monster;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MonsterImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterImageRepository extends JpaRepository<MonsterImage, Long> {
    List<MonsterImage> findAllByMonster(Monster monster);
}
