import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { CreateUserRequest, UpdateUserRequest, UserResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly base = `${environment.apiBaseUrl}/admin/users`;

  constructor(private readonly http: HttpClient) {}

  list(page = 0, size = 50): Observable<Page<UserResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'fullName,asc');
    return this.http.get<Page<UserResponse>>(this.base, { params });
  }

  getById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.base}/${id}`);
  }

  create(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.base, request);
  }

  update(id: number, request: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.base}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
