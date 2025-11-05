package com.studioedge.focus_to_levelup_server.domain.event.dao;

import com.studioedge.focus_to_levelup_server.domain.event.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByName(String schoolName);
}
