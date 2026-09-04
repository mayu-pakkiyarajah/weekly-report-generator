package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Project> findByActiveTrue(Pageable pageable);
}
