package com.teamreports.weeklyreport.controller;

import com.teamreports.weeklyreport.dto.project.ProjectAssignmentRequest;
import com.teamreports.weeklyreport.dto.project.ProjectRequest;
import com.teamreports.weeklyreport.dto.project.ProjectResponse;
import com.teamreports.weeklyreport.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** Any authenticated user can see active projects (needed to tag their own reports). */
    @GetMapping
    public Page<ProjectResponse> listActiveProjects(Pageable pageable) {
        return projectService.listActiveProjects(pageable);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    public Page<ProjectResponse> listAllProjects(Pageable pageable) {
        return projectService.listAllProjects(pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> assignUser(@PathVariable Long id, @Valid @RequestBody ProjectAssignmentRequest request) {
        projectService.assignUserToProject(id, request.userId());
        return ResponseEntity.noContent().build();
    }
}
