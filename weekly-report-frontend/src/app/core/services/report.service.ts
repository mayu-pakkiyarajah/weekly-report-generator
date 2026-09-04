import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import {
  ReportContentRequest, ReportCreateRequest, ReportResponse, ReportSummaryResponse,
  ReportStatus, ReviewActionRequest, ReportVersionResponse
} from '../models/report.model';

/** Team-member-facing report endpoints - always scoped to the authenticated user server-side. */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly base = `${environment.apiBaseUrl}/reports`;

  constructor(private readonly http: HttpClient) {}

  createDraft(request: ReportCreateRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(this.base, request);
  }

  listOwn(page = 0, size = 20): Observable<Page<ReportSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'weekStartDate,desc');
    return this.http.get<Page<ReportSummaryResponse>>(this.base, { params });
  }

  getOwn(id: number): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/${id}`);
  }

  updateContent(id: number, content: ReportContentRequest): Observable<ReportResponse> {
    return this.http.put<ReportResponse>(`${this.base}/${id}`, content);
  }

  submit(id: number): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.base}/${id}/submit`, {});
  }
}

/** Manager-facing report endpoints - can reach any team member's reports. */
@Injectable({ providedIn: 'root' })
export class ManagerReportService {
  private readonly base = `${environment.apiBaseUrl}/manager/reports`;

  constructor(private readonly http: HttpClient) {}

  search(filters: {
    memberId?: number | null; projectId?: number | null; status?: ReportStatus | null;
    weekStart?: string | null; weekEnd?: string | null;
  }, page = 0, size = 20): Observable<Page<ReportSummaryResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'weekStartDate,desc');
    if (filters.memberId) params = params.set('memberId', filters.memberId);
    if (filters.projectId) params = params.set('projectId', filters.projectId);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.weekStart) params = params.set('weekStart', filters.weekStart);
    if (filters.weekEnd) params = params.set('weekEnd', filters.weekEnd);
    return this.http.get<Page<ReportSummaryResponse>>(this.base, { params });
  }

  get(id: number): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.base}/${id}`);
  }

  getVersionHistory(id: number): Observable<ReportVersionResponse[]> {
    return this.http.get<ReportVersionResponse[]>(`${this.base}/${id}/versions`);
  }

  review(id: number, request: ReviewActionRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.base}/${id}/review`, request);
  }
}
