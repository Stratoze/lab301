import React, { useState } from 'react';
import { Layout, Button, Drawer, Menu } from 'antd';
import { MenuOutlined, MenuFoldOutlined } from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

const { Sider, Content } = Layout;

interface Props {
  children: React.ReactNode;
}

const DashboardLayout: React.FC<Props> = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const role = localStorage.getItem('role');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/auth');
  };

  const handleNavigate = (key: string) => {
    navigate(key);
    setMobileOpen(false);
  };

  const menuItems = [
    { key: '/account', icon: <></>, label: 'Account' },
    { key: '/history', icon: <></>, label: 'History & Analytics' },
    ...(role === 'ROLE_ADMIN'
      ? [
          { key: '/admin/users', icon: <></>, label: 'Manage Users' },
          { key: '/admin/tickets', icon: <></>, label: 'Manage Tickets' },
        ]
      : []),
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header>
        <Button
          className="mobile-hamburger-btn"
          type="text"
          icon={mobileOpen ? <MenuFoldOutlined /> : <MenuOutlined />}
          onClick={() => setMobileOpen(!mobileOpen)}
        />
      </Header>

      <Layout>
        <Sider
          className="desktop-sider"
          theme="light"
          width={240}
          style={{ boxShadow: '2px 0 8px rgba(0,0,0,0.03)' }}
        >
          <Sidebar
            selectedKeys={[location.pathname]}
            onNavigate={navigate}
            onLogout={handleLogout}
            role={role}
          />
        </Sider>

        <Content style={{ margin: '24px', minHeight: 280 }}>{children}</Content>
      </Layout>

      <Drawer
        title={null}
        placement="left"
        closable={true}
        onClose={() => setMobileOpen(false)}
        open={mobileOpen}
        width="100%"
        styles={{
          body: { padding: 0, display: 'flex', flexDirection: 'column', height: '100%' },
        }}
        maskStyle={{ background: 'rgba(0,0,0,0.45)' }}
      >
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => handleNavigate(key)}
          style={{ flex: 1, borderRight: 0 }}
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
            onClick={() => {
              handleLogout();
              setMobileOpen(false);
            }}
            style={{ color: '#ff4d4f', borderRadius: 8 }}
            block
          >
            Logout
          </Button>
        </div>
      </Drawer>

      <style>{`
        .mobile-hamburger-btn { display: none !important; }

        @media (max-width: 992px) {
          .desktop-sider        { display: none !important; }
          .mobile-hamburger-btn { display: inline-flex !important; }
        }

        .ant-layout-header {
          z-index: 1001 !important;
          position: sticky !important;
          top: 0 !important;
        }
      `}</style>
    </Layout>
  );
};

export default DashboardLayout;