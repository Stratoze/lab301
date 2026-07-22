import React from 'react';
import { Button, Space } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import Header from './Header';

const PublicHeader: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const token = localStorage.getItem('token');
  const isAuthPage = location.pathname === '/auth';

  return (
    <Header>
      {!isAuthPage && (
        <Space>
          {!token ? (
            <Button type="primary" onClick={() => navigate('/auth')}>
              Sign In
            </Button>
          ) : (
            <Button onClick={() => navigate('/history')}>
              Dashboard
            </Button>
          )}
        </Space>
      )}
    </Header>
  );
};

export default PublicHeader;