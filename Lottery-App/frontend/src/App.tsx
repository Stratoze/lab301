import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/public/AuthPage';
import ManageUsers from './pages/admin/ManageUsers';
import ManageTickets from './pages/admin/ManageTickets';
import LotteryCheck from './pages/public/LotteryCheck';
import HistoryAnalytics from './pages/user/HistoryAnalytics';
import Profile from './pages/user/Profile';
import DashboardLayout from './components/layout/DashboardLayout';
import Navbar from './components/layout/Navbar';
import './App.css';

const ProtectedRoute = ({ children, adminOnly = false }: { children: React.ReactNode, adminOnly?: boolean }) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (!token) return <Navigate to="/auth" replace />;
  if (adminOnly && role !== 'ROLE_ADMIN') return <Navigate to="/lottery" replace />;

  return <>{children}</>;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/auth" element={<><Navbar /><AuthPage /></>} />
        <Route path="/lottery" element={<><Navbar /><LotteryCheck /></>} />

        {/* User/Admin Private Routes - ALL guarded */}
        <Route path="/account" element={
          <ProtectedRoute>
            <DashboardLayout><Profile /></DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/history" element={
          <ProtectedRoute>
            <DashboardLayout><HistoryAnalytics /></DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/admin/users" element={
          <ProtectedRoute adminOnly={true}>
            <DashboardLayout><ManageUsers /></DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/admin/tickets" element={
          <ProtectedRoute adminOnly={true}>
            <DashboardLayout><ManageTickets /></DashboardLayout>
          </ProtectedRoute>
        } />

        <Route path="/" element={<Navigate to="/lottery" />} />
      </Routes>
    </Router>
  );
}

export default App;
