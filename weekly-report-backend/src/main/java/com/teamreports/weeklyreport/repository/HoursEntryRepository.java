package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.dto.dashboard.TimeByTaskTypeResponse;
import com.teamreports.weeklyreport.entity.HoursEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HoursEntryRepository extends JpaRepository<HoursEntry, Long> {

    @Query("""
            select new com.teamreports.weeklyreport.dto.dashboard.TimeByTaskTypeResponse(
                h.taskType, coalesce(sum(h.hours), 0))
            from HoursEntry h
            join h.reportVersion v
            join v.report r
            where r.currentVersion = v
              and (:weekStart is null or r.weekStartDate = :weekStart)
            group by h.taskType
            """)
    List<TimeByTaskTypeResponse> findHoursByTaskType(@Param("weekStart") LocalDate weekStart);
}
