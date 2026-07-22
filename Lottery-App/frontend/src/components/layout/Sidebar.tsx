import React from 'react';
import { Menu, Button } from 'antd';
import {
  UserOutlined,
  HistoryOutlined,
  TeamOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
} from '@ant-design/icons';

interface SidebarProps {
  selectedKeys: string[];
  onNavigate: (key: string) => void;
  onLogout: () => void;
  role: string | null;
}

const Sidebar: React.FC<SidebarProps> = ({ selectedKeys, onNavigate, onLogout, role }) => {
  const menuItems = [
    { key: '/account', icon: <UserOutlined />, label: 'Account' },
    { key: '/history', icon: <HistoryOutlined />, label: 'History & Analytics' },
    ...(role === 'ROLE_ADMIN'
      ? [
          { key: '/admin/users', icon: <TeamOutlined />, label: 'Manage Users' },
          { key: '/admin/tickets', icon: <FileTextOutlined />, label: 'Manage Tickets' },
        ]
      : []),
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Menu
        mode="inline"
        selectedKeys={selectedKeys}
        items={menuItems}
        onClick={({ key }) => onNavigate(key)}
        style={{ flex: 1, borderRight: 0, marginTop: 8 }}
      />
      <div
        style={{
          padding: '12px 16px',
          borderTop: '1px solid #f0f0f0',
        }}
      >
        <Button
          type="text"
          icon={<MenuFoldOutlined />}
          onClick={onLogout}
          block
        >
          Logout
        </Button>
      </div>
    </div>
  );
};

export default Sidebar;