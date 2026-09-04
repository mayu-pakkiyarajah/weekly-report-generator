package com.teamreports.weeklyreport.service;

import com.teamreports.weeklyreport.dto.project.ProjectRequest;
import com.teamreports.weeklyreport.dto.project.ProjectResponse;
import com.teamreports.weeklyreport.entity.Project;
import com.teamreports.weeklyreport.entity.ProjectAssignment;
import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.exception.DuplicateResourceException;
import com.teamreports.weeklyreport.exception.ResourceNotFoundException;
import com.teamreports.weeklyreport.mapper.ProjectMapper;
import com.teamreports.weeklyreport.repository.ProjectAssignmentRepository;
import com.teamreports.weeklyreport.repository.ProjectRepository;
import com.teamreports.weeklyreport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public Page<ProjectResponse> listActiveProjects(Pageable pageable) {
        return projectRepository.findByActiveTrue(pageable).map(projectMapper::toResponse);
    }

    public Page<ProjectResponse> listAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("A project with this name already exists.");
        }
        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .active(true)
                .build();
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProjectOrThrow(id);
        project.setName(request.name());
        project.setDescription(request.description());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        // Soft delete: reports already tagged with this project must remain valid/reportable.
        Project project = findProjectOrThrow(id);
        project.setActive(false);
        projectRepository.save(project);
    }

    @Transactional
    public void assignUserToProject(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (projectAssignmentRepository.existsByUserIdAndProjectId(userId, projectId)) {
            return; // idempotent
        }

        projectAssignmentRepository.save(ProjectAssignment.builder()
                .project(project)
                .user(user)
                .build());
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
