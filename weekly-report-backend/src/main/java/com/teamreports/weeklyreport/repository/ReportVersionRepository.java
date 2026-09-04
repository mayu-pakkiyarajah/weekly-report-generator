package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.ReportVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportVersionRepository extends JpaRepository<ReportVersion, Long> {
    List<ReportVersion> findByReportIdOrderByVersionNumberDesc(Long reportId);
    Optional<ReportVersion> findByReportIdAndVersionNumber(Long reportId, int versionNumber);
    int countByReportId(Long reportId);
}
