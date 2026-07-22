import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import HistoryAnalytics from '../HistoryAnalytics';

// Mock the hook
vi.mock('../hooks/useHistoryAnalytics', () => ({
  default: vi.fn(),
}));

import useHistoryAnalytics from '../hooks/useHistoryAnalytics';

const mockUseHistory = vi.mocked(useHistoryAnalytics);

// Mock child components to simplify assertions
vi.mock('../components/AnalyticsChart', () => ({
  default: ({ data }: { data: { name: string; spent: number; won: number }[] }) => <div data-testid="analytics-chart">{data.length} points</div>,
}));

vi.mock('../components/HistoryTable', () => ({
  default: ({ data }: { data: { key: number; number: string; station: string }[] }) => (
    <table data-testid="history-table">
      <tbody>
        {data.map((t) => (
          <tr key={t.key}>
            <td>{t.number}</td>
            <td>{t.station}</td>
          </tr>
        ))}
      </tbody>
    </table>
  ),
}));

// Mock antd CardList and HistoryCard to simplify
vi.mock('../../../components/CardList', () => ({
  default: ({ children }: { children: React.ReactNode; sortBy?: string; onSortChange?: (val: string) => void; sortOptions?: { value: string; label: string }[] }) => (
    <div data-testid="card-list">{children}</div>
  ),
}));

vi.mock('../../../components/HistoryCard', () => ({
  default: ({ ticket }: { ticket: { key: number; number: string } }) => (
    <div data-testid={`history-card-${ticket.key}`}>{ticket.number}</div>
  ),
}));

// Mock DashboardCard to just render children
vi.mock('../../../components/DashboardCard', () => ({
  default: ({ title, children }: { title: string; children: React.ReactNode }) => (
    <div>
      <h2>{title}</h2>
      <div>{children}</div>
    </div>
  ),
}));



describe('HistoryAnalytics', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default to desktop
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1024,
    });
  });

  const renderWithRouter = () =>
    render(
      <MemoryRouter>
        <HistoryAnalytics />
      </MemoryRouter>
    );

  // 5.14 History table displays rows with data
  it('displays history table with ticket rows when data is present', () => {
    mockUseHistory.mockReturnValue({
      loading: false,
      history: [
        { key: 0, date: '10/23/2023', checkTime: '2023-10-23T16:30:00Z', number: '123485', station: 'TP. H? Chi Minh', isWon: true, amount: 100000 },
        { key: 1, date: '10/23/2023', checkTime: '2023-10-23T16:30:01Z', number: '000000', station: 'TP. H? Chi Minh', isWon: false, amount: 0 },
      ],
      chartData: [{ name: '10/23/2023', spent: 30000, won: 100000 }],
      hasHistory: true,
    });

    renderWithRouter();

    // Table should be visible (desktop)
    expect(screen.getByTestId('history-table')).toBeInTheDocument();
    // Chart should be rendered
    expect(screen.getByTestId('analytics-chart')).toBeInTheDocument();
  });

  // 5.15 Empty state shown when no history
  it('shows empty state with CTA when no history exists', () => {
    mockUseHistory.mockReturnValue({
      loading: false,
      history: [],
      chartData: [],
      hasHistory: false,
    });

    renderWithRouter();

    expect(screen.getByText(/No history yet/)).toBeInTheDocument();
    expect(screen.getByText('Go to Lottery')).toBeInTheDocument();
  });
});