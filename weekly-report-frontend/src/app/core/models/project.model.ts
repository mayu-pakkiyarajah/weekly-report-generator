export interface ProjectResponse {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
}

export interface ProjectRequest {
  name: string;
  description?: string;
}
