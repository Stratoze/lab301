import { useState, useEffect } from 'react';
import { message } from 'antd';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../../api/apiClient';
import { loginWithFacebookPopup } from '../../../utils/facebookOAuth';

interface UseAuthReturn {
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (values: any) => Promise<void>;
  socialLogin: (provider: string, token: string) => Promise<void>;
  facebookLogin: () => void;
  forgotPassword: (email: string) => Promise<void>;
}

const useAuth = (): UseAuthReturn => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      navigate('/lottery', { replace: true });
    }
  }, [navigate]);

  const saveAuth = (data: any) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);
    localStorage.setItem('userCode', data.userCode);
    localStorage.setItem('email', data.email || '');
    localStorage.setItem('fullName', data.fullName);
  };

  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/login', { email, password });
      const data = response.data.data;
      saveAuth({ ...data, email });
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const register = async (values: any) => {
    setLoading(true);
    try {
      await apiClient.post('/auth/register', values);
      message.success('Registration successful! Please login.');
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const socialLogin = async (provider: string, token: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/social', { provider, token });
      saveAuth(response.data.data);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Social login failed');
    } finally {
      setLoading(false);
    }
  };

  const facebookLogin = () => {
    loginWithFacebookPopup()
      .then((token: string) => socialLogin('FACEBOOK', token))
      .catch((err: any) => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  const forgotPassword = async (email: string) => {
    await apiClient.post('/password/forgot', { email });
    message.success('If the email is registered, a reset link has been sent.');
  };

  return { loading, login, register, socialLogin, facebookLogin, forgotPassword };
};

export default useAuth;