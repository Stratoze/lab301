import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { GoogleOAuthProvider } from '@react-oauth/google'
import { ConfigProvider } from 'antd'
import { PasswordRulesProvider } from './providers/PasswordRulesProvider'
import './index.css'
import App from './App.tsx'

const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || ''

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <GoogleOAuthProvider clientId={googleClientId}>
      <ConfigProvider
            theme={{
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
                Tag: { borderRadius: 12},
              },
            }}
          >
        <PasswordRulesProvider>
          <App />
        </PasswordRulesProvider>
      </ConfigProvider>
    </GoogleOAuthProvider>
  </StrictMode>,
)