import type { ReactNode } from 'react';

export interface RouteConfig {
  path: string;
  element: ReactNode;
  protected?: boolean;
  adminOnly?: boolean;
}

// Lazy imports to keep config clean
import AuthPage from '../pages/public/AuthPage';
import LotteryCheck from '../pages/public/LotteryCheck';
import Profile from '../pages/user/Profile';
import HistoryAnalytics from '../pages/user/HistoryAnalytics';
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