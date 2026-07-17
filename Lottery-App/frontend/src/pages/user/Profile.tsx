import React, { useState, useEffect, useCallback } from 'react';
import { Card, Typography, Button, Space, Divider, message, Modal, Form, Input, Progress, Tag, Row, Col } from 'antd';
import { CopyOutlined, EditOutlined, LockOutlined, LinkOutlined, DisconnectOutlined, GoogleOutlined, FacebookFilled, PhoneOutlined } from '@ant-design/icons';
import { GoogleLogin } from '@react-oauth/google';
import { loginWithFacebookPopup } from '../../utils/facebookOAuth';
import apiClient from '../../api/apiClient';

const { Text } = Typography;

const getPasswordStrength = (password: string): { percent: number; status: 'exception' | 'active' | 'success'; label: string } => {
  if (!password) return { percent: 0, status: 'exception', label: '' };
  if (password.length < 10) {
    return { percent: Math.min((password.length / 10) * 30, 30), status: 'exception', label: 'Too short (min 10 characters)' };
  }
  let score = 40;
  if (password.length >= 12) score += 10;
  if (password.length >= 16) score += 10;
  if (/[A-Z]/.test(password)) score += 15;
  if (/[a-z]/.test(password)) score += 5;
  if (/[0-9]/.test(password)) score += 10;
  if (/[^A-Za-z0-9]/.test(password)) score += 10;
  const percent = Math.min(score, 100);
  if (percent >= 80) return { percent, status: 'success', label: 'Strong' };
  if (percent >= 50) return { percent, status: 'active', label: 'Medium' };
  return { percent, status: 'exception', label: 'Weak' };
};

