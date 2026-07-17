export interface PasswordStrengthResult {
  percent: number;
  status: 'exception' | 'active' | 'success';
  label: string;
}

export const getPasswordStrength = (password: string): PasswordStrengthResult => {
  if (!password) return { percent: 0, status: 'exception', label: '' };
  if (password.length < 10) {
    return { percent: Math.min((password.length / 10) * 30, 30), status: 'exception', label: 'Too short (min 10 characters)' };
  }
  let score = 40;
  if (password.length >= 12) score += 10;
  if (password.length >= 16) score += 10;
  if (/[A-Z]/.test(password)) score += 15;
  if (/[a-z]/.test(password)) score += 5;
  if (/[0-9]/.test(password)) score += 10;
  if (/[^A-Za-z0-9]/.test(password)) score += 10;
  const percent = Math.min(score, 100);
  if (percent >= 80) return { percent, status: 'success', label: 'Strong' };
  if (percent >= 50) return { percent, status: 'active', label: 'Medium' };
  return { percent, status: 'exception', label: 'Weak' };
};