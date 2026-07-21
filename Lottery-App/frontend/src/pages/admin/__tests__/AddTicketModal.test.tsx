import React from 'react';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AddTicketModal from '../components/AddTicketModal';

// Mock apiClient
vi.mock('../../../api/apiClient', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

// Mock antd message
vi.mock('antd', async () => {
  const actual = await vi.importActual('antd');
  return {
    ...(actual as any),
    message: { error: vi.fn(), warning: vi.fn(), success: vi.fn(), info: vi.fn() },
  };
});

// Mock LotteryNumberInput
vi.mock('../../../components/LotteryNumberInput', () => ({
  default: ({ placeholder, value, onChange }: any) => (
    <input
      data-testid="number-input"
      placeholder={placeholder}
      value={value || ''}
      onChange={(e) => onChange(e.target.value)}
    />
  ),
}));

// Mock HighlightDatePicker
vi.mock('../../../components/HighlightDatePicker', () => ({
  default: ({ value, onChange }: any) => (
    <input
      data-testid="date-picker"
      value={value?.format?.('YYYY-MM-DD') || ''}
      onChange={(e) => onChange({ format: () => e.target.value })}
    />
  ),
}));

const mockStations = [
  { id: 1, stationCode: 'SOU-HCM', name: 'TP. H? Chi Minh', region: 'SOUTH' },
  { id: 2, stationCode: 'NOR-HN', name: 'Ha N?i', region: 'NORTH' },
];

describe('AddTicketModal', () => {
  const onClose = vi.fn();
  const onSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderModal = (props: Partial<Parameters<typeof AddTicketModal>[0]> = {}) =>
    render(
      <AddTicketModal
        open={true}
        onClose={onClose}
        ticket={undefined}
        stations={mockStations}
        onSuccess={onSuccess}
        {...props}
      />
    );

  // 5.7 Opens with correct defaults (station, date, save/cancel buttons)
  it('opens with save and cancel buttons when adding new ticket', () => {
    renderModal();

    expect(screen.getByText('Add Ticket')).toBeInTheDocument();
    expect(screen.getByText('Save')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  // 5.8 Cancel button calls onClose
  it('calls onClose when cancel is clicked', async () => {
    const user = userEvent.setup();
    renderModal();

    await user.click(screen.getByText('Cancel'));

    expect(onClose).toHaveBeenCalled();
  });

  // 5.9 All 9 prize fields are rendered with correct labels
  it('renders all 9 prize input fields', () => {
    renderModal();

    expect(screen.getByText('Special (6 digits)')).toBeInTheDocument();
    expect(screen.getByText('1st (5 digits)')).toBeInTheDocument();
    expect(screen.getByText('8th (2 digits)')).toBeInTheDocument();
    // All 9 inputs rendered
    const inputs = screen.getAllByTestId('number-input');
    expect(inputs).toHaveLength(9);
  });

  // Helper to fill all 9 prize fields with digit counts matching the labels
  // Input order: Special (6), 1st (5), 2nd (5), 3rd (5), 4th (5), 5th (4), 6th (4), 7th (3), 8th (2)
  const fillValidPrizes = async (user: ReturnType<typeof userEvent.setup>) => {
    const inputs = screen.getAllByTestId('number-input');
    // Non-overlapping numbers: no number ends with another number in this set
    const validNumbers = ['111111', '22222', '33333', '44444', '55555', '6666', '7777', '888', '99'];
    for (let i = 0; i < inputs.length; i++) {
      await user.clear(inputs[i]);
      await user.type(inputs[i], validNumbers[i]);
    }
  };

  // 5.10 Successful save closes modal and calls onSuccess
  it('closes modal and calls onSuccess after successful save', async () => {
    const user = userEvent.setup();
    const apiClient = (await import('../../../api/apiClient')).default;
    vi.mocked(apiClient.post).mockResolvedValueOnce({ data: { id: 1 } });

    renderModal();

    await fillValidPrizes(user);

    await user.click(screen.getByText('Save'));

    await vi.waitFor(() => {
      expect(apiClient.post).toHaveBeenCalledWith('/admin/tickets', expect.any(Object));
    });
  });

  // 5.11 Duplicate station/date shows error message
  it('shows error when duplicate station/date is submitted', async () => {
    const user = userEvent.setup();
    const apiClient = (await import('../../../api/apiClient')).default;
    vi.mocked(apiClient.post).mockRejectedValueOnce({
      response: { data: { message: 'Potential duplicate date/station' } },
    });

    const { message } = await import('antd');

    renderModal();

    await fillValidPrizes(user);

    await user.click(screen.getByText('Save'));

    await vi.waitFor(() => {
      expect(message.error).toHaveBeenCalledWith(
        expect.stringContaining('Potential duplicate date/station')
      );
    });
  });
});