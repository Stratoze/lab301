import { createContext } from 'react';
import type { Role } from '../types/auth';

export interface AuthUser {
  token: string;
  userCode: string;
  fullName: string;
  email: string;
  role: Role;
}

export interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (data: { token: string; userCode: string; fullName: string; role: Role }, email?: string) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue>({
  user: null,
  isAuthenticated: false,
  isAdmin: false,
  login: () => {},
  logout: () => {},
  refreshUser: async () => {},
});