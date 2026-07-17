import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { message } from 'antd';
import apiClient from '../../../api/apiClient';

interface ValidationRules {
  minLength: number;
  maxLength: number;
  blocklist: string[];
}

export const useAuth = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [rules, setRules] = useState<ValidationRules | null>(null);

  useEffect(() => {
    const fetchRules = async () => {
      try {
        const response = await apiClient.get('/auth/password-rules');
        if (response.data?.data) {
          setRules(response.data.data);
        }
      } catch (error) {
        console.error('Failed to load password validation rules', error);
      }
    };
    fetchRules();
  }, []);

  const handleSocialLogin = async (provider: string, token: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/social', { provider, token });
      const { token: jwtToken, role, userCode, fullName, email } = response.data.data;
      localStorage.setItem('token', jwtToken);
      localStorage.setItem('role', role);
      localStorage.setItem('userCode', userCode);
      localStorage.setItem('email', email || '');
      localStorage.setItem('fullName', fullName);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Social login failed');
    } finally {
      setLoading(false);
    }
  };

  const login = async (values: any) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/login', values);
      const { token, role, userCode, fullName } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);
      localStorage.setItem('userCode', userCode);
      localStorage.setItem('email', values.email);
      localStorage.setItem('fullName', fullName);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const register = async (values: any) => {
    setLoading(true);
    try {
      await apiClient.post('/auth/register', values);
      message.success('Registration successful! Please login.');
      return true;
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Registration failed.');
      return false;
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    rules,
    login,
    register,
    handleSocialLogin,
  };
};