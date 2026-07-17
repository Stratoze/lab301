import React, { useState } from 'react';
import { Modal, Form, Input, Button, message } from 'antd';
import apiClient from '../../../api/apiClient';

interface ResetModalProps {
  open: boolean;
  onCancel: () => void;
}

const ResetModal: React.FC<ResetModalProps> = ({ open, onCancel }) => {
  const [resetLoading, setResetLoading] = useState(false);
  const [form] = Form.useForm();

  const handleForgotPassword = async (values: { email: string }) => {
    setResetLoading(true);
    try {
      await apiClient.post('/password/forgot', { email: values.email });
      message.success('If the email is registered, a reset link has been sent.');
      onCancel();
      form.resetFields();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Something went wrong.');
    } finally {
      setResetLoading(false);
    }
  };

  return (
    <Modal
      title="Reset Password"
      open={open}
      onCancel={onCancel}
      footer={null}
      destroyOnClose
    >
      <Form 
        form={form} 
        layout="vertical" 
        onFinish={handleForgotPassword}
      >
        <Form.Item name="email" label="Enter your email address" rules={[{ required: true, type: 'email' }]}>
          <Input placeholder="enter your email" style={{ borderRadius: 12 }} />
        </Form.Item>
        <div style={{ textAlign: 'right', gap: 8, display: 'flex', justifyContent: 'flex-end' }}>
          <Button type="primary" htmlType="submit" loading={resetLoading} style={{ borderRadius: 12 }}>Send Request</Button>
          <Button onClick={onCancel} style={{ borderRadius: 12 }}>Cancel</Button>
        </div>
      </Form>
    </Modal>
  );
};

export default ResetModal;