const Profile: React.FC = () => {
  const [isPassModalOpen, setIsPassModalOpen] = useState(false);
  const [isEditNameOpen, setIsEditNameOpen] = useState(false);
  const [isPhoneModalOpen, setIsPhoneModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [newPassword, setNewPassword] = useState('');
  const [form] = Form.useForm();
  const [passForm] = Form.useForm();
  const [phoneForm] = Form.useForm();
 
  const [user, setUser] = useState({
    userCode: localStorage.getItem('userCode') || 'N/A',
    email: localStorage.getItem('email') || 'N/A',
    fullName: localStorage.getItem('fullName') || 'N/A'
  });

  const [linkedAccounts, setLinkedAccounts] = useState({
    googleLinked: false,
    facebookLinked: false,
    phone: null as string | null
  });

  const fetchLinkedAccounts = useCallback(async () => {
    try {
      const res = await apiClient.get('/user/linked-accounts');
      setLinkedAccounts(res.data.data);
    } catch {
      // Silently fail; user can still see profile
    }
  }, []);

  useEffect(() => {
    fetchLinkedAccounts();
  }, [fetchLinkedAccounts]);

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Copied to clipboard');
  };

  const handleUpdateName = async (values: { fullName: string }) => {
    setLoading(true);
    try {
      const res = await apiClient.put('/user/me', { fullName: values.fullName });
      const newName = res.data.data.fullName;
      localStorage.setItem('fullName', newName);
      setUser({ ...user, fullName: newName });
      message.success('Profile updated successfully');
      setIsEditNameOpen(false);
    } catch (e: any) {
      const msg = e.response?.data?.message || 'Update failed';
      message.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (values: any) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('New passwords do not match!');
      return;
    }
    setLoading(true);
    try {
      await apiClient.post('/user/change-password', {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword
      });
      message.success('Password changed successfully!');
      setIsPassModalOpen(false);
      passForm.resetFields();
    } catch (e: any) {
      const msg = e.response?.data?.message || 'Failed to change password. Check your current password.';
      message.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleLinkSocial = async (provider: string, token: string) => {
    setLoading(true);
    try {
      await apiClient.post('/user/link-social', { provider, token });
      message.success(`${provider} account linked!`);
      fetchLinkedAccounts();
    } catch (e: any) {
      message.error(e.response?.data?.message || `Failed to link ${provider}`);
    } finally {
      setLoading(false);
    }
  };

  const handleFacebookLink = () => {
    loginWithFacebookPopup()
      .then(token => handleLinkSocial('FACEBOOK', token))
      .catch(err => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  const handleUnlinkPhone = async () => {
    setLoading(true);
    try {
      await apiClient.post('/user/unlink-phone');
      message.success('Phone number unlinked');
      fetchLinkedAccounts();
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to unlink phone');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdatePhone = async (values: { phone: string }) => {
    setLoading(true);
    try {
      // Use updateMe to set phone (reuse existing endpoint; add phone to body)
      await apiClient.put('/user/me', { phone: values.phone });
      message.success('Phone number updated');
      setIsPhoneModalOpen(false);
      fetchLinkedAccounts();
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to update phone');
    } finally {
      setLoading(false);
    }
  };

  const strength = getPasswordStrength(newPassword);

  const handlePassModalOk = async () => {
    try {
      const values = await passForm.validateFields();
      await handleChangePassword(values);
    } catch {
      // validation failed, Ant Design shows inline errors
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <Card title="My Profile" style={{ borderRadius: 12 }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <div>
            <Text type="secondary">User Code</Text>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text strong>{user.userCode}</Text>
              <Button icon={<CopyOutlined />} type="text" onClick={() => copyToClipboard(user.userCode)} />
            </div>
          </div>

          <div>
            <Text type="secondary">Email</Text>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text strong>{user.email}</Text>
              <Button icon={<CopyOutlined />} type="text" onClick={() => copyToClipboard(user.email)} />
            </div>
          </div>

          <div>
            <Text type="secondary">Full Name</Text>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text strong>{user.fullName}</Text>
              <Button icon={<EditOutlined />} type="text" onClick={() => setIsEditNameOpen(true)} />
            </div>
          </div>

          <Divider />

          {/* Linked Accounts Section */}
          <div>
            <Text strong style={{ fontSize: 16 }}>Linked Accounts</Text>
          </div>

          <Row align="middle" justify="space-between">
            <Col>
              <Space>
                <GoogleOutlined style={{ fontSize: 18, color: linkedAccounts.googleLinked ? '#52c41a' : '#d9d9d9' }} />
                <Text>Google</Text>
                {linkedAccounts.googleLinked ? (
                  <Tag color="success">Linked</Tag>
                ) : (
                  <Tag>Not linked</Tag>
                )}
              </Space>
            </Col>
            <Col>
              {!linkedAccounts.googleLinked && (
                <GoogleLogin
                  onSuccess={(credentialResponse) => {
                    handleLinkSocial('GOOGLE', credentialResponse.credential!);
                  }}
                  onError={() => message.error('Google login failed')}
                  size="medium"
                  shape="rectangular"
                  text="continue_with"
                />
              )}
            </Col>
          </Row>

          <Row align="middle" justify="space-between">
            <Col>
              <Space>
                <FacebookFilled style={{ fontSize: 18, color: linkedAccounts.facebookLinked ? '#52c41a' : '#d9d9d9' }} />
                <Text>Facebook</Text>
                {linkedAccounts.facebookLinked ? (
                  <Tag color="success">Linked</Tag>
                ) : (
                  <Tag>Not linked</Tag>
                )}
              </Space>
            </Col>
            <Col>
              {!linkedAccounts.facebookLinked && (
                <Button
                  icon={<FacebookFilled />}
                  onClick={handleFacebookLink}
                  style={{ borderRadius: 12 }}
                >
                  Link
                </Button>
              )}
            </Col>
          </Row>

          <Row align="middle" justify="space-between">
            <Col>
              <Space>
                <PhoneOutlined style={{ fontSize: 18, color: linkedAccounts.phone ? '#52c41a' : '#d9d9d9' }} />
                <Text>Phone</Text>
                {linkedAccounts.phone ? (
                  <Tag color="success">{linkedAccounts.phone}</Tag>
                ) : (
                  <Tag>Not linked</Tag>
                )}
              </Space>
            </Col>
            <Col>
              {linkedAccounts.phone ? (
                <Button
                  icon={<DisconnectOutlined />}
                  danger
                  onClick={handleUnlinkPhone}
                  style={{ borderRadius: 12 }}
                >
                  Unlink
                </Button>
              ) : (
                <Button
                  icon={<LinkOutlined />}
                  onClick={() => setIsPhoneModalOpen(true)}
                  style={{ borderRadius: 12 }}
                >
                  Add Phone
                </Button>
              )}
            </Col>
          </Row>

          <Divider />

          <Button block onClick={() => setIsPassModalOpen(true)} style={{ borderRadius: 12 }}>
            Change my password
          </Button>
        </Space>
      </Card>

      {/* Edit Name Modal */}
      <Modal
        title="Edit Full Name"
        open={isEditNameOpen}
        onCancel={() => setIsEditNameOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleUpdateName} initialValues={{ fullName: user.fullName }}>
          <Form.Item name="fullName" label="New Full Name" rules={[{ required: true, message: 'Full name is required' }]}>
            <Input style={{ borderRadius: 2 }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Change Password Modal */}
      <Modal
        title="Change Password"
        open={isPassModalOpen}
        onCancel={() => setIsPassModalOpen(false)}
        onOk={handlePassModalOk}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={passForm} layout="vertical">
          <Form.Item
            name="oldPassword"
            label="Enter Your Current Password"
            rules={[{ required: true, message: 'Current password is required' }]}
          >
            <Input.Password prefix={<LockOutlined />} style={{ borderRadius: 2 }} />
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
              style={{ borderRadius: 2 }}
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
            <Input.Password prefix={<LockOutlined />} style={{ borderRadius: 2 }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Phone Modal */}
      <Modal
        title="Add Phone Number"
        open={isPhoneModalOpen}
        onCancel={() => setIsPhoneModalOpen(false)}
        onOk={() => phoneForm.submit()}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={phoneForm} layout="vertical" onFinish={handleUpdatePhone}>
          <Form.Item
            name="phone"
            label="Phone Number"
            rules={[
              { required: true, message: 'Please enter your phone number' },
              { pattern: /^\d{7,15}$/, message: 'Enter a valid phone number (7-15 digits)' }
            ]}
          >
            <Input placeholder="e.g., 0912345678" style={{ borderRadius: 2 }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Profile; 
