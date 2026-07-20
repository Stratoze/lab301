import React from 'react';
import { DollarOutlined, ClockCircleOutlined } from '@ant-design/icons';
import DashboardItemCard from './DashboardItemCard';

interface HistoryTicket {
  key: number;
  number: string;
  station: string;
  isWon: boolean;
  amount: number;
  checkTime: string;
}

interface HistoryCardProps {
  ticket: HistoryTicket;
  formatCheckTime: (iso: string) => string;
}

const HistoryCard: React.FC<HistoryCardProps> = ({ ticket, formatCheckTime }) => (
  <DashboardItemCard
    title={ticket.number}
    tags={[
      { label: ticket.isWon ? 'Winner' : 'No Prize', color: ticket.isWon ? 'green' : 'red' },
      { label: `${ticket.amount?.toLocaleString()} VND`, color: ticket.amount === 0 ? 'default' : 'purple' },
    ]}
    details={[
      { icon: <DollarOutlined />, label: 'Station', value: ticket.station },
      { icon: <ClockCircleOutlined />, label: 'Searched', value: formatCheckTime(ticket.checkTime) },
    ]}
  />
);

export default HistoryCard;