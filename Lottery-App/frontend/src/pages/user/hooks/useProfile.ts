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
}

export const useProfile = () => {
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<UserProfile>({
    userCode: localStorage.getItem('userCode') || 'N/A',
    email: localStorage.getItem('email') || 'N/A',
    fullName: localStorage.getItem('fullName') || 'N/A'
  });

  const [linkedAccounts, setLinkedAccounts] = useState<LinkedAccounts>({
    googleLinked: false,
    facebookLinked: false,
    phone: null
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
      return true;
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Update failed');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const updatePhone = async (phone: string) => {
    setLoading(true);
    try {
      await apiClient.put('/user/me', { phone });
      message.success('Phone number updated');
      fetchLinkedAccounts();
      return true;
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to update phone');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const unlinkPhone = async () => {
    setLoading(true);
    try {
      await apiClient.post('/user/unlink-phone');
      message.success('Phone number unlinked');
      fetchLinkedAccounts();
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to unlink phone');
    } finally {
      setLoading(false);
    }
  };

  const changePassword = async (values: any) => {
    setLoading(true);
    try {
      await apiClient.post('/user/change-password', {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword
      });
      message.success('Password changed successfully!');
      return true;
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to change password. Check your current password.');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const handleLinkSocial = async (provider: string, token: string) => {
    setLoading(true);
    try {
      await apiClient.post('/user/link-social', { provider, token });
      message.success(`${provider} account linked!`);
      fetchLinkedAccounts();
    } catch (e: any) {
      message.error(e.response?.data?.message || `Failed to link ${provider}`);
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    user,
    linkedAccounts,
    updateName,
    updatePhone,
    unlinkPhone,
    changePassword,
    handleLinkSocial
  };
};