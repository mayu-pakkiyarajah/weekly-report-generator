package com.teamreports.weeklyreport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "report_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_id", "version_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private int versionNumber;

    @Column(name = "tasks_planned_next_week", length = 10000)
    private String tasksPlannedNextWeek;

    @Column(length = 10000)
    private String notes;

    @Column(length = 1000)
    private String links;

    /** Set only once this version is submitted for review; null while still a draft. */
    @Column(name = "submitted_at")
    private Instant submittedAt;

    @OneToMany(mappedBy = "reportVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskEntry> taskEntries = new ArrayList<>();

    @OneToMany(mappedBy = "reportVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Blocker> blockers = new ArrayList<>();

    @OneToMany(mappedBy = "reportVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Achievement> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "reportVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HoursEntry> hoursEntries = new ArrayList<>();
}
