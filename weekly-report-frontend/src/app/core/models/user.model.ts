import { Role } from './auth.model';

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  fullName: string;
  email: string;
  temporaryPassword: string;
  role: Role;
}

export interface UpdateUserRequest {
  role: Role;
  active: boolean;
}
