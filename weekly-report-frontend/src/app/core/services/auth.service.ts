import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, Role } from '../models/auth.model';

const STORAGE_KEY = 'wr_auth';

interface StoredSession {
  accessToken: string;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}

/**
 * Holds the current session in memory (signals) and persists it to localStorage
 * only so a page refresh doesn't force a re-login - the token itself is still
 * validated/expired server-side on every request.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sessionSignal = signal<StoredSession | null>(this.readStoredSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.sessionSignal());
  readonly isManager = computed(() => this.sessionSignal()?.role === 'MANAGER');
  readonly currentUserId = computed(() => this.sessionSignal()?.userId ?? null);
  readonly currentUserName = computed(() => this.sessionSignal()?.fullName ?? '');

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, request)
      .pipe(tap((res) => this.storeSession(res)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, request)
      .pipe(tap((res) => this.storeSession(res)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sessionSignal.set(null);
  }

  getToken(): string | null {
    return this.sessionSignal()?.accessToken ?? null;
  }

  private storeSession(res: AuthResponse): void {
    const session: StoredSession = {
      accessToken: res.accessToken,
      userId: res.userId,
      fullName: res.fullName,
      email: res.email,
      role: res.role
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.sessionSignal.set(session);
  }

  private readStoredSession(): StoredSession | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredSession;
    } catch {
      return null;
    }
  }
}
