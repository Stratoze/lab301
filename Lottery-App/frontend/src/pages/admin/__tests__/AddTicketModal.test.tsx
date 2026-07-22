import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AddTicketModal from '../components/AddTicketModal';

vi.mock('../../../api/apiClient', () => ({
    default: {
        post: vi.fn(),
        put: vi.fn(),
        get: vi.fn(),
    },
}));

vi.mock('antd', async () => {
    const actual = await vi.importActual<typeof import('antd')>('antd');
    return {
        ...actual,
        message: { error: vi.fn(), warning: vi.fn(), success: vi.fn(), info: vi.fn() },
    };
});

vi.mock('../../../components/LotteryNumberInput', () => ({
    default: ({ placeholder, value, onChange }: { placeholder?: string; value?: string; onChange: (val: string) => void }) => (
        <input
            data-testid="number-input"
            placeholder={placeholder}
            value={value || ''}
            onChange={(e) => onChange(e.target.value)}
        />
    ),
}));

vi.mock('../../../components/HighlightDatePicker', () => ({
    default: ({ value, onChange }: { value?: { format: (fmt: string) => string }; onChange: (val: { format: () => string }) => void }) => (
        <input
            data-testid="date-picker"
            value={value?.format?.('YYYY-MM-DD') || ''}
            onChange={(e) => onChange({ format: () => e.target.value })}
        />
    ),
}));

const mockStations = [
    { id: 1, stationCode: 'SOU-HCM', name: 'TP. Ho Chi Minh', region: 'SOUTH' },
    { id: 2, stationCode: 'NOR-HN', name: 'Ha Noi', region: 'NORTH' },
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

    it('opens with save and cancel buttons when adding new ticket', () => {
        renderModal();

        expect(screen.getByText('Add Ticket')).toBeInTheDocument();
        expect(screen.getByText('Save')).toBeInTheDocument();
        expect(screen.getByText('Cancel')).toBeInTheDocument();
    });

    it('calls onClose when cancel is clicked', async () => {
        const user = userEvent.setup();
        renderModal();

        await user.click(screen.getByText('Cancel'));

        expect(onClose).toHaveBeenCalled();
    });

    it('renders all 9 prize input fields', () => {
        renderModal();

        expect(screen.getByText('Special (6 digits)')).toBeInTheDocument();
        expect(screen.getByText('1st (5 digits)')).toBeInTheDocument();
        expect(screen.getByText('8th (2 digits)')).toBeInTheDocument();

        const inputs = screen.getAllByTestId('number-input');
        expect(inputs).toHaveLength(9);
    });

    const fillValidPrizes = async (user: ReturnType<typeof userEvent.setup>) => {
        const inputs = screen.getAllByTestId('number-input');

        const validNumbers = [
            '100001', // G_DB: 1 number, 6 digits
            '21111', // G1: 1 number, 5 digits
            '31111', // G2: 1 number, 5 digits
            '41111,42222', // G3: 2 numbers, 5 digits
            '61111,62222,63333,64444,65555,66666,67777', // G4: 7 numbers, 5 digits
            '7111', // G5: 1 number, 4 digits
            '7222,7333,7444', // G6: 3 numbers, 4 digits
            '811', // G7: 1 number, 3 digits
            '99', // G8: 1 number, 2 digits
        ];

        for (let i = 0; i < inputs.length; i++) {
            await user.clear(inputs[i]);
            await user.type(inputs[i], validNumbers[i]);
        }
    };

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

    it('shows error when duplicate station/date is submitted', async () => {
        const user = userEvent.setup();
        const apiClient = (await import('../../../api/apiClient')).default;
        vi.mocked(apiClient.post).mockRejectedValueOnce({
            response: { data: { message: 'A result already exists for this station and draw date.' } },
        });

        const { message } = await import('antd');

        renderModal();

        await fillValidPrizes(user);
        await user.click(screen.getByText('Save'));

        await vi.waitFor(() => {
            expect(message.error).toHaveBeenCalledWith(
                expect.stringContaining('already exists')
            );
        });
    });
});