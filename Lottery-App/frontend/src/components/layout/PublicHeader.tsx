import React from 'react';
import { Button, Space } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthContext } from '../../contexts/useAuthContext';
import Header from './Header';

const PublicHeader: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuthContext();
  const isAuthPage = location.pathname === '/auth';

  return (
    <Header>
      {!isAuthPage && (
        <Space>
          {!isAuthenticated ? (
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