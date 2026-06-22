package com.studioedge.focus.repository;

import com.studioedge.focus.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllBySubjectId(Long subjectId);
}
