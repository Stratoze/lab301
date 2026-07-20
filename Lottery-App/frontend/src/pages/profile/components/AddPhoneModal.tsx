import React from 'react';
import { Modal, Form, Input } from 'antd';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token, css }) => ({
  input: css`
    border-radius: ${token.borderRadiusSM}px;
  `,
}));

interface AddPhoneModalProps {
  open: boolean;
  loading: boolean;
  onCancel: () => void;
  onSubmit: (values: { phone: string }) => Promise<void>;
}

const AddPhoneModal: React.FC<AddPhoneModalProps> = ({ open, loading, onCancel, onSubmit }) => {
  const [form] = Form.useForm();
  const { styles } = useStyles();

  React.useEffect(() => {
    if (open) {
      form.resetFields();
    }
  }, [open, form]);

  const handleOk = () => {
    form.submit();
  };

  return (
    <Modal
      title="Add Phone Number"
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnHidden
      zIndex={1100}
    >
      <Form form={form} layout="vertical" onFinish={onSubmit}>
        <Form.Item
          name="phone"
          label="Phone Number"
          rules={[
            { required: true, message: 'Please enter your phone number' },
            { pattern: /^\d{7,15}$/, message: 'Enter a valid phone number (7-15 digits)' },
          ]}
        >
          <Input placeholder="e.g., 0912345678" className={styles.input} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default AddPhoneModal;