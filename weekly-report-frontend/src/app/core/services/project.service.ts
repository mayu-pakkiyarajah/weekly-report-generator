import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { ProjectRequest, ProjectResponse } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly base = `${environment.apiBaseUrl}/projects`;

  constructor(private readonly http: HttpClient) {}

  listActive(page = 0, size = 100): Observable<Page<ProjectResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'name,asc');
    return this.http.get<Page<ProjectResponse>>(this.base, { params });
  }

  listAll(page = 0, size = 100): Observable<Page<ProjectResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'name,asc');
    return this.http.get<Page<ProjectResponse>>(`${this.base}/all`, { params });
  }

  create(request: ProjectRequest): Observable<ProjectResponse> {
    return this.http.post<ProjectResponse>(this.base, request);
  }

  update(id: number, request: ProjectRequest): Observable<ProjectResponse> {
    return this.http.put<ProjectResponse>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  assignUser(projectId: number, userId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${projectId}/assignments`, { userId });
  }
}
