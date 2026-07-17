import React, { useState } from 'react';
import { Card, Divider, Button, Space } from 'antd';
import { useProfile } from './hooks/useProfile';
import ProfileDetails from './components/ProfileDetails';
import LinkedAccountsSection from './components/LinkedAccountsSection';
import EditNameModal from './components/EditNameModal';
import ChangePasswordModal from './components/ChangePasswordModal';
import AddPhoneModal from './components/AddPhoneModal';

const Profile: React.FC = () => {
  const {
    loading,
    user,
    linkedAccounts,
    updateName,
    updatePhone,
    unlinkPhone,
    changePassword,
    handleLinkSocial
  } = useProfile();

  const [isEditNameOpen, setIsEditNameOpen] = useState(false);
  const [isPassModalOpen, setIsPassModalOpen] = useState(false);
  const [isPhoneModalOpen, setIsPhoneModalOpen] = useState(false);

  const onUpdateNameSuccess = async (fullName: string) => {
    const success = await updateName(fullName);
    if (success) {
      setIsEditNameOpen(false);
    }
  };

  const onChangePasswordSuccess = async (values: any) => {
    const success = await changePassword(values);
    if (success) {
      setIsPassModalOpen(false);
    }
  };

  const onUpdatePhoneSuccess = async (values: { phone: string }) => {
    const success = await updatePhone(values.phone);
    if (success) {
      setIsPhoneModalOpen(false);
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <Card title="My Profile" style={{ borderRadius: 12 }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <ProfileDetails 
            user={user} 
            onEditNameClick={() => setIsEditNameOpen(true)} 
          />
          <Divider />
          <LinkedAccountsSection 
            linkedAccounts={linkedAccounts}
            onLinkSocial={handleLinkSocial}
            onUnlinkPhone={unlinkPhone}
            onAddPhoneClick={() => setIsPhoneModalOpen(true)}
          />
          <Divider />
          <Button block onClick={() => setIsPassModalOpen(true)} style={{ borderRadius: 12 }}>
            Change my password
          </Button>
        </Space>
      </Card>

      <EditNameModal 
        open={isEditNameOpen}
        fullName={user.fullName}
        loading={loading}
        onCancel={() => setIsEditNameOpen(false)}
        onUpdate={onUpdateNameSuccess}
      />

      <ChangePasswordModal 
        open={isPassModalOpen}
        loading={loading}
        onCancel={() => setIsPassModalOpen(false)}
        onChangePassword={onChangePasswordSuccess}
      />

      <AddPhoneModal 
        open={isPhoneModalOpen}
        loading={loading}
        onCancel={() => setIsPhoneModalOpen(false)}
        onAdd={onUpdatePhoneSuccess}
      />
    </div>
  );
};

export default Profile;