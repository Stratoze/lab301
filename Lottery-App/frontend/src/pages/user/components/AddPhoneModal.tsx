import React from 'react';
import { Modal, Form, Input } from 'antd';

interface AddPhoneModalProps {
  open: boolean;
  loading: boolean;
  onCancel: () => void;
  onAdd: (values: { phone: string }) => void;
}

const AddPhoneModal: React.FC<AddPhoneModalProps> = ({
  open,
  loading,
  onCancel,
  onAdd
}) => {
  const [form] = Form.useForm();

  return (
    <Modal
      title="Add Phone Number"
      open={open}
      onCancel={onCancel}
      onOk={() => form.submit()}
      confirmLoading={loading}
      destroyOnClose
    >
      <Form form={form} layout="vertical" onFinish={onAdd}>
        <Form.Item
          name="phone"
          label="Phone Number"
          rules={[
            { required: true, message: 'Please enter your phone number' },
            { pattern: /^\d{7,15}$/, message: 'Enter a valid phone number (7-15 digits)' }
          ]}
        >
          <Input placeholder="e.g., 0912345678" style={{ borderRadius: 12 }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default AddPhoneModal;