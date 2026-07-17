import React from 'react';
import { Layout, Typography } from 'antd';
import { MoneyCollectOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Header: AntHeader } = Layout;
const { Text } = Typography;

interface HeaderProps {
  children?: React.ReactNode;
}

const Header: React.FC<HeaderProps> = ({ children }) => {
  const navigate = useNavigate();

  return (
    <AntHeader
      style={{
        background: '#fff',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '12px 24px',
        height: 'auto',
        lineHeight: 'normal',
        boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
      }}
    >
      <div
        style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }}
        onClick={() => navigate('/lottery')}
      >
        <MoneyCollectOutlined style={{ fontSize: 24, color: '#1677ff' }} />
        <Text strong style={{ fontSize: 16 }}>
          Lottery System
        </Text>
      </div>
      {children}
    </AntHeader>
  );
};

export default Header;