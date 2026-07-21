import React from 'react';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Profile from '../Profile';

// Mock the hook
vi.mock('../hooks/useProfile', () => ({
  default: vi.fn(),
}));

import useProfile from '../hooks/useProfile';

const mockUseProfile = vi.mocked(useProfile);

// Mock antd message
vi.mock('antd', async () => {
  const actual = await vi.importActual('antd');
  return {
    ...(actual as any),
    message: { error: vi.fn(), warning: vi.fn(), success: vi.fn(), info: vi.fn() },
  };
});

import { message } from 'antd';

// Mock LinkedAccountsSection to avoid GoogleOAuthProvider requirement
vi.mock('../components/LinkedAccountsSection', () => ({
  default: ({ accounts }: any) => (
    <div data-testid="linked-accounts">
      <span>Google: {accounts.googleLinked ? 'Linked' : 'Not Linked'}</span>
    </div>
  ),
}));

// Mock DashboardCard to just render children
vi.mock('../../../components/DashboardCard', () => ({
  default: ({ title, children }: any) => <div><h2>{title}</h2>{children}</div>,
}));

const baseMockReturn = {
  user: {
    userCode: 'USR-10-2023-00000003',
    email: 'khach1@gmail.com',
    fullName: 'Le V?n Tam',
  },
  linkedAccounts: {
    googleLinked: false,
    facebookLinked: false,
    phone: null,
    hasPassword: true,
  },
  loading: false,
  isPassModalOpen: false,
  isEditNameOpen: false,
  isPhoneModalOpen: false,
  setIsPassModalOpen: vi.fn(),
  setIsEditNameOpen: vi.fn(),
  setIsPhoneModalOpen: vi.fn(),
  fetchLinkedAccounts: vi.fn(),
  updateName: vi.fn(),
  changePassword: vi.fn(),
  linkSocial: vi.fn(),
  updatePhone: vi.fn(),
  unlinkPhone: vi.fn(),
};

describe('Profile', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseProfile.mockReturnValue({ ...baseMockReturn });
  });

  // 5.10 User info displayed correctly
  it('displays user info: userCode, email, fullName', () => {
    act(() => {
      render(<Profile />);
    });

    expect(screen.getByText('USR-10-2023-00000003')).toBeInTheDocument();
    expect(screen.getByText('khach1@gmail.com')).toBeInTheDocument();
    expect(screen.getByText('Le V?n Tam')).toBeInTheDocument();
  });

  // 5.11 Password modal shows old password field when hasPassword is true
  it('shows current password field when user has a password', () => {
    mockUseProfile.mockReturnValue({
      ...baseMockReturn,
      isPassModalOpen: true,
      linkedAccounts: { ...baseMockReturn.linkedAccounts, hasPassword: true },
    });

    act(() => {
      render(<Profile />);
    });

    expect(screen.getByText('Enter Your Current Password')).toBeInTheDocument();
  });

  // 5.12 Password modal hides old password field when no password (social user)
  it('hides current password field when user has no password set', () => {
    mockUseProfile.mockReturnValue({
      ...baseMockReturn,
      isPassModalOpen: true,
      linkedAccounts: { ...baseMockReturn.linkedAccounts, hasPassword: false },
    });

    act(() => {
      render(<Profile />);
    });

    expect(screen.queryByText('Enter Your Current Password')).not.toBeInTheDocument();
    // Title changes to "Set a Password"
    expect(screen.getByText('Set a Password')).toBeInTheDocument();
  });

  // 5.13 Copy to clipboard
  it('copies text to clipboard when copy button clicked', async () => {
    const user = userEvent.setup();
    const clipboardSpy = vi.spyOn(navigator.clipboard, 'writeText');

    act(() => {
      render(<Profile />);
    });

    // Click the first copy button (next to user code) - antd CopyOutlined icon has aria-label="copy"
    const copyButtons = screen.getAllByLabelText('copy');
    await user.click(copyButtons[0]);

    expect(clipboardSpy).toHaveBeenCalledWith('USR-10-2023-00000003');
    expect(message.success).toHaveBeenCalledWith('Copied to clipboard');
  });

  // 5.14 Change password - success flow
  it('shows success message on successful password change', async () => {
    const user = userEvent.setup();
    const changePassword = vi.fn().mockResolvedValue(undefined);
    mockUseProfile.mockReturnValue({
      ...baseMockReturn,
      isPassModalOpen: true,
      changePassword,
    });

    act(() => {
      render(<Profile />);
    });

    // Fill old password, new password, confirm
    const oldPw = screen.getByLabelText(/Enter Your Current Password/i);
    const newPwFields = screen.getAllByLabelText(/New Password/i);
    const confirmPw = screen.getByLabelText(/Confirm your new password/i);

    await user.type(oldPw, 'oldPassword123');
    await user.type(newPwFields[0], 'NewStrongPass1!');
    await user.type(confirmPw, 'NewStrongPass1!');

    // Click OK button on modal
    const okButton = screen.getByRole('button', { name: /OK/i });
    await user.click(okButton);

    await vi.waitFor(() => {
      expect(changePassword).toHaveBeenCalledWith('oldPassword123', 'NewStrongPass1!');
    });
  });

  // 5.15 Change password - mismatch error
  it('shows validation error when passwords do not match', async () => {
    const user = userEvent.setup();
    mockUseProfile.mockReturnValue({
      ...baseMockReturn,
      isPassModalOpen: true,
    });

    act(() => {
      render(<Profile />);
    });

    const oldPw = screen.getByLabelText(/Enter Your Current Password/i);
    const newPwFields = screen.getAllByLabelText(/New Password/i);
    const confirmPw = screen.getByLabelText(/Confirm your new password/i);

    await user.type(oldPw, 'oldPassword123');
    await user.type(newPwFields[0], 'NewStrongPass1!');
    await user.type(confirmPw, 'DifferentPass1!');

    const okButton = screen.getByRole('button', { name: /OK/i });
    await user.click(okButton);

    // Ant Design shows inline validation error
    await vi.waitFor(() => {
      expect(screen.getByText(/Passwords do not match!/i)).toBeInTheDocument();
    });
  });

  // 5.16 Change password - API error is displayed
  it('displays error message when change password API fails', async () => {
    const user = userEvent.setup();
    const changePassword = vi.fn().mockRejectedValue(new Error('Current password is incorrect'));
    mockUseProfile.mockReturnValue({
      ...baseMockReturn,
      isPassModalOpen: true,
      changePassword,
    });

    act(() => {
      render(<Profile />);
    });

    const oldPw = screen.getByLabelText(/Enter Your Current Password/i);
    const newPwFields = screen.getAllByLabelText(/New Password/i);
    const confirmPw = screen.getByLabelText(/Confirm your new password/i);

    await user.type(oldPw, 'wrongOldPassword');
    await user.type(newPwFields[0], 'NewStrongPass1!');
    await user.type(confirmPw, 'NewStrongPass1!');

    const okButton = screen.getByRole('button', { name: /OK/i });
    await user.click(okButton);

    await vi.waitFor(() => {
      expect(message.error).toHaveBeenCalledWith('Current password is incorrect');
    });
  });
});