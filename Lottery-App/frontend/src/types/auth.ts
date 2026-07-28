export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export interface LoginResponse {
  token: string;
  userCode: string;
  fullName: string;
  role: Role;
}