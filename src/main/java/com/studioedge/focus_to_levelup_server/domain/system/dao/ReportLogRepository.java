package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.ReportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {
}
