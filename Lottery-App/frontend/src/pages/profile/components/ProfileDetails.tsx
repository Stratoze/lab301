import React from 'react';
import { Typography, Button, Space } from 'antd';
import { CopyOutlined, EditOutlined } from '@ant-design/icons';
import { createStyles } from 'antd-style';

const { Text } = Typography;

const useStyles = createStyles(({ token, css }) => ({
  infoRow: css`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: ${token.paddingSM}px 0;
  `,
}));

interface ProfileDetailsProps {
  userCode: string;
  email: string;
  fullName: string;
  onCopy: (text: string) => void;
  onEditName: () => void;
}

const ProfileDetails: React.FC<ProfileDetailsProps> = ({
  userCode,
  email,
  fullName,
  onCopy,
  onEditName,
}) => {
  const { styles } = useStyles();

  return (
    <Space orientation="vertical" style={{ width: '100%' }} size="middle">
      <div>
        <Text type="secondary">User Code</Text>
        <div className={styles.infoRow}>
          <Text strong>{userCode}</Text>
          <Button icon={<CopyOutlined />} type="text" onClick={() => onCopy(userCode)} />
        </div>
      </div>

      <div>
        <Text type="secondary">Email</Text>
        <div className={styles.infoRow}>
          <Text strong>{email}</Text>
          <Button icon={<CopyOutlined />} type="text" onClick={() => onCopy(email)} />
        </div>
      </div>

      <div>
        <Text type="secondary">Full Name</Text>
        <div className={styles.infoRow}>
          <Text strong>{fullName}</Text>
          <Button icon={<EditOutlined />} type="text" onClick={onEditName} />
        </div>
      </div>
    </Space>
  );
};

export default ProfileDetails;