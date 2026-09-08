# Weekly Report Generator & Team Dashboard — Frontend

Angular 18 (standalone components, signals) single-page app for the Weekly Report
Generator & Team Dashboard assignment. Talks to the Spring Boot backend over REST.

## 1. Prerequisites

- Node.js 18+ and npm
- The backend running locally (see `weekly-report-backend/README.md`) — by default this
  app expects it at `http://localhost:8080/api`

## 2. Installing dependencies

```bash
cd weekly-report-frontend
npm install
```

## 3. Running the frontend

```bash
npm start
```

The app runs at **http://localhost:4200** and proxies API calls directly to
`http://localhost:8080/api` (see `src/environments/environment.ts`).

To point at a different backend URL (e.g. a deployed instance), edit
`src/environments/environment.ts` (dev) or `environment.prod.ts` (production build).

## 4. Building for production

```bash
npm run build
```

Output goes to `dist/weekly-report-frontend`. Serve it with any static file server, or
behind the same reverse proxy as the backend.

## 5. Logging in

- Use **Create account** on the login screen to self-register as a team member.
- Manager accounts are never created through self-registration (by design — see the
  backend's `AuthService`). Create the first manager either directly in the database, or
  by running the backend with `SEED_DEMO_DATA=true` for a ready-made demo manager
  account (see the backend README for credentials).

## 6. Project structure

```
src/app/
  core/
    models/        # TypeScript interfaces mirroring the backend DTOs exactly
    services/       # HttpClient wrappers, one per backend controller area
    guards/         # authGuard, guestGuard, managerGuard (route-level; the backend
                     # is the real authority — these just avoid a wasted round trip)
    interceptors/   # attaches the JWT bearer token; logs out on 401
  layout/shell/      # sidebar + content shell shown for every authenticated route
  shared/components/ # status badge, pagination, spinner, confirm dialog, chart wrapper
  features/
    auth/            # login, register
    reports/         # report-editor (create/edit), report-history, report-detail
    manager/         # team-dashboard, manager-reports, review, manager-report-detail,
                      # projects, users, member-profile
```

## 7. Pages implemented

| Page | Route | Notes |
|---|---|---|
| Login / Register | `/login`, `/register` | Public registration always creates a team-member account |
| Personal report (create/edit) | `/reports/new`, `/reports/:id/edit` | Fixed field order matching the spec; editable only while Draft or Needs Correction |
| Report history | `/reports` | Team member's own reports, paginated, by status |
| Report detail (read-only) | `/reports/:id` | Used once a report is Submitted/Approved |
| Team dashboard | `/manager/dashboard` | Summary metrics, status-by-member, 3 charts, activity feed |
| All reports (search/filter) | `/manager/reports` | Filter by member, project, status, week range |
| Manager review | `/manager/reports/:id/review` | Approve / request changes, with version history browsing |
| Manager report detail | `/manager/reports/:id` | Read-only view for non-submitted reports, with version history |
| Team member profile | `/manager/members/:id` | Full history + basic stats for one team member |
| Projects | `/manager/projects` | Full CRUD list page (not a modal) |
| Team members (admin) | `/manager/users` | Invite, assign roles, deactivate/reactivate |

## 8. AI chat assistant (optional)

When the backend has the AI assistant enabled (see the backend README), managers get:

- A floating **"Ask about the team"** chat widget (bottom-right, on every manager page) for
  conversational Q&A — e.g. *"What did the team work on last week?"*, *"What has Carol been
  working on?"*
- A **"Generate summary"** button on the team dashboard that produces an AI-written
  summary of completed work, recurring blockers, and workload balance for the selected week.

If the backend feature is off, both surfaces show a plain, honest "not configured" message
instead of a broken request — the rest of the app works identically either way.

## 8. Design notes

The visual identity ("ledger") treats the app as a status board, since the product
literally is one — status colors carry real meaning (left-border accents on cards,
not decoration), a serif display face for headings against a workhorse sans for data,
and a sidebar shell that separates "my work" from "team" navigation for managers.
