import React, { useState } from 'react';
import { Layout, Button, Drawer } from 'antd';
import { MenuOutlined, CloseOutlined } from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthContext } from '../../contexts/useAuthContext';
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
  const { user, logout } = useAuthContext();
  const role = user?.role ?? null;

  const handleLogout = () => {
    logout();
    navigate('/auth');
  };

  const handleNavigate = (path: string) => {
    navigate(path);
    setMobileOpen(false); // Close mobile drawer on navigation
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      {/* Header Bar */}
      <div style={{ zIndex: 1001, position: 'relative' }}>
        <Header>
          <Button
            className="mobile-hamburger-btn"
            type="text"
            icon={
              mobileOpen ? (
                <CloseOutlined style={{ fontSize: '20px' }} />
              ) : (
                <MenuOutlined style={{ fontSize: '20px' }} />
              )
            }
            onClick={() => setMobileOpen(!mobileOpen)}
          />
        </Header>
      </div>

      {/* Main Body Layout */}
      <Layout
        id="sub-layout-container"
        style={{ flex: 1, overflow: 'hidden', position: 'relative', zIndex: 1 }}
      >
        {/* Desktop Sidebar */}
        <Sider
          className="desktop-sider"
          theme="light"
          width={240}
          style={{
            boxShadow: '2px 0 8px rgba(0,0,0,0.03)',
            overflowY: 'auto',
            height: '100%',
          }}
        >
          <Sidebar
            selectedKeys={[location.pathname]}
            onNavigate={handleNavigate}
            onLogout={handleLogout}
            role={role}
          />
        </Sider>

        {/* Page Content */}
        <Content style={{ margin: '24px', overflowY: 'auto', height: '100%' }}>
          {children}
        </Content>

        {/* Mobile Drawer - Reuses <Sidebar /> */}
        <Drawer
          placement="left"
          closable={false}
          mask={true}
          onClose={() => setMobileOpen(false)}
          open={mobileOpen}
          getContainer={() =>
            document.getElementById('sub-layout-container') || document.body
          }
          rootStyle={{ position: 'absolute', zIndex: 1000, width: '100%' }}
          styles={{
            body: { padding: 0, height: '100%' },
          }}
        >
          <Sidebar
            selectedKeys={[location.pathname]}
            onNavigate={handleNavigate}
            onLogout={() => {
              handleLogout();
              setMobileOpen(false);
            }}
            role={role}
          />
        </Drawer>
      </Layout>

      <style>{`
        .mobile-hamburger-btn { display: none !important; }

        @media (max-width: 992px) {
          .desktop-sider        { display: none !important; }
          .mobile-hamburger-btn { display: inline-flex !important; }
        }
      `}</style>
    </div>
  );
};

export default DashboardLayout;