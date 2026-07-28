import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthContext } from '../contexts/useAuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  adminOnly?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, adminOnly = false }) => {
  const { isAuthenticated, isAdmin } = useAuthContext();

  if (!isAuthenticated) return <Navigate to="/auth" replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/auth" replace />;
  return <>{children}</>;
};

export default ProtectedRoute;