import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';

import { ReportService } from '../../../core/services/report.service';
import { ProjectService } from '../../../core/services/project.service';
import { ProjectResponse } from '../../../core/models/project.model';
import { ReportResponse } from '../../../core/models/report.model';
import { PRIORITIES, TASK_STATUSES, TASK_TYPES } from '../../../core/models/report.model';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

function mondayOf(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay(); // 0 = Sunday
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  return d;
}
function toIso(d: Date): string {
  return d.toISOString().slice(0, 10);
}
function addDays(iso: string, days: number): string {
  const d = new Date(iso + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return toIso(d);
}

@Component({
  selector: 'app-report-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SpinnerComponent, StatusBadgeComponent],
  templateUrl: './report-editor.component.html',
  styleUrl: './report-editor.component.scss'
})
export class ReportEditorComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly priorities = PRIORITIES;
  readonly taskStatuses = TASK_STATUSES;
  readonly taskTypes = TASK_TYPES;

  mode: 'create' | 'edit' = 'create';
  reportId: number | null = null;
  report = signal<ReportResponse | null>(null);
  projects = signal<ProjectResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  submitError = signal<string | null>(null);
  saveConfirmation = signal<string | null>(null);

  latestCorrectionComment = computed<string | null>(() => {
    const r = this.report();
    if (!r || r.status !== 'NEEDS_CORRECTION') return null;
    const latest = r.reviewHistory?.find((c) => c.action === 'CHANGES_REQUESTED');
    return latest?.comment ?? null;
  });

  createForm: FormGroup = this.fb.group({
    weekStartDate: [toIso(mondayOf(new Date())), Validators.required],
    projectId: [null, Validators.required]
  });

  contentForm: FormGroup = this.fb.group({
    tasksCompleted: this.fb.array([]),
    tasksPlannedNextWeek: [''],
    blockers: this.fb.array([]),
    achievements: this.fb.array([]),
    hoursByTaskType: this.fb.array(this.taskTypes.map((t) => this.fb.group({ taskType: [t], hours: [0] }))),
    notes: [''],
    links: ['']
  });

  get tasksCompleted(): FormArray { return this.contentForm.get('tasksCompleted') as FormArray; }
  get blockers(): FormArray { return this.contentForm.get('blockers') as FormArray; }
  get achievements(): FormArray { return this.contentForm.get('achievements') as FormArray; }
  get hoursByTaskType(): FormArray { return this.contentForm.get('hoursByTaskType') as FormArray; }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly reportService: ReportService,
    private readonly projectService: ProjectService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.mode = idParam ? 'edit' : 'create';
    this.reportId = idParam ? Number(idParam) : null;

    if (this.mode === 'create') {
      this.projectService.listActive(0, 100)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((page) => {
          this.projects.set(page.content);
          this.loading.set(false);
        });
    } else {
      this.loadForEdit();
    }
  }

  private loadForEdit(): void {
    forkJoin({
      report: this.reportService.getOwn(this.reportId!),
      projects: this.projectService.listActive(0, 100)
    })
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: ({ report, projects }) => {
        this.projects.set(projects.content);

        if (report.status !== 'DRAFT' && report.status !== 'NEEDS_CORRECTION') {
          // Only draft / needs-correction reports are editable - send to the read-only view instead.
          this.router.navigate(['/reports', report.id]);
          return;
        }

        this.report.set(report);
        this.populateContent(report);
        this.loading.set(false);
      },
      error: () => this.router.navigate(['/reports'])
    });
  }

  private populateContent(report: ReportResponse): void {
    const v = report.currentVersion;

    this.tasksCompleted.clear();
    (v.tasksCompleted ?? []).forEach((t) => this.tasksCompleted.push(this.taskGroup(t)));

    this.blockers.clear();
    (v.blockers ?? []).forEach((b) => this.blockers.push(this.blockerGroup(b)));

    this.achievements.clear();
    (v.achievements ?? []).forEach((a) => this.achievements.push(this.achievementGroup(a)));

    this.taskTypes.forEach((type, i) => {
      const existing = (v.hoursByTaskType ?? []).find((h) => h.taskType === type);
      this.hoursByTaskType.at(i).patchValue({ hours: existing?.hours ?? 0 });
    });

    this.contentForm.patchValue({
      tasksPlannedNextWeek: v.tasksPlannedNextWeek ?? '',
      notes: v.notes ?? '',
      links: v.links ?? ''
    });
  }

  private taskGroup(t?: Partial<{ taskName: string; priority: string; plannedPercent: number; actualPercent: number; status: string; timePlannedHours: number; timeSpentHours: number; outputDeliverable: string; }>): FormGroup {
    return this.fb.group({
      taskName: [t?.taskName ?? '', Validators.required],
      priority: [t?.priority ?? 'MEDIUM', Validators.required],
      plannedPercent: [t?.plannedPercent ?? 0, [Validators.min(0), Validators.max(100)]],
      actualPercent: [t?.actualPercent ?? 0, [Validators.min(0), Validators.max(100)]],
      status: [t?.status ?? 'NOT_STARTED', Validators.required],
      timePlannedHours: [t?.timePlannedHours ?? 0, Validators.min(0)],
      timeSpentHours: [t?.timeSpentHours ?? 0, Validators.min(0)],
      outputDeliverable: [t?.outputDeliverable ?? '']
    });
  }
  private blockerGroup(b?: Partial<{ description: string; keyIssue: boolean }>): FormGroup {
    return this.fb.group({
      description: [b?.description ?? '', Validators.required],
      keyIssue: [b?.keyIssue ?? false]
    });
  }
  private achievementGroup(a?: Partial<{ description: string; keyAchievement: boolean }>): FormGroup {
    return this.fb.group({
      description: [a?.description ?? '', Validators.required],
      keyAchievement: [a?.keyAchievement ?? false]
    });
  }

  addTask(): void { this.tasksCompleted.push(this.taskGroup()); }
  removeTask(i: number): void { this.tasksCompleted.removeAt(i); }
  addBlocker(): void { this.blockers.push(this.blockerGroup()); }
  removeBlocker(i: number): void { this.blockers.removeAt(i); }
  addAchievement(): void { this.achievements.push(this.achievementGroup()); }
  removeAchievement(i: number): void { this.achievements.removeAt(i); }

  /** Only one blocker/achievement may be flagged as "key" at a time. */
  setKeyBlocker(i: number): void {
    this.blockers.controls.forEach((c, idx) => c.get('keyIssue')!.setValue(idx === i));
  }
  setKeyAchievement(i: number): void {
    this.achievements.controls.forEach((c, idx) => c.get('keyAchievement')!.setValue(idx === i));
  }

  createDraft(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.submitError.set(null);
    const { weekStartDate, projectId } = this.createForm.getRawValue();

    this.reportService.createDraft({
      weekStartDate,
      weekEndDate: addDays(weekStartDate, 6),
      projectId
    })
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: (report) => this.router.navigate(['/reports', report.id, 'edit']),
      error: (err) => {
        this.saving.set(false);
        this.submitError.set(err?.error?.message ?? 'Could not create the report.');
      }
    });
  }

  private buildContentPayload() {
    return this.contentForm.getRawValue();
  }

  saveDraft(): void {
    this.saving.set(true);
    this.submitError.set(null);
    this.saveConfirmation.set(null);

    this.reportService.updateContent(this.reportId!, this.buildContentPayload())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (report) => {
          this.report.set(report);
          this.saving.set(false);
          this.saveConfirmation.set('Draft saved.');
        },
        error: (err) => {
          this.saving.set(false);
          this.submitError.set(err?.error?.message ?? 'Could not save the report.');
        }
      });
  }

  submitForReview(): void {
    if (this.contentForm.invalid) {
      this.contentForm.markAllAsTouched();
      this.submitError.set('Please fix the highlighted fields before submitting.');
      return;
    }
    this.saving.set(true);
    this.submitError.set(null);

    this.reportService.updateContent(this.reportId!, this.buildContentPayload())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.reportService.submit(this.reportId!)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => this.router.navigate(['/reports', this.reportId]),
              error: (err) => {
                this.saving.set(false);
                this.submitError.set(err?.error?.message ?? 'Could not submit the report.');
              }
            });
        },
        error: (err) => {
          this.saving.set(false);
          this.submitError.set(err?.error?.message ?? 'Could not save the report before submitting.');
        }
      });
  }
}
