import React from 'react';
import { Layout, Button, Space, Typography } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { MoneyCollectOutlined } from '@ant-design/icons';

const { Header } = Layout;
const { Title } = Typography;

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const token = localStorage.getItem('token');
  const isAuthPage = location.pathname === '/auth';

  return (
    <Header style={{ 
      background: '#fff', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'space-between',
      padding: '12px 32px',
      height: 'auto',
      lineHeight: 'normal',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
      position: 'sticky',
      top: 0,
      zIndex: 1000
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }} onClick={() => navigate('/')}>
        <MoneyCollectOutlined style={{ fontSize: 28, color: '#1677ff' }} />
        <Title level={4} style={{ margin: 0 }}>Lottery System</Title>
      </div>
      
      {/* Hide sign-in button on auth page */}
      {!isAuthPage && (
        <Space>
          {!token ? (
            <Button type="primary" onClick={() => navigate('/auth')} style={{ borderRadius: 12 }}>
              Sign In
            </Button>
          ) : (
            <Button onClick={() => navigate('/history')} style={{ borderRadius: 12 }}>
              Dashboard
            </Button>
          )}
        </Space>
      )}
    </Header>
  );
};

export default Navbar;