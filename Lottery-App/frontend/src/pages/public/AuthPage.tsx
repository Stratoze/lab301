import React, { useState, useEffect } from 'react';
import { Card, Tabs, Form, Input, Button, message, Divider, Modal, Progress, Typography } from 'antd';
import { GoogleLogin } from '@react-oauth/google';
import { loginWithFacebookPopup } from '../../utils/facebookOAuth';
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined, FacebookFilled } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';

const { Text } = Typography;

const COMMON_PASSWORDS = new Set([
  'password', 'password1', 'password123', '123456', '12345678', '123456789',
  '1234567890', 'qwerty', 'qwerty123', 'abc123', 'letmein', 'monkey',
  'dragon', 'master', 'admin', 'login', 'welcome', 'iloveyou', 'trustno1',
  'sunshine', 'princess', 'football', 'baseball', 'starwars', 'access',
  'shadow', 'michael', 'superman', 'batman', 'hello', 'charlie', 'donald',
  'mustang', 'hunter', 'ranger', 'thomas', 'george', 'robert', 'jennifer',
  'jessica', 'amanda', 'joshua', 'andrew', 'william', 'matthew', 'daniel',
  'vietnam', 'vietnam123', 'hanoi', 'saigon', 'veso123',
  'lottery', 'lottery123', 'doveso', 'doveso123',
  'aaaaaa', 'aaaaaaa', '111111', '222222', '333333',
  '444444', '555555', '666666', '777777', '888888', '999999', '000000'
]);

const getPasswordStrength = (password: string): { percent: number; status: 'exception' | 'active' | 'success'; label: string } => {
  if (!password) return { percent: 0, status: 'exception', label: '' };
 
  if (password.length < 10) {
    return { percent: Math.min((password.length / 10) * 30, 30), status: 'exception', label: 'Too short (min 10 characters)' };
  }

  if (COMMON_PASSWORDS.has(password.toLowerCase())) {
    return { percent: 20, status: 'exception', label: 'This password is too common' };
  }

  let score = 40;
  if (password.length >= 12) score += 10;
  if (password.length >= 16) score += 10;
  if (/[A-Z]/.test(password)) score += 15;
  if (/[a-z]/.test(password)) score += 5;
  if (/[0-9]/.test(password)) score += 10;
  if (/[^A-Za-z0-9]/.test(password)) score += 10;

  const percent = Math.min(score, 100);
  if (percent >= 80) return { percent, status: 'success', label: 'Strong' };
  if (percent >= 50) return { percent, status: 'active', label: 'Medium' };
  return { percent, status: 'exception', label: 'Weak' };
};

const AuthPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [resetForm] = Form.useForm();
  const [registerPassword, setRegisterPassword] = useState('');

  const handleSocialLogin = async (provider: string, token: string) => {
    setLoading(true);
    try {
      const response = await apiClient.post('/auth/social', { provider, token });
      const { token: jwtToken, role, userCode, fullName } = response.data.data;
      localStorage.setItem('token', jwtToken);
      localStorage.setItem('role', role);
      localStorage.setItem('userCode', userCode);
      localStorage.setItem('email', response.data.data.email || '');
      localStorage.setItem('fullName', fullName);
      message.success('Login successful!');
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Social login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleFacebookLogin = () => {
    loginWithFacebookPopup()
      .then(token => handleSocialLogin('FACEBOOK', token))
      .catch(err => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  // Redirect logged-in users away from auth page
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      navigate('/lottery', { replace: true });
    }
  }, [navigate]);

  const onLogin = async (values: any) => {
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
      // Use navigate instead of window.location.href
      navigate('/lottery', { replace: true });
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Login failed. Please try again.';
      message.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const onRegister = async (values: any) => {
    setLoading(true);
    try {
      await apiClient.post('/auth/register', values);
      message.success('Registration successful! Please login.');
      setActiveTab('login');
      setRegisterPassword('');
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (values: { email: string }) => {
    setResetLoading(true);
    try {
      await apiClient.post('/password/forgot', { email: values.email });
      message.success('If the email is registered, a reset link has been sent.');
      setIsResetModalOpen(false);
      resetForm.resetFields();
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Something went wrong.';
      message.error(msg);
    } finally {
      setResetLoading(false);
    }
  };

  const strength = getPasswordStrength(registerPassword);

  return (
    <>
      <div className="auth-container" style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: '#f0f2f5',
        padding: '0 10px'
      }}>
        <Card className="auth-card" style={{
          width: '100%',
          maxWidth: 400,
          borderRadius: 12,
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
        }}>
          <Tabs
            activeKey={activeTab}
            onChange={(key) => { setActiveTab(key); setRegisterPassword(''); }}
            tabBarStyle={{ marginBottom: 24 }}
            items={[
              {
                key: 'login',
                label: 'Sign In',
                children: (
                  <Form layout="vertical" onFinish={onLogin}>
                    <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Please input email!' }]}>
                      <Input prefix={<UserOutlined />} placeholder="Email" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please input password!' }]}>
                      <Input.Password prefix={<LockOutlined />} placeholder="Password" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <div style={{ marginBottom: 16, textAlign: 'right' }}>
                      <a href="#" onClick={(e) => { e.preventDefault(); setIsResetModalOpen(true); }}>Forgot your password?</a>
                    </div>
                    <Form.Item>
                      <Button type="primary" htmlType="submit" loading={loading} block style={{ borderRadius: 12, height: 40 }}>
                        Sign In
                      </Button>
                    </Form.Item>
                    <Divider>OR</Divider>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                      <GoogleLogin
                        onSuccess={(credentialResponse) => {
                          handleSocialLogin('GOOGLE', credentialResponse.credential!);
                        }}
                        onError={() => message.error('Google login failed')}
                        size="large"
                        shape="pill"
                        width="100%"
                      />
                      <Button
                        icon={<FacebookFilled />}
                        block
                        style={{ borderRadius: 12, height: 40, backgroundColor: '#1877F2', color: '#fff' }}
                        onClick={handleFacebookLogin}
                      >
                        Continue with Facebook
                      </Button>
                    </div>
                  </Form>
                ),
              },
              {
                key: 'register',
                label: 'Sign Up',
                children: (
                  <Form layout="vertical" onFinish={onRegister}>
                    <Form.Item name="fullName" label="Full Name" rules={[{ required: true, message: 'Full name is required' }]}>
                      <Input prefix={<UserOutlined />} placeholder="Full Name" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Valid email is required' }]}>
                      <Input prefix={<MailOutlined />} placeholder="Email" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <Form.Item name="phone" label="Phone Number (optional)">
                      <Input prefix={<PhoneOutlined />} placeholder="Phone number" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <Form.Item
                      name="password"
                      label="Password"
                      rules={[
                        { required: true, message: 'Password is required' },
                        { min: 10, message: 'Password must be at least 10 characters' },
                        { max: 64, message: 'Password must be no more than 64 characters' },
                        {
                          validator: (_, value) => {
                            if (value && COMMON_PASSWORDS.has(value.toLowerCase())) {
                              return Promise.reject(new Error('This password is too common. Please choose a stronger one.'));
                            }
                            return Promise.resolve();
                          }
                        }
                      ]}
                      extra={
                        registerPassword && (
                          <div style={{ marginTop: 4 }}>
                            <Progress
                              percent={strength.percent}
                              status={strength.status}
                              size="small"
                              format={() => strength.label}
                            />
                            <Text type="secondary" style={{ fontSize: 11 }}>
                              Use a memorable passphrase ? special characters optional.
                            </Text>
                          </div>
                        )
                      }
                    >
                      <Input.Password
                        prefix={<LockOutlined />}
                        placeholder="Password (10-64 characters)"
                        style={{ borderRadius: 12 }}
                        onChange={(e) => setRegisterPassword(e.target.value)}
                      />
                    </Form.Item>
                    <Form.Item name="confirm" label="Confirm Password" dependencies={['password']} rules={[
                      { required: true, message: 'Please confirm your password' },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue('password') === value) return Promise.resolve();
                          return Promise.reject(new Error('Passwords do not match!'));
                        },
                      }),
                    ]}>
                      <Input.Password prefix={<LockOutlined />} placeholder="Confirm Password" style={{ borderRadius: 12 }} />
                    </Form.Item>
                    <Form.Item>
                      <Button type="primary" htmlType="submit" loading={loading} block style={{ borderRadius: 12, height: 40 }}>
                        Sign Up
                      </Button>
                    </Form.Item>
                    <Divider>OR</Divider>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                      <GoogleLogin
                        onSuccess={(credentialResponse) => {
                          handleSocialLogin('GOOGLE', credentialResponse.credential!);
                        }}
                        onError={() => message.error('Google login failed')}
                        size="large"
                        shape="pill"
                        width="100%"
                      />
                      <Button
                        icon={<FacebookFilled />}
                        block
                        style={{ borderRadius: 12, height: 40, backgroundColor: '#1877F2', color: '#fff' }}
                        onClick={handleFacebookLogin}
                      >
                        Continue with Facebook
                      </Button>
                    </div>
                  </Form>
                ),
              },
            ]}
          />
        </Card>

        {/* Reset Password Modal */}
        <Modal
          title="Reset Password"
          open={isResetModalOpen}
          onCancel={() => setIsResetModalOpen(false)}
          footer={null}
          destroyOnClose
        >
          <Form
            form={resetForm}
            layout="vertical"
            onFinish={handleForgotPassword}
          >
            <Form.Item name="email" label="Enter your email address" rules={[{ required: true, type: 'email' }]}>
              <Input placeholder="enter your email" style={{ borderRadius: 12 }} />
            </Form.Item>
            <div style={{ textAlign: 'right', gap: 8, display: 'flex', justifyContent: 'flex-end' }}>
              <Button type="primary" htmlType="submit" loading={resetLoading} style={{ borderRadius: 12 }}>Send Request</Button>
              <Button onClick={() => setIsResetModalOpen(false)} style={{ borderRadius: 12 }}>Cancel</Button>
            </div>
          </Form>
        </Modal>
      </div>

      {/* Mobile: 10px gap, tabs fill card width */}
      <style>{`
        @media (max-width: 768px) {
          .auth-container {
            padding: 0 10px !important;
          }
          .auth-card {
            margin: 0 !important;
            border-radius: 12px !important;
          }
        }

        .auth-card .ant-tabs-nav-list {
          width: 100%;
        }
        .auth-card .ant-tabs-tab {
          flex: 1;
          justify-content: center;
          margin: 0 !important;
        }
        .auth-card .ant-tabs-tab + .ant-tabs-tab {
          margin-left: 0 !important;
        }
      `}</style>
    </>
  );
};

export default AuthPage; 