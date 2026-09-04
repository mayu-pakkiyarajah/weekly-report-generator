-- V1: Core schema for Weekly Report Generator & Team Dashboard
-- reports <-> report_versions has a circular reference (reports.current_version_id
-- points at the "active" version), so report_versions is created first without the
-- back-reference, and the FK from reports to report_versions is added afterwards.

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120)  NOT NULL,
    email           VARCHAR(180)  NOT NULL,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('TEAM_MEMBER', 'MANAGER'))
) ENGINE=InnoDB;

CREATE TABLE projects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120)  NOT NULL,
    description     VARCHAR(500),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uq_projects_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE project_assignments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    project_id      BIGINT        NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uq_project_assignment UNIQUE (user_id, project_id),
    CONSTRAINT fk_pa_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE reports (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    project_id          BIGINT        NOT NULL,
    week_start_date     DATE          NOT NULL,
    week_end_date       DATE          NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    current_version_id  BIGINT        NULL,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT uq_report_user_week UNIQUE (user_id, week_start_date),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_report_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT chk_report_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'NEEDS_CORRECTION', 'APPROVED'))
) ENGINE=InnoDB;

CREATE TABLE report_versions (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id                   BIGINT        NOT NULL,
    version_number              INT           NOT NULL,
    tasks_planned_next_week     TEXT,
    notes                       TEXT,
    links                       VARCHAR(1000),
    submitted_at                DATETIME(6)   NULL,
    created_at                  DATETIME(6)   NOT NULL,
    updated_at                  DATETIME(6)   NOT NULL,
    CONSTRAINT uq_report_version UNIQUE (report_id, version_number),
    CONSTRAINT fk_version_report FOREIGN KEY (report_id) REFERENCES reports (id) ON DELETE CASCADE
) ENGINE=InnoDB;

ALTER TABLE reports
    ADD CONSTRAINT fk_report_current_version
        FOREIGN KEY (current_version_id) REFERENCES report_versions (id);

CREATE TABLE task_entries (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_version_id   BIGINT        NOT NULL,
    task_name           VARCHAR(200)  NOT NULL,
    priority            VARCHAR(20)   NOT NULL,
    planned_percent     INT           NOT NULL DEFAULT 0,
    actual_percent      INT           NOT NULL DEFAULT 0,
    status              VARCHAR(20)   NOT NULL,
    time_planned_hours  DOUBLE        NOT NULL DEFAULT 0,
    time_spent_hours    DOUBLE        NOT NULL DEFAULT 0,
    output_deliverable  VARCHAR(500),
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT fk_task_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id) ON DELETE CASCADE,
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_task_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CARRIED_OVER'))
) ENGINE=InnoDB;

CREATE TABLE blockers (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_version_id   BIGINT        NOT NULL,
    description         VARCHAR(1000) NOT NULL,
    key_issue           BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT fk_blocker_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE achievements (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_version_id   BIGINT        NOT NULL,
    description         VARCHAR(1000) NOT NULL,
    key_achievement     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT fk_achievement_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hours_entries (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_version_id   BIGINT        NOT NULL,
    task_type           VARCHAR(20)   NOT NULL,
    hours               DOUBLE        NOT NULL DEFAULT 0,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT uq_hours_version_type UNIQUE (report_version_id, task_type),
    CONSTRAINT fk_hours_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id) ON DELETE CASCADE,
    CONSTRAINT chk_hours_task_type CHECK (task_type IN
        ('DEVELOPMENT', 'TESTING', 'MEETINGS', 'DOCUMENTATION', 'CODE_REVIEW', 'RESEARCH', 'OTHER'))
) ENGINE=InnoDB;

CREATE TABLE review_comments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id           BIGINT        NOT NULL,
    report_version_id   BIGINT        NOT NULL,
    reviewer_id         BIGINT        NOT NULL,
    action              VARCHAR(20)   NOT NULL,
    comment             VARCHAR(2000) NOT NULL,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT fk_review_report FOREIGN KEY (report_id) REFERENCES reports (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id),
    CONSTRAINT chk_review_action CHECK (action IN ('APPROVED', 'CHANGES_REQUESTED'))
) ENGINE=InnoDB;

CREATE INDEX idx_reports_week ON reports (week_start_date);
CREATE INDEX idx_reports_status ON reports (status);
CREATE INDEX idx_reports_user ON reports (user_id);
CREATE INDEX idx_review_comments_report ON review_comments (report_id);
