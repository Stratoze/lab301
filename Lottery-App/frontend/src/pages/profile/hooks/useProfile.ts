import { useState, useEffect, useCallback } from 'react';
import { message } from 'antd';
import apiClient from '../../../api/apiClient';

export interface UserProfile {
  userCode: string;
  email: string;
  fullName: string;
}

export interface LinkedAccounts {
  googleLinked: boolean;
  facebookLinked: boolean;
  phone: string | null;
  hasPassword: boolean;
}

interface UseProfileReturn {
  user: UserProfile;
  linkedAccounts: LinkedAccounts;
  loading: boolean;
  isPassModalOpen: boolean;
  isEditNameOpen: boolean;
  isPhoneModalOpen: boolean;
  setIsPassModalOpen: (v: boolean) => void;
  setIsEditNameOpen: (v: boolean) => void;
  setIsPhoneModalOpen: (v: boolean) => void;
  fetchLinkedAccounts: () => Promise<void>;
  updateName: (fullName: string) => Promise<void>;
  changePassword: (oldPassword: string, newPassword: string) => Promise<void>;
  linkSocial: (provider: string, token: string) => Promise<void>;
  updatePhone: (phone: string) => Promise<void>;
  unlinkPhone: () => Promise<void>;
}

const useProfile = (): UseProfileReturn => {
  const [loading, setLoading] = useState(false);
  const [isPassModalOpen, setIsPassModalOpen] = useState(false);
  const [isEditNameOpen, setIsEditNameOpen] = useState(false);
  const [isPhoneModalOpen, setIsPhoneModalOpen] = useState(false);

  const [user, setUser] = useState<UserProfile>({
    userCode: localStorage.getItem('userCode') || 'N/A',
    email: localStorage.getItem('email') || 'N/A',
    fullName: localStorage.getItem('fullName') || 'N/A',
  });

  const [linkedAccounts, setLinkedAccounts] = useState<LinkedAccounts>({
    googleLinked: false,
    facebookLinked: false,
    phone: null,
    hasPassword: true,
  });

  const fetchLinkedAccounts = useCallback(async () => {
    try {
      const res = await apiClient.get('/user/linked-accounts');
      setLinkedAccounts(res.data.data);
    } catch {
      // Silently fail; user can still see profile
    }
  }, []);

  useEffect(() => {
    fetchLinkedAccounts();
  }, [fetchLinkedAccounts]);

  const updateName = async (fullName: string) => {
    setLoading(true);
    try {
      const res = await apiClient.put('/user/me', { fullName });
      const newName = res.data.data.fullName;
      localStorage.setItem('fullName', newName);
      setUser(prev => ({ ...prev, fullName: newName }));
      message.success('Profile updated successfully');
    } catch (e: any) {
      throw new Error(e.response?.data?.message || 'Update failed');
    } finally {
      setLoading(false);
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    setLoading(true);
    try {
      await apiClient.post('/user/change-password', {
        oldPassword: linkedAccounts.hasPassword ? oldPassword : '',
        newPassword,
      });
      message.success('Password changed successfully!');
      await fetchLinkedAccounts();
    } catch (e: any) {
      throw new Error(e.response?.data?.message || 'Failed to change password');
    } finally {
      setLoading(false);
    }
  };

  const linkSocial = async (provider: string, token: string) => {
    setLoading(true);
    try {
      await apiClient.post('/user/link-social', { provider, token });
      message.success(`${provider} account linked!`);
      await fetchLinkedAccounts();
    } catch (e: any) {
      throw new Error(e.response?.data?.message || `Failed to link ${provider}`);
    } finally {
      setLoading(false);
    }
  };

  const updatePhone = async (phone: string) => {
    setLoading(true);
    try {
      await apiClient.put('/user/me', { phone });
      message.success('Phone number updated');
      await fetchLinkedAccounts();
    } catch (e: any) {
      throw new Error(e.response?.data?.message || 'Failed to update phone');
    } finally {
      setLoading(false);
    }
  };

  const unlinkPhone = async () => {
    setLoading(true);
    try {
      await apiClient.post('/user/unlink-phone');
      message.success('Phone number unlinked');
      await fetchLinkedAccounts();
    } catch (e: any) {
      throw new Error(e.response?.data?.message || 'Failed to unlink phone');
    } finally {
      setLoading(false);
    }
  };

  return {
    user,
    linkedAccounts,
    loading,
    isPassModalOpen,
    isEditNameOpen,
    isPhoneModalOpen,
    setIsPassModalOpen,
    setIsEditNameOpen,
    setIsPhoneModalOpen,
    fetchLinkedAccounts,
    updateName,
    changePassword,
    linkSocial,
    updatePhone,
    unlinkPhone,
  };
};

export default useProfile;