package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.dto.dashboard.TasksTrendPointResponse;
import com.teamreports.weeklyreport.dto.dashboard.WorkloadByProjectResponse;
import com.teamreports.weeklyreport.entity.TaskEntry;
import com.teamreports.weeklyreport.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {

    @Query("""
            select new com.teamreports.weeklyreport.dto.dashboard.TasksTrendPointResponse(
                r.weekStartDate, count(t))
            from TaskEntry t
            join t.reportVersion v
            join v.report r
            where t.status = :status
              and r.currentVersion = v
              and r.weekStartDate >= :fromWeek
              and (:userId is null or r.user.id = :userId)
            group by r.weekStartDate
            order by r.weekStartDate
            """)
    List<TasksTrendPointResponse> findCompletionTrend(@Param("status") TaskStatus status,
                                                        @Param("fromWeek") LocalDate fromWeek,
                                                        @Param("userId") Long userId);

    @Query("""
            select new com.teamreports.weeklyreport.dto.dashboard.WorkloadByProjectResponse(
                r.project.id, r.project.name, count(t), coalesce(sum(t.timeSpentHours), 0))
            from TaskEntry t
            join t.reportVersion v
            join v.report r
            where r.currentVersion = v
              and (:weekStart is null or r.weekStartDate = :weekStart)
            group by r.project.id, r.project.name
            """)
    List<WorkloadByProjectResponse> findWorkloadByProject(@Param("weekStart") LocalDate weekStart);
}
