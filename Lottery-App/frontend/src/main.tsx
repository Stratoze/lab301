import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { GoogleOAuthProvider } from '@react-oauth/google'
import { ConfigProvider } from 'antd'
import { PasswordRulesProvider } from './providers/PasswordRulesProvider'
import { AuthProvider } from './contexts/AuthProvider'
import './index.css'
import App from './App.tsx'

const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || ''

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <GoogleOAuthProvider clientId={googleClientId}>
      <ConfigProvider
            theme={{
              token: {
                colorPrimary: '#1677ff',
                colorBgBase: '#f9f5f5',      // Page / layout background
                colorBgContainer: '#ffffff', // Cards, Modals, Inputs
              },
              components: {
                // Input controls: 2px radius
                Input: { borderRadius: 2 },
                Select: { borderRadius: 2 },
                DatePicker: { borderRadius: 2 },

                // Buttons and containers and tags: 12px radius
                Button: { borderRadius: 12 },
                Card: { borderRadiusLG: 12 },
                Modal: { borderRadiusLG: 12 },
                Drawer: { borderRadiusLG: 12 },
                Tag: { borderRadiusSM: 12},
              },
            }}
          >
        <PasswordRulesProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </PasswordRulesProvider>
      </ConfigProvider>
    </GoogleOAuthProvider>
  </StrictMode>,
)