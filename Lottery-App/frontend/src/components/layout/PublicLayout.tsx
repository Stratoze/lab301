import React from 'react';
import { Layout } from 'antd';
import Header from './Header';

const { Content } = Layout;

interface Props {
  children: React.ReactNode;
}

const PublicLayout: React.FC<Props> = ({ children }) => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header showSignIn={true} />
      <Content>
        {children}
      </Content>
    </Layout>
  );
};

export default PublicLayout;