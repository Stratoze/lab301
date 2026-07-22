import { useContext } from 'react';
import { PasswordRulesContext } from './PasswordRulesContext';

export const usePasswordRulesContext = () => useContext(PasswordRulesContext);