import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider } from '../AuthProvider';
import { useAuthContext } from '../useAuthContext';

// Mock the API layer — AuthProvider calls GET /user/me on mount
vi.mock('../../api/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import apiClient from '../../api/apiClient';

/**
 * Test consumer that surfaces the auth context values
 * so we can assert on them from the DOM.
 */
const AuthConsumer = () => {
  const { user, isAuthenticated, isAdmin, login, logout } = useAuthContext();
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'authenticated' : 'anonymous'}</span>
      <span data-testid="admin-status">{isAdmin ? 'admin' : 'not-admin'}</span>
      <span data-testid="user-name">{user?.fullName ?? 'none'}</span>
      <span data-testid="user-role">{user?.role ?? 'none'}</span>
      <button
        data-testid="login-btn"
        onClick={() =>
          login(
            {
              token: 'new.jwt.token',
              userCode: 'USR-01-2026-00000001',
              fullName: 'New User',
              role: 'ROLE_ADMIN',
            },
            'newuser@gmail.com'
          )
        }
      >
        login
      </button>
      <button data-testid="logout-btn" onClick={logout}>
        logout
      </button>
    </div>
  );
};

const renderWithProvider = () =>
  render(
    <AuthProvider>
      <AuthConsumer />
    </AuthProvider>
  );

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  // No token → stays anonymous, and never bothers the server
  it('starts unauthenticated when no token in localStorage', () => {
    renderWithProvider();
    expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');
    expect(apiClient.get).not.toHaveBeenCalled();
  });

  // Fast path: user is hydrated from localStorage on first paint,
  // before the server validation round-trip completes
  it('hydrates user from localStorage before server validation', () => {
    localStorage.setItem('token', 'stored.jwt.token');
    localStorage.setItem('userCode', 'USR-10-2023-00000003');
    localStorage.setItem('fullName', 'Le Van Tam');
    localStorage.setItem('email', 'khach1@gmail.com');
    localStorage.setItem('role', 'ROLE_USER');

    // Hang the request so we can observe the hydrated (pre-validation) state
    vi.mocked(apiClient.get).mockReturnValue(new Promise(() => {}));

    renderWithProvider();

    expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    expect(screen.getByTestId('user-name')).toHaveTextContent('Le Van Tam');
    expect(screen.getByTestId('user-role')).toHaveTextContent('ROLE_USER');
  });

  // SRS: "validates token on mount" — server truth overrides stale localStorage
  it('validates token on mount and updates user from server response', async () => {
    localStorage.setItem('token', 'stored.jwt.token');
    localStorage.setItem('fullName', 'Stale Name');
    localStorage.setItem('role', 'ROLE_USER');

    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: {
        data: {
          userCode: 'USR-10-2023-00000003',
          fullName: 'Server Name',
          email: 'khach1@gmail.com',
          role: 'ROLE_ADMIN',
        },
      },
    });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('Server Name');
    });

    // Server role wins over the stale localStorage value
    expect(screen.getByTestId('user-role')).toHaveTextContent('ROLE_ADMIN');
    expect(screen.getByTestId('admin-status')).toHaveTextContent('admin');
    // localStorage is synced back to server truth
    expect(localStorage.getItem('role')).toBe('ROLE_ADMIN');
    expect(localStorage.getItem('fullName')).toBe('Server Name');
  });

  // SRS: "clears on 401" — a revoked/blocked token wipes everything
  it('clears user and localStorage when token validation fails with 401', async () => {
    localStorage.setItem('token', 'revoked.jwt.token');
    localStorage.setItem('fullName', 'Blocked User');
    localStorage.setItem('role', 'ROLE_USER');

    vi.mocked(apiClient.get).mockRejectedValueOnce({
      response: { status: 401, data: { message: 'Authentication required' } },
    });

    renderWithProvider();

    // Initially hydrated from localStorage...
    expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');

    // ...then the failed validation logs the user out
    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');
    });
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('fullName')).toBeNull();
  });

  // SRS: "provides role from server" — isAdmin tracks the server-issued role
  it('provides isAdmin=true when server role is ROLE_ADMIN', async () => {
    localStorage.setItem('token', 'admin.jwt.token');
    localStorage.setItem('role', 'ROLE_USER'); // stale on purpose

    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: {
        data: {
          userCode: 'USR-10-2023-00000001',
          fullName: 'Admin User',
          email: 'admin@veso.vn',
          role: 'ROLE_ADMIN',
        },
      },
    });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('admin-status')).toHaveTextContent('admin');
    });
  });

  // SRS: "login/logout updates state" — login sets state + persists token
  it('login() sets user state and persists to localStorage', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');

    await user.click(screen.getByTestId('login-btn'));

    expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    expect(screen.getByTestId('user-name')).toHaveTextContent('New User');
    expect(screen.getByTestId('admin-status')).toHaveTextContent('admin');
    expect(localStorage.getItem('token')).toBe('new.jwt.token');
    expect(localStorage.getItem('email')).toBe('newuser@gmail.com');
  });

  // SRS: "login/logout updates state" — logout clears state + storage
  it('logout() clears user state and localStorage', async () => {
    const user = userEvent.setup();
    localStorage.setItem('token', 'some.jwt.token');
    localStorage.setItem('fullName', 'Le Van Tam');
    localStorage.setItem('role', 'ROLE_USER');

    vi.mocked(apiClient.get).mockResolvedValueOnce({
      data: {
        data: {
          userCode: 'USR-10-2023-00000003',
          fullName: 'Le Van Tam',
          email: 'khach1@gmail.com',
          role: 'ROLE_USER',
        },
      },
    });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    });

    await user.click(screen.getByTestId('logout-btn'));

    expect(screen.getByTestId('auth-status')).toHaveTextContent('anonymous');
    expect(localStorage.getItem('token')).toBeNull();
  });
});