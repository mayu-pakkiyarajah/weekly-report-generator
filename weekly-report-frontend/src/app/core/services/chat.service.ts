import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatRequest, ChatResponse, TeamSummaryResponse } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly base = `${environment.apiBaseUrl}/manager/chat`;

  constructor(private readonly http: HttpClient) {}

  ask(question: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.base}/ask`, { question } as ChatRequest);
  }

  getWeeklySummary(weekStart: string): Observable<TeamSummaryResponse> {
    return this.http.get<TeamSummaryResponse>(`${this.base}/summary`, { params: { weekStart } });
  }
}
