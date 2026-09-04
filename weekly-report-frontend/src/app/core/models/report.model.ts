export type ReportStatus = 'DRAFT' | 'SUBMITTED' | 'NEEDS_CORRECTION' | 'APPROVED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type TaskStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED' | 'CARRIED_OVER';
export type TaskType = 'DEVELOPMENT' | 'TESTING' | 'MEETINGS' | 'DOCUMENTATION' | 'CODE_REVIEW' | 'RESEARCH' | 'OTHER';
export type ReviewAction = 'APPROVED' | 'CHANGES_REQUESTED';

export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
export const TASK_STATUSES: TaskStatus[] = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CARRIED_OVER'];
export const TASK_TYPES: TaskType[] = ['DEVELOPMENT', 'TESTING', 'MEETINGS', 'DOCUMENTATION', 'CODE_REVIEW', 'RESEARCH', 'OTHER'];

export interface TaskEntryDto {
  id?: number;
  taskName: string;
  priority: Priority;
  plannedPercent: number;
  actualPercent: number;
  status: TaskStatus;
  timePlannedHours: number;
  timeSpentHours: number;
  outputDeliverable?: string;
}

export interface BlockerDto {
  id?: number;
  description: string;
  keyIssue: boolean;
}

export interface AchievementDto {
  id?: number;
  description: string;
  keyAchievement: boolean;
}

export interface HoursEntryDto {
  taskType: TaskType;
  hours: number;
}

export interface ReportCreateRequest {
  weekStartDate: string; // ISO date (yyyy-MM-dd)
  weekEndDate: string;
  projectId: number;
}

export interface ReportContentRequest {
  tasksCompleted: TaskEntryDto[];
  tasksPlannedNextWeek: string;
  blockers: BlockerDto[];
  achievements: AchievementDto[];
  hoursByTaskType: HoursEntryDto[];
  notes: string;
  links: string;
}

export interface ReportVersionResponse {
  id: number;
  versionNumber: number;
  tasksCompleted: TaskEntryDto[];
  tasksPlannedNextWeek: string | null;
  blockers: BlockerDto[];
  achievements: AchievementDto[];
  hoursByTaskType: HoursEntryDto[];
  notes: string | null;
  links: string | null;
  submittedAt: string | null;
}

export interface ReviewCommentResponse {
  id: number;
  reviewedVersionNumber: number;
  action: ReviewAction;
  comment: string;
  reviewerName: string;
  createdAt: string;
}

export interface ReportResponse {
  id: number;
  userId: number;
  userFullName: string;
  projectId: number;
  projectName: string;
  weekStartDate: string;
  weekEndDate: string;
  status: ReportStatus;
  currentVersion: ReportVersionResponse;
  reviewHistory: ReviewCommentResponse[];
}

export interface ReportSummaryResponse {
  id: number;
  userId: number;
  userFullName: string;
  projectId: number;
  projectName: string;
  weekStartDate: string;
  weekEndDate: string;
  status: ReportStatus;
  versionCount: number;
  hasOpenBlockers: boolean;
}

export interface ReviewActionRequest {
  action: ReviewAction;
  comment: string;
}
