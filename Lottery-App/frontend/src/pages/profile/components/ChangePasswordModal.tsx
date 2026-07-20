import React from 'react';
import { Modal, Form, Input } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { createStyles } from 'antd-style';
import PasswordField from '../../../components/PasswordField';

const useStyles = createStyles(({ token, css }) => ({
  input: css`
    border-radius: ${token.borderRadiusSM}px;
  `,
}));

interface ChangePasswordModalProps {
  open: boolean;
  loading: boolean;
  hasPassword: boolean;
  onCancel: () => void;
  onSubmit: (values: { oldPassword: string; newPassword: string; confirmPassword: string }) => Promise<void>;
}

const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  open,
  loading,
  hasPassword,
  onCancel,
  onSubmit,
}) => {
  const [form] = Form.useForm();
  const { styles } = useStyles();

  React.useEffect(() => {
    if (open) {
      form.resetFields();
    }
  }, [open, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
    } catch {
      // validation failed, Ant Design shows inline errors
    }
  };

  return (
    <Modal
      title={hasPassword ? 'Change Password' : 'Set a Password'}
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnHidden
      zIndex={1100}
    >
      <Form form={form} layout="vertical">
        {hasPassword && (
          <Form.Item
            name="oldPassword"
            label="Enter Your Current Password"
            rules={[{ required: true, message: 'Current password is required' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              className={styles.input}
            />
          </Form.Item>
        )}
        <PasswordField name="newPassword" label="New Password" />
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
          <Input.Password
            prefix={<LockOutlined />}
            className={styles.input}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ChangePasswordModal;