import React from 'react';
import { Button } from 'antd';
import { EditOutlined, EyeOutlined, CalendarOutlined } from '@ant-design/icons';
import DashboardItemCard from './DashboardItemCard';

interface Ticket {
  id: number;
  resultCode: string;
  stationName: string;
  drawDate: string;
  status: string;
  totalQueries: number;
}

interface TicketCardProps {
  ticket: Ticket;
  onEdit?: (ticket: Ticket) => void;
}

const TicketCard: React.FC<TicketCardProps> = ({ ticket, onEdit }) => (
  <DashboardItemCard
    title={ticket.resultCode}
    tags={[
      { label: ticket.status, color: ticket.status === 'PUBLISH' ? 'green' : 'default' },
      { label: `${(ticket.totalQueries ?? 0).toLocaleString()} Views`, color: 'blue', icon: <EyeOutlined /> },
    ]}
    details={[
      { icon: <CalendarOutlined />, label: 'Date Added', value: ticket.drawDate },
      { icon: <span />, label: 'Station', value: ticket.stationName },
    ]}
    actions={
      onEdit
        ? [<Button type="link" icon={<EditOutlined />} key="edit" onClick={() => onEdit(ticket)}>Edit</Button>]
        : undefined
    }
  />
);

export default TicketCard;