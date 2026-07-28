import React from 'react';
import { Table, Tag } from 'antd';

interface Ticket {
  key: number;
  date: string;
  number: string;
  station: string;
  isWon: boolean;
  prize: string;
  amount: number;
}

interface HistoryTableProps {
  data: Ticket[];
}

const HistoryTable: React.FC<HistoryTableProps> = ({ data }) => {
  return (
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
          render: (w: boolean, record: Ticket) => (
            <Tag color={!w ? 'red' : 'green'}>{w ? `Won: ${record.prize}` : 'No Prize'}</Tag>
          ),
        },
        {
          title: 'Amount',
          dataIndex: 'amount',
          render: (a: number) => a?.toLocaleString() + ' VND',
        },
      ]}
    />
  );
};

export default HistoryTable;