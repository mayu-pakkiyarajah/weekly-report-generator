import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ProjectService } from '../../../core/services/project.service';
import { ProjectRequest, ProjectResponse } from '../../../core/models/project.model';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SpinnerComponent, ConfirmDialogComponent],
  templateUrl: './projects.component.html'
})
export class ProjectsComponent implements OnInit {
  projects = signal<ProjectResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  errorMessage = signal<string | null>(null);

  /** null = "add new" form; a project = editing that row inline. */
  editingId = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);
  showAddForm = signal(false);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(500)]
  });

  constructor(private readonly fb: FormBuilder, private readonly projectService: ProjectService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.projectService.listAll(0, 200).subscribe((page) => {
      this.projects.set(page.content);
      this.loading.set(false);
    });
  }

  startAdd(): void {
    this.editingId.set(null);
    this.showAddForm.set(true);
    this.form.reset({ name: '', description: '' });
  }

  startEdit(p: ProjectResponse): void {
    this.showAddForm.set(false);
    this.editingId.set(p.id);
    this.form.reset({ name: p.name, description: p.description ?? '' });
  }

  cancelForm(): void {
    this.showAddForm.set(false);
    this.editingId.set(null);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const request = this.form.getRawValue() as ProjectRequest;
    const editingId = this.editingId();

    const call = editingId
      ? this.projectService.update(editingId, request)
      : this.projectService.create(request);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelForm();
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Could not save the project.');
      }
    });
  }

  confirmDelete(id: number): void {
    this.pendingDeleteId.set(id);
  }

  cancelDelete(): void {
    this.pendingDeleteId.set(null);
  }

  deleteConfirmed(): void {
    const id = this.pendingDeleteId();
    if (!id) return;
    this.projectService.delete(id).subscribe(() => {
      this.pendingDeleteId.set(null);
      this.load();
    });
  }
}
