import React from 'react';
import { Form, Input, Progress, Typography } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import usePasswordRules, { getPasswordStrength } from '../hooks/usePasswordRules';

const { Text } = Typography;

interface PasswordFieldProps {
  name?: string;
  label?: string;
  placeholder?: string;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  showStrength?: boolean;
  extraRules?: Record<string, unknown>[];
  dependencies?: string[];
}

const PasswordField: React.FC<PasswordFieldProps> = ({
  name = 'password',
  label = 'Password',
  placeholder,
  value,
  onChange,
  showStrength = true,
  extraRules = [],
  dependencies,
}) => {
  const rules = usePasswordRules();
  const currentValue = value || '';

  const strength = getPasswordStrength(currentValue, rules);

  const fieldRules = [
    { required: true, message: `${label} is required` },
    { min: rules.minLength, message: `Password must be at least ${rules.minLength} characters` },
    { max: rules.maxLength, message: `Password must be no more than ${rules.maxLength} characters` },
    {
      validator: (_: unknown, val: string) => {
        if (val && rules.blocklist.has(val.toLowerCase())) {
          return Promise.reject(new Error('This password is too common. Please choose a stronger one.'));
        }
        return Promise.resolve();
      },
    },
    ...extraRules,
  ];

  return (
    <Form.Item
      name={name}
      label={label}
      rules={fieldRules}
      dependencies={dependencies}
      extra={
        showStrength && currentValue && (
          <div style={{ marginTop: 4 }}>
            <Progress
              percent={strength.percent}
              status={strength.status}
              size="small"
              format={() => strength.label}
            />
            <Text type="secondary" style={{ fontSize: 11 }}>
              Use a memorable passphrase ? special characters optional.
            </Text>
          </div>
        )
      }
    >
      <Input.Password
        prefix={<LockOutlined />}
        placeholder={placeholder || `${label} (${rules.minLength}-${rules.maxLength} characters)`}
        style={{ borderRadius: 12 }}
        onChange={onChange}
        value={value}
      />
    </Form.Item>
  );
};

export default PasswordField;