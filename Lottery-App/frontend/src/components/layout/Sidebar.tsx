import React from 'react';
import { Menu, Button } from 'antd';
import { 
  UserOutlined, 
  HistoryOutlined, 
  TeamOutlined, 
  FileTextOutlined, 
  MenuFoldOutlined 
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';

const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const role = localStorage.getItem('role');

  const menuItems = [
    { key: '/account', icon: <UserOutlined />, label: 'Account' },
    { key: '/history', icon: <HistoryOutlined />, label: 'History & Analytics' },
    ...(role === 'ROLE_ADMIN' ? [
      { key: '/admin/users', icon: <TeamOutlined />, label: 'Manage Users' },
      { key: '/admin/tickets', icon: <FileTextOutlined />, label: 'Manage Tickets' },
    ] : []),
  ];

  const handleLogout = () => {
    localStorage.clear();
    navigate('/auth');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Menu 
        mode="inline"
        selectedKeys={[location.pathname]} 
        items={menuItems} 
        onClick={({ key }) => navigate(key)}
        style={{ flex: 1, borderRight: 0, marginTop: 8 }}
      />
      
      <div style={{ 
        padding: '12px 16px', 
        borderTop: '1px solid #f0f0f0',
        marginTop: 'auto',
      }}>
        <Button 
          type="text" 
          icon={<MenuFoldOutlined />} 
          onClick={handleLogout}
          style={{ color: '#ff4d4f', borderRadius: 8 }}
          block
        >
          Logout
        </Button>
      </div>
    </div>
  );
};

export default Sidebar;