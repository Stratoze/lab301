import React from 'react';
import { MailOutlined, ClockCircleOutlined, UserOutlined } from '@ant-design/icons';
import DashboardItemCard from './DashboardItemCard';
import dayjs from 'dayjs';

interface User {
  id: number;
  fullName: string;
  email: string;
  userCode: string;
  role: string;
  isActive: boolean;
  lastLogin?: string;
}

interface UserCardProps {
  user: User;
  selected?: boolean;
  onClick?: () => void;
  actions?: React.ReactNode[];
}

const UserCard: React.FC<UserCardProps> = ({ user, selected, onClick, actions }) => (
  <DashboardItemCard
    title={user.fullName}
    tags={[
      { label: user.isActive ? 'Active' : 'BLOCKED', color: user.isActive ? 'success' : 'error' },
      { label: user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER', color: user.role === 'ROLE_ADMIN' ? 'purple' : 'processing' },
    ]}
    details={[
      { icon: <MailOutlined />, label: 'Email', value: user.email },
      {
        icon: <ClockCircleOutlined />,
        label: 'Last Login',
        value: user.lastLogin
            ? dayjs(user.lastLogin).format('DD/MM/YYYY')
            : 'N/A',
      },
      { icon: <UserOutlined />, label: 'User Code', value: user.userCode },
    ]}
    actions={actions}
    onClick={onClick}
    selected={selected}
  />
);

export default UserCard;