import React from 'react';
import { GoogleOAuthProvider as LibGoogleOAuthProvider } from '@react-oauth/google';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';

const GoogleOAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <LibGoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
    {children}
  </LibGoogleOAuthProvider>
);

export default GoogleOAuthProvider;