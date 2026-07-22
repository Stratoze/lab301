import React from 'react';
import { Modal, Form, Input } from 'antd';

interface EditNameModalProps {
  open: boolean;
  loading: boolean;
  currentName: string;
  onCancel: () => void;
  onSubmit: (values: { fullName: string }) => Promise<void>;
}

const EditNameModal: React.FC<EditNameModalProps> = ({ open, loading, currentName, onCancel, onSubmit }) => {
  const [form] = Form.useForm();

  React.useEffect(() => {
    if (open) {
      form.setFieldsValue({ fullName: currentName });
    }
  }, [open, currentName, form]);

  const handleOk = () => {
    form.submit();
  };

  return (
    <Modal
      title="Edit Full Name"
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnHidden
      zIndex={1100}
    >
      <Form form={form} layout="vertical" onFinish={onSubmit}>
        <Form.Item
          name="fullName"
          label="New Full Name"
          rules={[{ required: true, message: 'Full name is required' }]}
        >
          <Input/>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EditNameModal;