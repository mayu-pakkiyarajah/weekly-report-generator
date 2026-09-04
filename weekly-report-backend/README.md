# Weekly Report Generator & Team Dashboard — Backend

Spring Boot 3 / Java 21 REST API backing the Weekly Report Generator & Team Dashboard
assignment. MySQL for storage, Flyway for schema migrations, JWT for stateless auth,
role-based access control via Spring Security.

## 1. Prerequisites

- Java 21 (JDK)
- Maven 3.9+ (or use the included `mvnw` wrapper if you add one)
- MySQL 8.x running locally or reachable over the network
- (Optional) Postman/Insomnia or the built-in Swagger UI for exploring the API

## 2. Installing dependencies

```bash
cd weekly-report-backend
mvn dependency:go-offline
```

This downloads all dependencies declared in `pom.xml` (Spring Boot, Spring Security,
Spring Data JPA, MySQL driver, Flyway, JJWT, MapStruct, Lombok, springdoc-openapi).

## 3. Running the database

Create the database and an application user (adjust credentials as you like):

```sql
CREATE DATABASE weekly_reports CHARACTER SET utf8mb4;
CREATE USER 'report_app'@'%' IDENTIFIED BY 'report_app_password';
GRANT ALL PRIVILEGES ON weekly_reports.* TO 'report_app'@'%';
FLUSH PRIVILEGES;
```

You do **not** need to create any tables by hand — Flyway automatically applies
`src/main/resources/db/migration/V1__init_schema.sql` the first time the application starts.

Environment variables (all optional, sensible defaults shown):

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `weekly_reports` | Schema name |
| `DB_USERNAME` | `report_app` | DB user |
| `DB_PASSWORD` | `report_app_password` | DB password |
| `JWT_SECRET` | (dev default, **override in real deployments**) | HMAC signing key for JWTs |
| `JWT_ACCESS_TTL_MIN` | `60` | Access token lifetime in minutes |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Comma-separated list of allowed frontend origins |
| `SEED_DEMO_DATA` | `false` | Set to `true` to auto-populate demo users/projects/reports on first startup |

## 4. Running the backend

```bash
# from weekly-report-backend/
export SEED_DEMO_DATA=true   # optional, only needed the first time you want demo data
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. Swagger UI is available at
`http://localhost:8080/swagger-ui.html` for browsing/trying every endpoint.

### Demo accounts (only created when `SEED_DEMO_DATA=true` and the DB is empty)

| Role | Email | Password |
|---|---|---|
| Manager | `manager@demo.local` | `Password123!` |
| Team member | `kavindu@demo.local` | `Password123!` |
| Team member | `nadeesha@demo.local` | `Password123!` |
| Team member | `ruwan@demo.local` | `Password123!` |
| Team member | `ishara@demo.local` | `Password123!` |
| Team member | `tharindu@demo.local` | `Password123!` |

The seeder creates 3 projects and 5 weeks of reports spread across every status
(Draft, Submitted, Needs Correction, Approved — including at least one report that
went through a full correction cycle, so version history is populated).

## 5. Running the tests

```bash
mvn test
```

`RoleBasedAccessControlTest` runs against an in-memory H2 database (MySQL compatibility
mode) and verifies:
- a team member cannot reach manager-only endpoints (403)
- a manager can reach manager-only endpoints (200)
- a team member cannot read another team member's report, even by guessing its id (403)
- unauthenticated requests are rejected

## 6. API overview

All endpoints are under `/api`. Auth uses `Authorization: Bearer <token>`.

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | `register` (always creates a TEAM_MEMBER), `login` |
| User management | `/api/admin/users` | Manager-only: invite users, assign roles, deactivate |
| Projects | `/api/projects` | List active projects (any role); create/edit/delete/assign (manager only) |
| Reports (own) | `/api/reports` | Team member: create draft, edit, submit, view own history/detail |
| Reports (team) | `/api/manager/reports` | Manager-only: search/filter/paginate, view any report, view version history, review (approve / request changes) |
| Dashboard | `/api/manager/dashboard` | Manager-only: summary metrics, status-by-member, workload-by-project, time-by-task-type, tasks-completed trend, activity feed, blockers/achievements across the team |

### Report lifecycle

```
DRAFT --submit--> SUBMITTED --manager approves--> APPROVED
                        |
                        `--manager requests changes--> NEEDS_CORRECTION
                                                              |
                                              team member edits (new version created)
                                                              |
                                                          --submit--> SUBMITTED (loop)
```

Every time content is edited after a `NEEDS_CORRECTION`, a new `ReportVersion` row is
created instead of overwriting the previous one — `GET /api/manager/reports/{id}/versions`
returns the full list so a manager can see exactly what changed between review cycles.

## 7. Project structure

```
src/main/java/com/teamreports/weeklyreport/
  config/       # Spring configuration (security, CORS, JWT props, JPA auditing, seed toggle)
  controller/   # REST controllers
  dto/          # Request/response records, grouped by feature
  entity/       # JPA entities + enums
  exception/    # Custom exceptions + global @RestControllerAdvice
  mapper/       # Entity <-> DTO mapping
  repository/   # Spring Data JPA repositories
  security/     # JWT issuing/parsing, filter, UserDetails adapter
  seed/         # Optional demo data seeder
  service/      # Business logic
```
