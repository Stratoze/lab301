import React from 'react';
import { Modal, Form, Input } from 'antd';

interface EditNameModalProps {
  open: boolean;
  fullName: string;
  loading: boolean;
  onCancel: () => void;
  onUpdate: (fullName: string) => void;
}

const EditNameModal: React.FC<EditNameModalProps> = ({
  open,
  fullName,
  loading,
  onCancel,
  onUpdate
}) => {
  const [form] = Form.useForm();

  return (
    <Modal 
      title="Edit Full Name" 
      open={open} 
      onCancel={onCancel} 
      onOk={() => form.submit()} 
      confirmLoading={loading}
      destroyOnClose
    >
      <Form 
        form={form} 
        layout="vertical" 
        onFinish={(values) => onUpdate(values.fullName)} 
        initialValues={{ fullName }}
      >
        <Form.Item name="fullName" label="New Full Name" rules={[{ required: true, message: 'Full name is required' }]}>
          <Input style={{ borderRadius: 12 }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EditNameModal;