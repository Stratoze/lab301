import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import dayjs from 'dayjs';
import LotteryCheck from '../LotteryCheck';

// Mock the hook completely
vi.mock('../hooks/useLotteryChecker', () => ({
  default: vi.fn(),
}));

import useLotteryChecker from '../hooks/useLotteryChecker';

const mockUseLotteryChecker = vi.mocked(useLotteryChecker);

// Mock antd message
vi.mock('antd', async () => {
  const actual = await vi.importActual<typeof import('antd')>('antd');
  return {
    ...actual,
    message: { error: vi.fn(), warning: vi.fn(), success: vi.fn() },
  };
});

/** Type-safe factory so mock shape stays in sync with the hook's return type */
const createMockReturn = (): ReturnType<typeof useLotteryChecker> => ({
  loading: false,
  stations: [],
  results: null,
  isGuest: true,
  form: { stationId: null, date: dayjs(), numbers: '' },
  setForm: vi.fn(),
  handleCheck: vi.fn(),
  setResults: vi.fn(),
  availableDates: [],
});

describe('LotteryCheck', () => {
  beforeEach(() => {
    mockUseLotteryChecker.mockReturnValue(createMockReturn());
  });

  // 5.1 Guest sees single ticket input
  it('shows single-ticket input for guest user', () => {
    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: true,
    });

    render(<LotteryCheck />);

    expect(screen.getByText('Lottery Check')).toBeInTheDocument();
    expect(screen.getByText('Check Ticket')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your 6-digit ticket number/i)).toBeInTheDocument();
  });

  // 5.2 Authenticated user gets multi-line input
  it('shows multi-line input for authenticated user', () => {
    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: false,
    });

    render(<LotteryCheck />);

    expect(screen.getByPlaceholderText(/Enter ticket numbers/i)).toBeInTheDocument();
  });

  // 5.3 Winning result displays prize name and amount
  it('displays winning result with prize details', () => {
    const checkResult = {
      details: [
        { number: '123485', isWon: true, prize: 'G8' },
      ],
      summary: { totalSpent: 10000, totalWon: 100000 },
    };

    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: false,
      results: checkResult,
    });

    render(<LotteryCheck />);

    expect(screen.getByText(/Ticket: 123485/)).toBeInTheDocument();
    expect(screen.getByText(/Congratulations!! you won the G8 prize/)).toBeInTheDocument();
    expect(screen.getByText(/Total Spent: 10,000 VND/)).toBeInTheDocument();
    expect(screen.getByText(/Total Won: 100,000 VND/)).toBeInTheDocument();
  });

  // 5.4 Losing result shows "Better luck next time"
  it('displays losing result message', () => {
    const checkResult = {
      details: [
        { number: '000000', isWon: false, prize: 'None' },
      ],
      summary: { totalSpent: 10000, totalWon: 0 },
    };

    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: true,
      results: checkResult,
    });

    render(<LotteryCheck />);

    expect(screen.getByText(/Better luck next time/)).toBeInTheDocument();
    expect(screen.getByText(/Total Won: 0 VND/)).toBeInTheDocument();
  });

  // 5.5 Share button visible for winning authenticated user
  it('shows share button when user wins and is authenticated', () => {
    const checkResult = {
      details: [
        { number: '999999', isWon: true, prize: 'G_DB' },
      ],
      summary: { totalSpent: 10000, totalWon: 2000000000 },
    };

    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: false,
      results: checkResult,
    });

    render(<LotteryCheck />);

    expect(screen.getByText('Share')).toBeInTheDocument();
  });

  // 5.6 Share button hidden for guest winner
  it('hides share button for guest user even when winning', () => {
    const checkResult = {
      details: [
        { number: '123485', isWon: true, prize: 'G8' },
      ],
      summary: { totalSpent: 10000, totalWon: 100000 },
    };

    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      isGuest: true,
      results: checkResult,
    });

    render(<LotteryCheck />);

    expect(screen.queryByText('Share')).not.toBeInTheDocument();
  });

  // 5.7 Loading state shows spinner on check button
  it('shows loading state on check button while checking', () => {
    mockUseLotteryChecker.mockReturnValue({
      ...createMockReturn(),
      loading: true,
    });

    render(<LotteryCheck />);

    const button = screen.getByRole('button', { name: /Check Ticket/i });
    expect(button).toHaveClass('ant-btn-loading');
  });
});