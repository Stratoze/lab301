import React, { useState } from 'react';
import { Layout, Button, Drawer, Menu } from 'antd';
import { MenuOutlined, CloseOutlined } from '@ant-design/icons';
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

  const menuItems = [
    { key: '/account', icon: <span />, label: 'Account' },
    { key: '/history', icon: <span />, label: 'History & Analytics' },
    ...(role === 'ROLE_ADMIN'
      ? [
          { key: '/admin/users', icon: <span />, label: 'Manage Users' },
          { key: '/admin/tickets', icon: <span />, label: 'Manage Tickets' },
        ]
      : []),
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <div style={{ zIndex: 1001, position: 'relative' }}>
        <Header>
          <Button
            className="mobile-hamburger-btn"
            type="text"
            icon={mobileOpen ? <CloseOutlined style={{ fontSize: '20px' }} /> : <MenuOutlined style={{ fontSize: '20px' }} />}
            onClick={() => setMobileOpen(!mobileOpen)}
          />
        </Header>
      </div>

      <Layout 
        id="sub-layout-container" 
        style={{ flex: 1, overflow: 'hidden', position: 'relative', zIndex: 1 }}
      >
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
            onNavigate={navigate}
            onLogout={handleLogout}
            role={role}
          />
        </Sider>

        <Content style={{ margin: '24px', overflowY: 'auto', height: '100%' }}>
          {children}
        </Content>

        <Drawer
          title={null}
          placement="left"
          closable={false}
          mask={false}
          onClose={() => setMobileOpen(false)}
          open={mobileOpen}
          getContainer={() => document.getElementById('sub-layout-container') || document.body}
          rootStyle={{ position: 'absolute', zIndex: 1000, width: '100%' }}
          styles={{
            body: { padding: 0, display: 'flex', flexDirection: 'column', height: '100%' },
          }}
        >
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => {
              navigate(key);
              setMobileOpen(false);
            }}
            /* Added paddingTop: '64px' here to offset the header height */
            style={{ flex: 1, borderRight: 0, paddingTop: '64px' }}
          />
          <div
            style={{
              padding: '12px 16px',
              borderTop: '1px solid #f0f0f0',
            }}
          >
            <Button
              type="text"
              onClick={() => {
                handleLogout();
                setMobileOpen(false);
              }}
              style={{ color: '#ff4d4f', borderRadius: 12, textAlign: 'left' }}
              block
            >
              Logout
            </Button>
          </div>
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