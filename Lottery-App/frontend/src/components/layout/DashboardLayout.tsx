import React, { useState } from 'react';
import { Layout, Menu, Button, Drawer, Typography } from 'antd';
import {
UserOutlined,
HistoryOutlined,
TeamOutlined,
FileTextOutlined,
MenuOutlined,
MenuFoldOutlined,
MoneyCollectOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
const { Header, Sider, Content } = Layout;
const { Text } = Typography;
interface Props {
children: React.ReactNode;
}
const DashboardLayout: React.FC<Props> = ({ children }) => {
const [mobileOpen, setMobileOpen] = useState(false);
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
const sidebarContent = (
  <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
    {/* Menu - no redundant logo (header already has it) */}
    <Menu
      mode="inline"
      selectedKeys={[location.pathname]}
      items={menuItems}
      onClick={({ key }) => navigate(key)}
      style={{ flex: 1, borderRight: 0, marginTop: 8 }}
    />
    {/* Logout pinned to bottom */}
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
return (
<Layout style={{ minHeight: '100vh' }}>
{/* ===== TOP HEADER BAR ===== */}
<Header style={{
background: '#fff',
display: 'flex',
alignItems: 'center',
justifyContent: 'space-between',
padding: '12px 24px',
height: 'auto',
lineHeight: 'normal',
boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
zIndex: 1001,
position: 'sticky',
top: 0
}}>
{/* Left: Logo (click lottery) */}
<div
style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }}
onClick={() => navigate('/lottery')}
>
<MoneyCollectOutlined style={{ fontSize: 24, color: '#1677ff' }} />
<Text strong style={{ fontSize: 16 }}>Lottery System</Text>
</div>
{/* Right: hamburger toggle (mobile only) */}
    <Button
      className="mobile-hamburger-btn"
      type="text"
      icon={mobileOpen ? <MenuFoldOutlined /> : <MenuOutlined />}
      onClick={() => setMobileOpen(!mobileOpen)}
    />
  </Header>

  {/* ===== BODY: Sider + Content ===== */}
  <Layout>
    {/* Desktop sidebar */}
    <Sider 
      className="desktop-sider"
      theme="light" 
      width={240}
      style={{ 
        boxShadow: '2px 0 8px rgba(0,0,0,0.03)',
      }}
    >
      {sidebarContent}
    </Sider>

    <Content style={{ margin: '24px', minHeight: 280 }}>
      {children}
    </Content>
  </Layout>

  {/* ===== Mobile Drawer (full screen, below header) ===== */}
  <Drawer
    title={null}
    placement="left"
    closable={true}
    onClose={() => setMobileOpen(false)}
    open={mobileOpen}
    width="100%"
    styles={{ 
      body: { padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }
    }}
    maskStyle={{ background: 'rgba(0,0,0,0.45)' }}
  >
    <Menu 
      mode="inline"
      selectedKeys={[location.pathname]} 
      items={menuItems} 
      onClick={({ key }) => {
        navigate(key);
        setMobileOpen(false);
      }}
      style={{ flex: 1, borderRight: 0 }}
    />

    <div style={{ 
      padding: '12px 16px', 
      borderTop: '1px solid #f0f0f0',
      marginTop: 'auto',
    }}>
      <Button 
        type="text" 
        icon={<MenuFoldOutlined />} 
        onClick={() => { handleLogout(); setMobileOpen(false); }}
        style={{ color: '#ff4d4f', borderRadius: 8 }}
        block
      >
        Logout
      </Button>
    </div>
  </Drawer>

  <style>{`
    /* Desktop: hide hamburger */
    .mobile-hamburger-btn { display: none !important; }

    @media (max-width: 992px) {
      .desktop-sider        { display: none !important; }
      .mobile-hamburger-btn { display: inline-flex !important; }
    }

    /* Keep header above everything */
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