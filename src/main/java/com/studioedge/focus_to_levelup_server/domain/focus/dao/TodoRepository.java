package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
