import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { managerGuard } from './core/guards/manager.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'reports' },

      // ---- Team-member-facing report pages ----
      {
        path: 'reports',
        loadComponent: () =>
          import('./features/reports/report-history/report-history.component').then((m) => m.ReportHistoryComponent)
      },
      {
        path: 'reports/new',
        loadComponent: () =>
          import('./features/reports/report-editor/report-editor.component').then((m) => m.ReportEditorComponent)
      },
      {
        path: 'reports/:id/edit',
        loadComponent: () =>
          import('./features/reports/report-editor/report-editor.component').then((m) => m.ReportEditorComponent)
      },
      {
        path: 'reports/:id',
        loadComponent: () =>
          import('./features/reports/report-detail/report-detail.component').then((m) => m.ReportDetailComponent)
      },

      // ---- Manager-only pages ----
      {
        path: 'manager/dashboard',
        canActivate: [managerGuard],
        loadComponent: () =>
          import('./features/manager/team-dashboard/team-dashboard.component').then((m) => m.TeamDashboardComponent)
      },
      {
        path: 'manager/reports',
        canActivate: [managerGuard],
        loadComponent: () =>
          import('./features/manager/manager-reports/manager-reports.component').then((m) => m.ManagerReportsComponent)
      },
      {
        path: 'manager/reports/:id/review',
        canActivate: [managerGuard],
        loadComponent: () => import('./features/manager/review/review.component').then((m) => m.ReviewComponent)
      },
      {
        path: 'manager/reports/:id',
        canActivate: [managerGuard],
        loadComponent: () =>
          import('./features/manager/manager-report-detail/manager-report-detail.component')
            .then((m) => m.ManagerReportDetailComponent)
      },
      {
        path: 'manager/projects',
        canActivate: [managerGuard],
        loadComponent: () =>
          import('./features/manager/projects/projects.component').then((m) => m.ProjectsComponent)
      },
      {
        path: 'manager/users',
        canActivate: [managerGuard],
        loadComponent: () => import('./features/manager/users/users.component').then((m) => m.UsersComponent)
      },
      {
        path: 'manager/members/:id',
        canActivate: [managerGuard],
        loadComponent: () =>
          import('./features/manager/member-profile/member-profile.component').then((m) => m.MemberProfileComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'reports' }
];
