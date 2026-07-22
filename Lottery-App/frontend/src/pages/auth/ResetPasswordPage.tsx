import React, { useEffect } from 'react';
import { Card, Form, Input, Button, Typography, message } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import apiClient from '../../api/apiClient';

const { Title, Text } = Typography;

type ResetPasswordFormValues = Record<'token' | 'newPassword' | 'confirmPassword', string>;

const ResetPasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [form] = Form.useForm<ResetPasswordFormValues>();

  const tokenFromUrl = searchParams.get('token') || '';

  useEffect(() => {
    if (tokenFromUrl) {
      form.setFieldsValue({ ['token']: tokenFromUrl });
    }
  }, [tokenFromUrl, form]);

  const onFinish = async (values: ResetPasswordFormValues) => {
    try {
      const payload: Record<'token' | 'newPassword', string> = {
        ['token']: values['token'].trim(),
        ['newPassword']: values['newPassword'],
      };

      await apiClient.post('/password/reset', payload);

      message.success('Password has been reset successfully. Please login.');
      navigate('/auth');
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to reset password');
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: '#f0f2f5',
        padding: '0 10px',
      }}
    >
      <Card
        style={{
          width: '100%',
          maxWidth: 420,
          borderRadius: 12,
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
        }}
      >
        <Title level={4} style={{ marginBottom: 8 }}>
          Reset Password
        </Title>

        <Text type="secondary">
          Enter the reset token from your email and choose a new password.
        </Text>

        {tokenFromUrl && (
          <Text
            type="secondary"
            style={{
              display: 'block',
              marginTop: 12,
            }}
          >
            Reset token loaded from your email link.
          </Text>
        )}

        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          style={{ marginTop: 16 }}
        >
          <Form.Item
            name="token"
            label="Reset Token"
            rules={[
              {
                required: true,
                message: 'Reset token is required',
              },
            ]}
          >
            <Input
              placeholder="Paste reset token"
              style={{ borderRadius: 2 }}
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="New Password"
            rules={[
              {
                required: true,
                message: 'New password is required',
              },
              {
                min: 10,
                message: 'Password must be at least 10 characters',
              },
              {
                max: 64,
                message: 'Password must be at most 64 characters',
              },
            ]}
          >
            <Input.Password
              placeholder="New password"
              autoComplete="new-password"
              style={{ borderRadius: 2 }}
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Confirm Password"
            dependencies={['newPassword']}
            rules={[
              {
                required: true,
                message: 'Please confirm your password',
              },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }

                  return Promise.reject(new Error('Passwords do not match!'));
                },
              }),
            ]}
          >
            <Input.Password
              placeholder="Confirm password"
              autoComplete="new-password"
              style={{ borderRadius: 2 }}
            />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            block
            style={{ borderRadius: 12, height: 40 }}
          >
            Reset Password
          </Button>
        </Form>
      </Card>
    </div>
  );
};

export default ResetPasswordPage;