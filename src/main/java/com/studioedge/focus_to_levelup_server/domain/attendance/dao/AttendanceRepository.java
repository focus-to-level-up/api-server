package com.studioedge.focus_to_levelup_server.domain.attendance.dao;

import com.studioedge.focus_to_levelup_server.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberId(Long memberId);
}
