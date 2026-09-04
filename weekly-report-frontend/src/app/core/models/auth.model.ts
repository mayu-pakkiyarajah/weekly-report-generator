export type Role = 'TEAM_MEMBER' | 'MANAGER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}
