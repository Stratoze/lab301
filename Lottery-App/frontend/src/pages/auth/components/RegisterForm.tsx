import React, { useState } from 'react';
import { Form, Input, Button, Progress, Typography, Divider, message } from 'antd';
import { UserOutlined, MailOutlined, PhoneOutlined, LockOutlined, FacebookFilled } from '@ant-design/icons';
import { GoogleLogin } from '@react-oauth/google';
import { getPasswordStrength } from '../utils/passwordStrength';
import { loginWithFacebookPopup } from '../../../utils/facebookOAuth';

const { Text } = Typography;

interface RegisterFormProps {
  loading: boolean;
  rules: { minLength: number; maxLength: number; blocklist: string[] } | null;
  onRegister: (values: any) => void;
  onSocialLogin: (provider: string, token: string) => void;
}

const RegisterForm: React.FC<RegisterFormProps> = ({ 
  loading, 
  rules, 
  onRegister, 
  onSocialLogin 
}) => {
  const [password, setPassword] = useState('');
  const minLength = rules?.minLength || 10;
  const blocklistSet = new Set(rules?.blocklist || []);

  const strength = getPasswordStrength(password, blocklistSet, minLength);

  const handleFacebookLogin = () => {
    loginWithFacebookPopup()
      .then(token => onSocialLogin('FACEBOOK', token))
      .catch(err => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  return (
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
          { min: minLength, message: `Password must be at least ${minLength} characters` },
          { max: rules?.maxLength || 64, message: `Password must be no more than ${rules?.maxLength || 64} characters` },
          {
            validator: (_, value) => {
              if (value && blocklistSet.has(value.toLowerCase())) {
                return Promise.reject(new Error('This password is too common. Please choose a stronger one.'));
              }
              return Promise.resolve();
            }
          }
        ]}
        extra={
          password && (
            <div style={{ marginTop: 4 }}>
              <Progress 
                percent={strength.percent} 
                status={strength.status} 
                size="small" 
                format={() => strength.label}
              />
              <Text type="secondary" style={{ fontSize: 11 }}>
                Use a memorable passphrase - special characters optional.
              </Text>
            </div>
          )
        }
      >
        <Input.Password 
          prefix={<LockOutlined />} 
          placeholder={`Password (${minLength}-${rules?.maxLength || 64} characters)`} 
          style={{ borderRadius: 12 }}
          onChange={(e) => setPassword(e.target.value)}
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
          onClick={handleFacebookLogin}
        >
          Continue with Facebook
        </Button>
      </div>
    </Form>
  );
};

export default RegisterForm;