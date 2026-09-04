package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    @Query("""
            select count(b) from Blocker b
            join b.reportVersion v
            join v.report r
            where r.currentVersion = v
              and r.weekStartDate = :weekStart
              and r.status <> com.teamreports.weeklyreport.entity.enums.ReportStatus.APPROVED
            """)
    long countOpenBlockersForWeek(@Param("weekStart") LocalDate weekStart);

    @Query("""
            select b from Blocker b
            join b.reportVersion v
            join v.report r
            where r.currentVersion = v and r.weekStartDate = :weekStart
            order by r.user.fullName
            """)
    java.util.List<Blocker> findAllForWeek(@Param("weekStart") LocalDate weekStart);
}
