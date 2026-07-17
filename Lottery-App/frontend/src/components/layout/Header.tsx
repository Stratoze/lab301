import React from 'react';
import { Layout, Button, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { MoneyCollectOutlined, MenuOutlined, MenuFoldOutlined } from '@ant-design/icons';

const { Header: AntHeader } = Layout;
const { Title } = Typography;

interface HeaderProps {
  showSignIn?: boolean;
  showToggle?: boolean;
  onToggle?: () => void;
  isToggled?: boolean;
}

const Header: React.FC<HeaderProps> = ({ 
  showSignIn = false, 
  showToggle = false, 
  onToggle, 
  isToggled 
}) => {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  return (
    <AntHeader style={{ 
      background: '#fff', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'space-between',
      padding: '12px 24px',
      height: '64px',
      lineHeight: 'normal',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
      position: 'sticky',
      top: 0,
      zIndex: 1001
    }}>
      <div 
        style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }} 
        onClick={() => navigate('/')}
      >
        <MoneyCollectOutlined style={{ fontSize: 28, color: '#1677ff' }} />
        <Title level={4} style={{ margin: 0, whiteSpace: 'nowrap' }}>Lottery System</Title>
      </div>
      
      <Space>
        {showSignIn && (
          !token ? (
            <Button type="primary" onClick={() => navigate('/auth')} style={{ borderRadius: 12 }}>
              Sign In
            </Button>
          ) : (
            <Button onClick={() => navigate('/history')} style={{ borderRadius: 12 }}>
              Dashboard
            </Button>
          )
        )}
        
        {showToggle && (
          <Button
            className="mobile-hamburger-btn"
            type="text"
            icon={isToggled ? <MenuFoldOutlined /> : <MenuOutlined />}
            onClick={onToggle}
            style={{ display: 'none' }}
          />
        )}
      </Space>
    </AntHeader>
  );
};

export default Header;