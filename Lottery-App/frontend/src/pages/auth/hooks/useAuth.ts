import { useState, useEffect } from 'react';
import { message } from 'antd';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../../api/apiClient';
import { loginWithFacebookPopup } from '../../../utils/facebookOAuth';
import { useAuthContext } from '../../../contexts/useAuthContext';

interface RegisterValues {
  fullName: string;
  email: string;
  phone?: string;
  password: string;
}

interface UseAuthReturn {
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (values: RegisterValues) => Promise<void>;
  socialLogin: (provider: string, token: string) => Promise<void>;
  facebookLogin: () => void;
  forgotPassword: (email: string) => Promise<void>;
}

const useAuth = (): UseAuthReturn => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const { isAuthenticated, login: contextLogin } = useAuthContext();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/lottery', { replace: true });
    }
  }, [navigate, isAuthenticated]);

  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/login', { email, password });
      const data = response.data.data;
      contextLogin(data, email);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      throw new Error(err.response?.data?.message || 'Login failed. Please try again.', { cause: error });
    } finally {
      setLoading(false);
    }
  };

  const register = async (values: RegisterValues) => {
    setLoading(true);
    try {
      await apiClient.post('/auth/register', values);
      message.success('Registration successful! Please login.');
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      throw new Error(err.response?.data?.message || 'Registration failed.', { cause: error });
    } finally {
      setLoading(false);
    }
  };

  const socialLogin = async (provider: string, token: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/social', { provider, token });
      contextLogin(response.data.data);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      throw new Error(err.response?.data?.message || 'Social login failed', { cause: error });
    } finally {
      setLoading(false);
    }
  };

  const facebookLogin = () => {
    loginWithFacebookPopup()
      .then((token: string) => socialLogin('FACEBOOK', token))
      .catch((err: Error) => {
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