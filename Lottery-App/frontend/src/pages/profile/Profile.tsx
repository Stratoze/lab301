import React, { useState, useEffect, useCallback } from 'react';
import { Card, Typography, Button, Space, Divider, message, Modal, Form, Input, Tag, Row, Col } from 'antd';
import { CopyOutlined, EditOutlined, LockOutlined, LinkOutlined, DisconnectOutlined, GoogleOutlined, FacebookFilled, PhoneOutlined } from '@ant-design/icons';
import { GoogleLogin } from '@react-oauth/google';
import { loginWithFacebookPopup } from '../../utils/facebookOAuth';
import apiClient from '../../api/apiClient';
import PasswordField from '../../components/PasswordField';

const { Text } = Typography;

const Profile: React.FC = () => {
  const [isPassModalOpen, setIsPassModalOpen] = useState(false);
  const [isEditNameOpen, setIsEditNameOpen] = useState(false);
  const [isPhoneModalOpen, setIsPhoneModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
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
    phone: null as string | null,
    hasPassword: true,
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
        oldPassword: linkedAccounts.hasPassword ? values.oldPassword : '',
        newPassword: values.newPassword
      });
      message.success('Password changed successfully!');
      setIsPassModalOpen(false);
      passForm.resetFields();
      // Refresh to update hasPassword status
      fetchLinkedAccounts();
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
            {linkedAccounts.hasPassword ? 'Change my password' : 'Set a password'}
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

      {/* Change / Set Password Modal */}
      <Modal
        title={linkedAccounts.hasPassword ? 'Change Password' : 'Set a Password'}
        open={isPassModalOpen}
        onCancel={() => setIsPassModalOpen(false)}
        onOk={handlePassModalOk}
        confirmLoading={loading}
        destroyOnHidden
      >
        <Form form={passForm} layout="vertical">
          {linkedAccounts.hasPassword && (
            <Form.Item
              name="oldPassword"
              label="Enter Your Current Password"
              rules={[{ required: true, message: 'Current password is required' }]}
            >
              <Input.Password prefix={<LockOutlined />} style={{ borderRadius: 2 }} />
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
