import { ReportStatus, TaskType } from './report.model';

export interface DashboardSummaryResponse {
  totalReportsSubmitted: number;
  expectedReports: number;
  pendingCount: number;
  lateCount: number;
  complianceRatePercent: number;
  needsCorrectionCount: number;
  openBlockersCount: number;
}

export interface StatusByMemberResponse {
  userId: number;
  userFullName: string;
  status: ReportStatus | null;
}

export interface WorkloadByProjectResponse {
  projectId: number;
  projectName: string;
  taskCount: number;
  totalHours: number;
}

export interface TimeByTaskTypeResponse {
  taskType: TaskType;
  totalHours: number;
}

export interface TasksTrendPointResponse {
  weekStartDate: string;
  tasksCompletedCount: number;
}

export interface ActivityFeedItemResponse {
  type: string;
  reportId: number;
  userFullName: string;
  description: string;
  timestamp: string;
}

export interface SectionAcrossTeamResponse {
  userId: number;
  userFullName: string;
  entries: string[];
}
