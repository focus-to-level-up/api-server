package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
