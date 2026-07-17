import React from 'react';
import { Card, Table, Tag } from 'antd';
import type { TicketRecord } from '../hooks/useHistoryAnalytics';

interface HistoryTableProps {
  data: TicketRecord[];
}

const HistoryTable: React.FC<HistoryTableProps> = ({ data }) => {
  return (
    <Card title="Detailed History" style={{ borderRadius: 12 }}>
      <Table 
        pagination={{ pageSize: 5 }}
        dataSource={data}
        columns={[
          { title: 'Date', dataIndex: 'date' },
          { title: 'Ticket', dataIndex: 'number' },
          { title: 'Station', dataIndex: 'station' },
          { 
            title: 'Result', 
            dataIndex: 'isWon', 
            render: (w: boolean) => (
              <Tag color={!w ? 'red' : 'green'}>{w ? 'Winner' : 'No Prize'}</Tag>
            ) 
          },
          { 
            title: 'Amount', 
            dataIndex: 'amount', 
            render: (a: number) => `${a?.toLocaleString()} VND` 
          },
        ]}
      />
    </Card>
  );
};

export default HistoryTable;