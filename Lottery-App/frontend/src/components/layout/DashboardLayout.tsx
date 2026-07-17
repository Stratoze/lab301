import React, { useState } from 'react';
import { Layout, Drawer } from 'antd';
import Header from './Header';
import Sidebar from './Sidebar';

const { Sider, Content } = Layout;

interface Props {
  children: React.ReactNode;
}

const DashboardLayout: React.FC<Props> = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header 
        showToggle={true} 
        isToggled={mobileOpen} 
        onToggle={() => setMobileOpen(!mobileOpen)} 
      />

      <Layout>
        <Sider 
          className="desktop-sider"
          theme="light" 
          width={240}
          style={{ boxShadow: '2px 0 8px rgba(0,0,0,0.03)' }}
        >
          <Sidebar />
        </Sider>

        <Content style={{ margin: '24px', minHeight: 280 }}>
          {children}
        </Content>
      </Layout>

      <Drawer
        title={null}
        placement="left"
        closable={true}
        onClose={() => setMobileOpen(false)}
        open={mobileOpen}
        width="100%"
        styles={{ body: { padding: 0 } }}
      >
        <Sidebar />
      </Drawer>

      <style>{`
        .mobile-hamburger-btn { display: none !important; }
        @media (max-width: 992px) {
          .desktop-sider { display: none !important; }
          .mobile-hamburger-btn { display: inline-flex !important; }
        }
      `}</style>
    </Layout>
  );
};

export default DashboardLayout;