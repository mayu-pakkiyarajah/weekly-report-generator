import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { UserService } from '../../../core/services/user.service';
import { CreateUserRequest, UpdateUserRequest, UserResponse } from '../../../core/models/user.model';
import { Role } from '../../../core/models/auth.model';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

const ROLES: Role[] = ['TEAM_MEMBER', 'MANAGER'];

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SpinnerComponent, ConfirmDialogComponent],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  readonly roles = ROLES;

  users = signal<UserResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  errorMessage = signal<string | null>(null);
  showInviteForm = signal(false);
  pendingDeactivateId = signal<number | null>(null);

  inviteForm = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    temporaryPassword: ['', [Validators.required, Validators.minLength(8)]],
    role: ['TEAM_MEMBER' as Role, Validators.required]
  });

  constructor(private readonly fb: FormBuilder, private readonly userService: UserService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.userService.list(0, 200).subscribe((page) => {
      this.users.set(page.content);
      this.loading.set(false);
    });
  }

  toggleInviteForm(): void {
    this.showInviteForm.set(!this.showInviteForm());
    this.inviteForm.reset({ fullName: '', email: '', temporaryPassword: '', role: 'TEAM_MEMBER' });
  }

  invite(): void {
    if (this.inviteForm.invalid) {
      this.inviteForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);

    this.userService.create(this.inviteForm.getRawValue() as CreateUserRequest).subscribe({
      next: () => {
        this.saving.set(false);
        this.showInviteForm.set(false);
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Could not create the account.');
      }
    });
  }

  changeRole(user: UserResponse, role: Role): void {
    if (role === user.role) return;
    const request: UpdateUserRequest = { role, active: user.active };
    this.userService.update(user.id, request).subscribe(() => this.load());
  }

  confirmDeactivate(id: number): void {
    this.pendingDeactivateId.set(id);
  }

  cancelDeactivate(): void {
    this.pendingDeactivateId.set(null);
  }

  deactivateConfirmed(): void {
    const id = this.pendingDeactivateId();
    if (!id) return;
    this.userService.deactivate(id).subscribe(() => {
      this.pendingDeactivateId.set(null);
      this.load();
    });
  }

  reactivate(user: UserResponse): void {
    this.userService.update(user.id, { role: user.role, active: true }).subscribe(() => this.load());
  }
}
