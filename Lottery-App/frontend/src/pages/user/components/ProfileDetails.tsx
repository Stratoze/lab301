import React from 'react';
import { Typography, Button, message } from 'antd';
import { CopyOutlined, EditOutlined } from '@ant-design/icons';
import type { UserProfile } from '../hooks/useProfile';

const { Text } = Typography;

interface ProfileDetailsProps {
  user: UserProfile;
  onEditNameClick: () => void;
}

const ProfileDetails: React.FC<ProfileDetailsProps> = ({ user, onEditNameClick }) => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Copied to clipboard');
  };

  return (
    <>
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
          <Button icon={<EditOutlined />} type="text" onClick={onEditNameClick} />
        </div>
      </div>
    </>
  );
};

export default ProfileDetails;