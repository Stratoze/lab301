import { createContext } from 'react';
import type { PasswordRules } from './PasswordRulesProvider';

const defaultRules: PasswordRules = {
  minLength: 10,
  maxLength: 64,
  blocklist: new Set(),
};

export const PasswordRulesContext = createContext<PasswordRules>(defaultRules);