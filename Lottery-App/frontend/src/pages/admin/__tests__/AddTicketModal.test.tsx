import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
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
  it('calls onClose when cancel is clicked', () => {
    renderModal();

    fireEvent.click(screen.getByText('Cancel'));

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

  // 5.10 Successful save closes modal and calls onSuccess
  it('closes modal and calls onSuccess after successful save', async () => {
    const apiClient = (await import('../../../api/apiClient')).default;
    vi.mocked(apiClient.post).mockResolvedValueOnce({ data: { id: 1 } });

    renderModal();

    // Fill required fields: station (already defaulted), date, and all 9 prize inputs
    const inputs = screen.getAllByTestId('number-input');
    for (const input of inputs) {
      fireEvent.change(input, { target: { value: '123' } });
    }

    fireEvent.click(screen.getByText('Save'));

    await vi.waitFor(() => {
      expect(apiClient.post).toHaveBeenCalledWith('/admin/tickets', expect.any(Object));
    });
  });

  // 5.11 Duplicate station/date shows error message
  it('shows error when duplicate station/date is submitted', async () => {
    const apiClient = (await import('../../../api/apiClient')).default;
    vi.mocked(apiClient.post).mockRejectedValueOnce({
      response: { data: { message: 'Potential duplicate date/station' } },
    });

    const { message } = await import('antd');

    renderModal();

    const inputs = screen.getAllByTestId('number-input');
    for (const input of inputs) {
      fireEvent.change(input, { target: { value: '12' } });
    }

    fireEvent.click(screen.getByText('Save'));

    await vi.waitFor(() => {
      expect(message.error).toHaveBeenCalledWith(
        expect.stringContaining('Potential duplicate date/station')
      );
    });
  });
});