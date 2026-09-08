package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.Report;
import com.teamreports.weeklyreport.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @EntityGraph(attributePaths = {"user", "project", "currentVersion"})
    Optional<Report> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);

    @EntityGraph(attributePaths = {"user", "project", "currentVersion"})
    Page<Report> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "project", "currentVersion"})
    @Query("""
            select r from Report r
            where (:userId is null or r.user.id = :userId)
              and (:projectId is null or r.project.id = :projectId)
              and (:status is null or r.status = :status)
              and (:weekStart is null or r.weekStartDate >= :weekStart)
              and (:weekEnd is null or r.weekEndDate <= :weekEnd)
            """)
    Page<Report> search(@Param("userId") Long userId,
                         @Param("projectId") Long projectId,
                         @Param("status") ReportStatus status,
                         @Param("weekStart") LocalDate weekStart,
                         @Param("weekEnd") LocalDate weekEnd,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"user", "project", "currentVersion"})
    List<Report> findByWeekStartDate(LocalDate weekStartDate);

    long countByStatusAndWeekStartDate(ReportStatus status, LocalDate weekStartDate);

    long countByWeekStartDate(LocalDate weekStartDate);
}
