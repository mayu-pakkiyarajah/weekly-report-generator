package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Query("""
            select a from Achievement a
            join a.reportVersion v
            join v.report r
            where r.currentVersion = v and r.weekStartDate = :weekStart
            order by r.user.fullName
            """)
    List<Achievement> findAllForWeek(@Param("weekStart") LocalDate weekStart);
}
