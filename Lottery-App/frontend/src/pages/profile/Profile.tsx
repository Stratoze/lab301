import React from 'react';
import { Button, Space, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import DashboardCard from '../../components/DashboardCard';
import ProfileDetails from './components/ProfileDetails';
import LinkedAccountsSection from './components/LinkedAccountsSection';
import EditNameModal from './components/EditNameModal';
import ChangePasswordModal from './components/ChangePasswordModal';
import AddPhoneModal from './components/AddPhoneModal';
import useProfile from './hooks/useProfile';

const Profile: React.FC = () => {
  const {
    user,
    linkedAccounts,
    loading,
    isPassModalOpen,
    isEditNameOpen,
    isPhoneModalOpen,
    setIsPassModalOpen,
    setIsEditNameOpen,
    setIsPhoneModalOpen,
    updateName,
    changePassword,
    linkSocial,
    updatePhone,
    unlinkPhone,
  } = useProfile();

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Copied to clipboard');
  };

  const handleUpdateName = async (values: { fullName: string }) => {
    try {
      await updateName(values.fullName);
      setIsEditNameOpen(false);
    } catch (e: any) {
      message.error(e.message || 'Update failed');
    }
  };

  const handleChangePassword = async (values: {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
  }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('New passwords do not match!');
      return;
    }
    try {
      await changePassword(values.oldPassword, values.newPassword);
      setIsPassModalOpen(false);
    } catch (e: any) {
      message.error(e.message || 'Failed to change password');
    }
  };

  const handlePhoneSubmit = async (values: { phone: string }) => {
    try {
      await updatePhone(values.phone);
      setIsPhoneModalOpen(false);
    } catch (e: any) {
      message.error(e.message || 'Failed to update phone');
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <DashboardCard title="My Profile">
        <Space orientation="vertical" style={{ width: '100%' }} size="middle">
          <ProfileDetails
            userCode={user.userCode}
            email={user.email}
            fullName={user.fullName}
            onCopy={copyToClipboard}
            onEditName={() => setIsEditNameOpen(true)}
          />

          <LinkedAccountsSection
            accounts={linkedAccounts}
            onLinkGoogle={token => linkSocial('GOOGLE', token)}
            onLinkFacebook={token => linkSocial('FACEBOOK', token)}
            onAddPhone={() => setIsPhoneModalOpen(true)}
            onUnlinkPhone={unlinkPhone}
            loading={loading}
          />

          <Button
            block
            onClick={() => setIsPassModalOpen(true)}
            shape="round"
            icon={<LockOutlined />}
          >
            {linkedAccounts.hasPassword ? 'Change my password' : 'Set a password'}
          </Button>
        </Space>
      </DashboardCard>

      <EditNameModal
        open={isEditNameOpen}
        loading={loading}
        currentName={user.fullName}
        onCancel={() => setIsEditNameOpen(false)}
        onSubmit={handleUpdateName}
      />

      <ChangePasswordModal
        open={isPassModalOpen}
        loading={loading}
        hasPassword={linkedAccounts.hasPassword}
        onCancel={() => setIsPassModalOpen(false)}
        onSubmit={handleChangePassword}
      />

      <AddPhoneModal
        open={isPhoneModalOpen}
        loading={loading}
        onCancel={() => setIsPhoneModalOpen(false)}
        onSubmit={handlePhoneSubmit}
      />
    </div>
  );
};

export default Profile;