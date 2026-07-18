import React, { useState } from 'react';
import { Modal, Form, Input, Button, message } from 'antd';

interface ResetModalProps {
  open: boolean;
  onClose: () => void;
  onReset: (email: string) => Promise<void>;
}

const ResetModal: React.FC<ResetModalProps> = ({ open, onClose, onReset }) => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const handleSubmit = async (values: { email: string }) => {
    setLoading(true);
    try {
      await onReset(values.email);
      onClose();
      form.resetFields();
    } catch (err: any) {
      message.error(err.message || 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Reset Password"
      open={open}
      onCancel={onClose}
      footer={null}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item name="email" label="Enter your email address" rules={[{ required: true, type: 'email' }]}>
          <Input placeholder="enter your email" style={{ borderRadius: 12 }} />
        </Form.Item>
        <div style={{ textAlign: 'right', gap: 8, display: 'flex', justifyContent: 'flex-end' }}>
          <Button type="primary" htmlType="submit" loading={loading} style={{ borderRadius: 12 }}>
            Send Request
          </Button>
          <Button onClick={onClose} style={{ borderRadius: 12 }}>
            Cancel
          </Button>
        </div>
      </Form>
    </Modal>
  );
};

export default ResetModal;