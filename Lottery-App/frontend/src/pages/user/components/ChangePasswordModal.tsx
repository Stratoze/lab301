import React, { useState } from 'react';
import { Modal, Form, Input, Progress } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { getPasswordStrength } from '../utils/passwordStrength';

interface ChangePasswordModalProps {
  open: boolean;
  loading: boolean;
  onCancel: () => void;
  onChangePassword: (values: any) => void;
}

const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  open,
  loading,
  onCancel,
  onChangePassword
}) => {
  const [form] = Form.useForm();
  const [newPassword, setNewPassword] = useState('');
  const strength = getPasswordStrength(newPassword);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onChangePassword(values);
    } catch {
      // validation errors handled by antd
    }
  };

  return (
    <Modal 
      title="Change Password" 
      open={open} 
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item 
          name="oldPassword" 
          label="Enter Your Current Password" 
          rules={[{ required: true, message: 'Current password is required' }]}
        >
          <Input.Password prefix={<LockOutlined />} style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item 
          name="newPassword" 
          label="Enter Your New Password" 
          rules={[
            { required: true, message: 'New password is required' },
            { min: 10, message: 'Password must be at least 10 characters' }
          ]}
          extra={
            newPassword && (
              <div style={{ marginTop: 4 }}>
                <Progress
                  percent={strength.percent}
                  status={strength.status}
                  size="small"
                  format={() => strength.label}
                />
              </div>
            )
          }
        >
          <Input.Password
            prefix={<LockOutlined />}
            style={{ borderRadius: 12 }}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </Form.Item>
        <Form.Item 
          name="confirmPassword" 
          label="Confirm your new password" 
          dependencies={['newPassword']}
          rules={[
            { required: true, message: 'Please confirm your new password' },
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
          <Input.Password prefix={<LockOutlined />} style={{ borderRadius: 12 }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ChangePasswordModal;