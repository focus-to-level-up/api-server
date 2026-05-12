package com.studioedge.promotion.repository;

import com.studioedge.promotion.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberId(Long memberId);
}
