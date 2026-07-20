import React, { useState } from 'react';
import { Card, Tabs } from 'antd';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import ResetModal from './components/ResetModal';
import useAuth from './hooks/useAuth';

const AuthPage: React.FC = () => {
  const { loading, login, register, socialLogin, facebookLogin, forgotPassword } = useAuth();
  const [activeTab, setActiveTab] = useState('login');
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);

  const onRegisterSuccess = () => setActiveTab('login');

  return (
    <>
      <div
        className="auth-container"
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          background: '#f0f2f5',
          padding: '0 10px',
        }}
      >
        <Card
          className="auth-card"
          style={{
            width: '100%',
            maxWidth: 400,
            borderRadius: 12,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
          }}
        >
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            tabBarStyle={{ marginBottom: 24 }}
            items={[
              {
                key: 'login',
                label: 'Sign In',
                children: (
                  <LoginForm
                    loading={loading}
                    onLogin={login}
                    onSocialLogin={socialLogin}
                    onFacebookLogin={facebookLogin}
                    onForgotPassword={() => setIsResetModalOpen(true)}
                  />
                ),
              },
              {
                key: 'register',
                label: 'Sign Up',
                children: (
                  <RegisterForm
                    loading={loading}
                    onRegister={register}
                    onSwitchToLogin={onRegisterSuccess}
                    onSocialLogin={socialLogin}
                    onFacebookLogin={facebookLogin}
                  />
                ),
              },
            ]}
          />
        </Card>

        <ResetModal
          open={isResetModalOpen}
          onClose={() => setIsResetModalOpen(false)}
          onReset={forgotPassword}
        />
      </div>

      <style>{`
        @media (max-width: 768px) {
          .auth-container {
            padding: 0 10px !important;
          }
          .auth-card {
            margin: 0 !important;
            border-radius: 12px !important;
          }
        }

        .auth-card .ant-tabs-nav-list {
          width: 100%;
        }
        .auth-card .ant-tabs-tab {
          flex: 1;
          justify-content: center;
          margin: 0 !important;
        }
        .auth-card .ant-tabs-tab + .ant-tabs-tab {
          margin-left: 0 !important;
        }
      `}</style>
    </>
  );
};

export default AuthPage;