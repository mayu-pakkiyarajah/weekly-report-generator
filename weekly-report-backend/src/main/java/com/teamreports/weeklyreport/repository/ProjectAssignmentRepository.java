package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByUserId(Long userId);
    List<ProjectAssignment> findByProjectId(Long projectId);
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);
}
