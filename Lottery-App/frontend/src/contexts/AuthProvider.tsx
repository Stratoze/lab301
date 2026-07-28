import React, { useState, useEffect, useCallback } from 'react';
import { AuthContext } from './AuthContext';
import type { AuthUser } from './AuthContext';
import type { Role } from '../types/auth';
import apiClient from '../api/apiClient';

/**
 * AuthProvider stores auth state in memory and persists the token in localStorage.
 * On mount, it validates the stored token against /user/me to ensure the user
 * is still active and the role hasn't changed server-side.
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    // Hydrate from localStorage on initial render (fast path)
    const token = localStorage.getItem('token');
    if (!token) return null;
    return {
      token,
      userCode: localStorage.getItem('userCode') || '',
      fullName: localStorage.getItem('fullName') || '',
      email: localStorage.getItem('email') || '',
      role: (localStorage.getItem('role') as Role) || 'ROLE_USER',
    };
  });

  // On mount, validate token with server to catch stale/revoked tokens
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    apiClient
      .get('/user/me')
      .then((res) => {
        const data = res.data.data;
        const validated: AuthUser = {
          token,
          userCode: data.userCode,
          fullName: data.fullName,
          email: data.email,
          role: data.role,
        };
        setUser(validated);
        // Sync localStorage with server truth
        localStorage.setItem('role', data.role);
        localStorage.setItem('fullName', data.fullName);
        localStorage.setItem('userCode', data.userCode);
        localStorage.setItem('email', data.email);
      })
      .catch(() => {
        // Token invalid or user blocked → clear everything
        localStorage.clear();
        setUser(null);
      });
  }, []);

  const login = useCallback(
    (data: { token: string; userCode: string; fullName: string; role: Role }, email?: string) => {
      const authUser: AuthUser = {
        token: data.token,
        userCode: data.userCode,
        fullName: data.fullName,
        email: email || localStorage.getItem('email') || '',
        role: data.role,
      };
      setUser(authUser);
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userCode', data.userCode);
      localStorage.setItem('fullName', data.fullName);
      if (email) localStorage.setItem('email', email);
    },
    []
  );

  const logout = useCallback(() => {
    setUser(null);
    localStorage.clear();
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const res = await apiClient.get('/user/me');
      const data = res.data.data;
      const token = localStorage.getItem('token') || '';
      const updated: AuthUser = {
        token,
        userCode: data.userCode,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
      };
      setUser(updated);
      localStorage.setItem('role', data.role);
      localStorage.setItem('fullName', data.fullName);
    } catch {
      logout();
    }
  }, [logout]);

  const value = {
    user,
    isAuthenticated: user !== null,
    isAdmin: user?.role === 'ROLE_ADMIN',
    login,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};