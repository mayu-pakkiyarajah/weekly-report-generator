import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActivityFeedItemResponse, DashboardSummaryResponse, SectionAcrossTeamResponse,
  StatusByMemberResponse, TasksTrendPointResponse, TimeByTaskTypeResponse, WorkloadByProjectResponse
} from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly base = `${environment.apiBaseUrl}/manager/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getSummary(weekStart: string): Observable<DashboardSummaryResponse> {
    return this.http.get<DashboardSummaryResponse>(`${this.base}/summary`, { params: { weekStart } });
  }

  getStatusByMember(weekStart: string): Observable<StatusByMemberResponse[]> {
    return this.http.get<StatusByMemberResponse[]>(`${this.base}/status-by-member`, { params: { weekStart } });
  }

  getWorkloadByProject(weekStart?: string): Observable<WorkloadByProjectResponse[]> {
    let params = new HttpParams();
    if (weekStart) params = params.set('weekStart', weekStart);
    return this.http.get<WorkloadByProjectResponse[]>(`${this.base}/workload-by-project`, { params });
  }

  getTimeByTaskType(weekStart?: string): Observable<TimeByTaskTypeResponse[]> {
    let params = new HttpParams();
    if (weekStart) params = params.set('weekStart', weekStart);
    return this.http.get<TimeByTaskTypeResponse[]>(`${this.base}/time-by-task-type`, { params });
  }

  getTasksCompletedTrend(weeksBack = 8): Observable<TasksTrendPointResponse[]> {
    return this.http.get<TasksTrendPointResponse[]>(`${this.base}/tasks-completed-trend`, { params: { weeksBack } });
  }

  getActivityFeed(limit = 20): Observable<ActivityFeedItemResponse[]> {
    return this.http.get<ActivityFeedItemResponse[]>(`${this.base}/activity-feed`, { params: { limit } });
  }

  getBlockersAcrossTeam(weekStart: string): Observable<SectionAcrossTeamResponse[]> {
    return this.http.get<SectionAcrossTeamResponse[]>(`${this.base}/sections/blockers`, { params: { weekStart } });
  }

  getAchievementsAcrossTeam(weekStart: string): Observable<SectionAcrossTeamResponse[]> {
    return this.http.get<SectionAcrossTeamResponse[]>(`${this.base}/sections/achievements`, { params: { weekStart } });
  }
}
