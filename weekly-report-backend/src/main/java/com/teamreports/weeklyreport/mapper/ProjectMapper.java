package com.teamreports.weeklyreport.mapper;

import com.teamreports.weeklyreport.dto.project.ProjectResponse;
import com.teamreports.weeklyreport.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.isActive());
    }
}
