import React from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: React.ReactNode;
  adminOnly?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, adminOnly = false }) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (!token) return <Navigate to="/auth" replace />;
  if (adminOnly && role !== 'ROLE_ADMIN') return <Navigate to="/lottery" replace />;

  return <>{children}</>;
};

export default ProtectedRoute;