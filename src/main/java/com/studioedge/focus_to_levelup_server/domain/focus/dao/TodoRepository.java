package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllBySubjectId(Long subjectId);
=======
public interface TodoRepository extends JpaRepository<Todo, Long> {
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
}
