import React, { useEffect, useState } from 'react';
import apiClient from '../api/apiClient';
import { PasswordRulesContext } from './PasswordRulesContext';

export interface PasswordRules {
  minLength: number;
  maxLength: number;
  blocklist: Set<string>;
}

const defaultRules: PasswordRules = {
  minLength: 10,
  maxLength: 64,
  blocklist: new Set(),
};

const PasswordRulesProviderComponent: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [rules, setRules] = useState<PasswordRules>(defaultRules);

  useEffect(() => {
    apiClient
      .get('/auth/password-rules')
      .then((res) => {
        const data = res.data.data;
        setRules({
          minLength: data.minLength,
          maxLength: data.maxLength,
          blocklist: new Set(data.blocklist),
        });
      })
      .catch(() => {
        // keep defaults
      });
  }, []);

  return <PasswordRulesContext.Provider value={rules}>{children}</PasswordRulesContext.Provider>;
};

export const PasswordRulesProvider = PasswordRulesProviderComponent;