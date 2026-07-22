import React from 'react';
import { Form, Input, Button, Divider, message } from 'antd';
import { GoogleLogin } from '@react-oauth/google';
import { UserOutlined, MailOutlined, PhoneOutlined, LockOutlined, FacebookFilled } from '@ant-design/icons';
import PasswordField from '../../../components/PasswordField';

interface RegisterValues {
  fullName: string;
  email: string;
  phone?: string;
  password: string;
  confirm: string;
}

interface RegisterFormProps {
  loading: boolean;
  onRegister: (values: RegisterValues) => Promise<void>;
  onSwitchToLogin: () => void;
  onSocialLogin: (provider: string, token: string) => Promise<void>;
  onFacebookLogin: () => void;
}

const RegisterForm: React.FC<RegisterFormProps> = ({
  loading,
  onRegister,
  onSwitchToLogin,
  onSocialLogin,
  onFacebookLogin,
}) => {
  const onFinish = async (values: RegisterValues) => {
    try {
      await onRegister(values);
      onSwitchToLogin();
    } catch (err: unknown) {
      const error = err as Error;
      message.error(error.message || 'Registration failed');
    }
  };

  return (
    <Form layout="vertical" onFinish={onFinish}>
      <Form.Item name="fullName" label="Full Name" rules={[{ required: true, message: 'Full name is required' }]}>
        <Input prefix={<UserOutlined />} placeholder="Full Name" style={{ borderRadius: 12 }} />
      </Form.Item>
      <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Valid email is required' }]}>
        <Input prefix={<MailOutlined />} placeholder="Email" style={{ borderRadius: 12 }} />
      </Form.Item>
      <Form.Item name="phone" label="Phone Number (optional)">
        <Input prefix={<PhoneOutlined />} placeholder="Phone number" style={{ borderRadius: 12 }} />
      </Form.Item>

      <PasswordField />

      <Form.Item
        name="confirm"
        label="Confirm Password"
        dependencies={['password']}
        rules={[
          { required: true, message: 'Please confirm your password' },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('password') === value) return Promise.resolve();
              return Promise.reject(new Error('Passwords do not match!'));
            },
          }),
        ]}
      >
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
            onSocialLogin('GOOGLE', credentialResponse.credential!);
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
          onClick={onFacebookLogin}
        >
          Continue with Facebook
        </Button>
      </div>
    </Form>
  );
};

export default RegisterForm;