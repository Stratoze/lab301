import React from 'react';
import { Form, Input, Button, Divider, message } from 'antd';
import { GoogleLogin } from '@react-oauth/google';
import { UserOutlined, LockOutlined, FacebookFilled } from '@ant-design/icons';

interface LoginFormProps {
  loading: boolean;
  onLogin: (email: string, password: string) => Promise<void>;
  onSocialLogin: (provider: string, token: string) => Promise<void>;
  onFacebookLogin: () => void;
  onForgotPassword: () => void;
}

const LoginForm: React.FC<LoginFormProps> = ({
  loading,
  onLogin,
  onSocialLogin,
  onFacebookLogin,
  onForgotPassword,
}) => {
  const onFinish = async (values: { email: string; password: string }) => {
    try {
      await onLogin(values.email, values.password);
    } catch (err: unknown) {
      const error = err as Error;
      message.error(error.message || 'Login failed');
    }
  };

  return (
    <Form layout="vertical" onFinish={onFinish}>
      <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Please input email!' }]}>
        <Input prefix={<UserOutlined />} placeholder="Email"/>
      </Form.Item>
      <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please input password!' }]}>
        <Input.Password prefix={<LockOutlined />} placeholder="Password"/>
      </Form.Item>
      <div style={{ marginBottom: 16, textAlign: 'right' }}>
        <a href="#" onClick={(e) => { e.preventDefault(); onForgotPassword(); }}>
          Forgot your password?
        </a>
      </div>
      <Form.Item>
        <Button 
          type="primary" 
          htmlType="submit" 
          loading={loading} 
          block style={{ height: 40 }}
        >
          Sign In
        </Button>
      </Form.Item>
      <Divider>OR</Divider>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <GoogleLogin
          onSuccess={(credentialResponse) => {
            onSocialLogin('GOOGLE', credentialResponse.credential!);
          }}
          onError={() => message.error('Google login failed')}
          size="large"
          width="100%"
        />
        <Button
          icon={<FacebookFilled />}
          block
          style={{ 
            height: 40, 
            backgroundColor: '#1877F2', 
            color: '#fff' 
          }}
          onClick={onFacebookLogin}
        >
          Continue with Facebook
        </Button>
      </div>
    </Form>
  );
};

export default LoginForm;