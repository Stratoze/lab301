import type { ReactNode } from 'react';

export interface RouteConfig {
  path: string;
  element: ReactNode;
  protected?: boolean;
  adminOnly?: boolean;
}

// Lazy imports to keep config clean
import AuthPage from '../pages/auth/AuthPage';
import LotteryCheck from '../pages/lottery/LotteryCheck';
import Profile from '../pages/profile/Profile';
import HistoryAnalytics from '../pages/history/HistoryAnalytics';
import ManageUsers from '../pages/admin/ManageUsers';
import ManageTickets from '../pages/admin/ManageTickets';
import DashboardLayout from '../components/layout/DashboardLayout';
import PublicHeader from '../components/layout/PublicHeader';

const routeConfig: RouteConfig[] = [
  {
    path: '/auth',
    element: <><PublicHeader /><AuthPage /></>,
  },
  {
    path: '/lottery',
    element: <><PublicHeader /><LotteryCheck /></>,
  },
  {
    path: '/account',
    element: <DashboardLayout><Profile /></DashboardLayout>,
    protected: true,
  },
  {
    path: '/history',
    element: <DashboardLayout><HistoryAnalytics /></DashboardLayout>,
    protected: true,
  },
  {
    path: '/admin/users',
    element: <DashboardLayout><ManageUsers /></DashboardLayout>,
    protected: true,
    adminOnly: true,
  },
  {
    path: '/admin/tickets',
    element: <DashboardLayout><ManageTickets /></DashboardLayout>,
    protected: true,
    adminOnly: true,
  },
];

export default routeConfig;