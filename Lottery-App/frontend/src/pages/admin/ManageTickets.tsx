import React, { useEffect, useState, useMemo } from 'react';
import { Table, Tag, Button, Input, Space, DatePicker, Select, message, Modal, Skeleton, Card } from 'antd';
import { EditOutlined, PlusOutlined, EyeOutlined, ExclamationCircleOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons';
import apiClient from '../../api/apiClient';
import dayjs from 'dayjs';
import AddTicketModal from './components/AddTicketModal';
import DashboardCard from '../../components/DashboardCard';
import CardList from '../../components/CardList';
import TicketCard from '../../components/TicketCard';

const { RangePicker } = DatePicker;

interface TicketRow {
  id: number;
  resultCode: string;
  stationName: string;
  drawDate: string;
  status: string;
  totalQueries: number;
  prizes?: Array<{
    type: string;
    winningNumber: string;
    rewardAmount?: number;
  }>;
}

const ManageTickets: React.FC = () => {
  const [data, setData] = useState<TicketRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState<{ id: number; name: string; stationCode: string }[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTicket, setEditingTicket] = useState<TicketRow | null>(null);

  interface FilterState {
    stationId: number | null;
    startDate: dayjs.Dayjs | null;
    endDate: dayjs.Dayjs | null;
    keyword: string;
  }

  const [filters, setFilters] = useState<FilterState>({
    stationId: null,
    startDate: null,
    endDate: null,
    keyword: '',
  });
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);
  const [sortField, setSortField] = useState<string>('drawDate');
  const [sortDir, setSortDir] = useState<string>('desc');
  const [refreshTick, setRefreshTick] = useState(0);
  const triggerRefresh = () => setRefreshTick(t => t + 1);

  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    apiClient.get('/admin/tickets/stations')
      .then(res => setStations(res.data.data))
      .catch(() => message.error('Failed to load stations'));
  }, []);

  useEffect(() => {
    const fetchTickets = async () => {
      setLoading(true);

      const params: Record<string, unknown> = {
        page,
        size: pageSize,
        sort: `${sortField},${sortDir}`,
      };

      if (filters.stationId) params.stationId = filters.stationId;
      if (filters.startDate) params.startDate = (filters.startDate as dayjs.Dayjs).format('YYYY-MM-DD');
      if (filters.endDate) params.endDate = (filters.endDate as dayjs.Dayjs).format('YYYY-MM-DD');
      if (filters.keyword) params.keyword = filters.keyword;

      try {
        const response = await apiClient.get('/admin/tickets', { params });

        setData(response.data.data.content);
        setTotal(response.data.data.totalElements || 0);
      } catch {
        message.error('Failed to load tickets');
      } finally {
        setLoading(false);
      }
    };

    fetchTickets();
  }, [filters, page, refreshTick, sortField, sortDir]);

  // Server-side sorting is now handled via sort param; local sort kept only for mobile card view
  const sortedData = useMemo(() => {
    if (!sortBy) return data;
    const arr = [...data];
    switch (sortBy) {
      case 'resultCode_asc': arr.sort((a, b) => (a.resultCode || '').localeCompare(b.resultCode || '')); break;
      case 'resultCode_desc': arr.sort((a, b) => (b.resultCode || '').localeCompare(a.resultCode || '')); break;
      case 'station_asc': arr.sort((a, b) => (a.stationName || '').localeCompare(b.stationName || '')); break;
      case 'station_desc': arr.sort((a, b) => (b.stationName || '').localeCompare(a.stationName || '')); break;
      case 'date_asc': arr.sort((a, b) => (a.drawDate || '').localeCompare(b.drawDate || '')); break;
      case 'date_desc': arr.sort((a, b) => (b.drawDate || '').localeCompare(a.drawDate || '')); break;
    }
    return arr;
  }, [data, sortBy]);

  const handleEdit = (ticket: TicketRow) => {
    setEditingTicket(ticket);
    setIsModalOpen(true);
  };

  const handleStatusChange = (id: number, currentStatus: string) => {
    const newStatus = currentStatus === 'PUBLISH' ? 'UNPUBLISH' : 'PUBLISH';
    const action = newStatus === 'PUBLISH' ? 'publish' : 'unpublish';
    Modal.confirm({
      title: `Are you sure you want to ${action} this ticket?`,
      icon: <ExclamationCircleOutlined />,
      content: newStatus === 'PUBLISH'
        ? 'This ticket will become publicly available for checking.'
        : 'Users will no longer be able to check this ticket.',
      okText: `Yes, ${action}`,
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await apiClient.patch(`/admin/tickets/${id}/status`, { status: newStatus });
          message.success(`Ticket ${action}ed successfully`);
          triggerRefresh();
        } catch (e: unknown) {
          const err = e as { response?: { data?: { message?: string } } };
          message.error(err.response?.data?.message || 'Failed to update status');
        }
      },
    });
  };

  const columns = [
    { title: 'Result Code', dataIndex: 'resultCode', key: 'resultCode' },
    { title: 'Station', dataIndex: 'stationName', key: 'stationName' },
    { title: 'Date', dataIndex: 'drawDate', key: 'drawDate', sorter: (a: TicketRow, b: TicketRow) => a.drawDate.localeCompare(b.drawDate) },
    {
      title: 'Tags',
      key: 'tags',
      render: (_: unknown, record: TicketRow) => (
        <Space>
          <Tag
            color={record.status === 'PUBLISH' ? 'green' : 'default'}
            style={{ cursor: 'pointer' }}
            onClick={() => handleStatusChange(record.id, record.status)}
          >
            {record.status === 'PUBLISH' ? 'Published' : 'Unpublished'}
          </Tag>
          <Tag icon={<EyeOutlined />} color="blue">{record.totalQueries.toLocaleString()} Views</Tag>
        </Space>
      ),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_: unknown, record: TicketRow) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>Edit</Button>
      ),
    },
  ];

  const desktopControls = (
    <Space wrap>
      <Select
        placeholder="Select Station"
        style={{ width: 200 }}
        allowClear
        value={filters.stationId}
        onChange={(val) => setFilters({ ...filters, stationId: val || null })}
      >
        {stations.map((s: { id: number; name: string }) => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
      </Select>
      <RangePicker
        onChange={(dates) => setFilters({
          ...filters,
          startDate: dates ? dates[0] : null,
          endDate: dates ? dates[1] : null,
        })}
      />
      <Input.Search
        placeholder="Search by result code or station"
        onSearch={(val) => setFilters({ ...filters, keyword: val })}
        style={{ width: 250 }}
      />
      <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingTicket(null); setIsModalOpen(true); }}>
        Add New
      </Button>
    </Space>
  );

  const mobileControls = (
    <>
      <Input
        placeholder="input search text"
        prefix={<SearchOutlined/>}
        onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
        onPressEnter={triggerRefresh}
        allowClear
      />
      <Select
        placeholder="Sort By"
        suffixIcon={<DownOutlined />}
        style={{ width: '100%' }}
        allowClear
        value={sortBy}
        onChange={(val) => setSortBy(val)}
        options={[
          { value: 'resultCode_asc', label: 'Result Code A-Z' },
          { value: 'resultCode_desc', label: 'Result Code Z-A' },
          { value: 'station_asc', label: 'Station A-Z' },
          { value: 'station_desc', label: 'Station Z-A' },
          { value: 'date_asc', label: 'Date Oldest' },
          { value: 'date_desc', label: 'Date Newest' },
        ]}
      />
      <Space orientation="horizontal">
        {/* Start Date Picker */}
        <DatePicker
          placeholder="Start Date"
          value={filters.startDate}
          onChange={(date) => setFilters({ ...filters, startDate: date })}
          disabledDate={(current) => {
            // Disable dates after the selected endDate
            return filters.endDate ? current && current > filters.endDate : false;
          }}
        />

        {/* End Date Picker */}
        <DatePicker
          placeholder="End Date"
          value={filters.endDate}
          onChange={(date) => setFilters({ ...filters, endDate: date })}
          disabledDate={(current) => {
            // Disable dates before the selected startDate
            return filters.startDate ? current && current < filters.startDate : false;
          }}
        />
      </Space>
      <Button
        type="primary"
        icon={<PlusOutlined />}
        onClick={() => { setEditingTicket(null); setIsModalOpen(true); }}
        block
      >
        Add New Ticket
      </Button>
    </>
  );

  return (
    <div>
      <DashboardCard
        title="Manage Ticket"
        desktopControls={desktopControls}
        mobileControls={mobileControls}
      >
        {/* Desktop Table */}
        <div className="desktop-view">
          {loading && data.length === 0 ? (
            <Skeleton active paragraph={{ rows: 8 }} />
          ) : (
            <Card>
              <Table
                columns={columns}
                dataSource={data}
                rowKey="id"
                loading={loading}
                pagination={{
                  current: page + 1,
                  pageSize,
                  total,
                  onChange: (p) => setPage(p - 1),
                }}
                onChange={(_pagination, _filters, sorter) => {
                  if (!Array.isArray(sorter) && sorter.field) {
                    setSortField(sorter.field as string);
                    setSortDir(sorter.order === 'ascend' ? 'asc' : 'desc');
                    setPage(0);
                  }
                }}
                locale={{ emptyText: 'No tickets found' }}
              />
            </Card>
          )}
        </div>

        {/* Mobile Cards */}
        <div className="mobile-view">
          <CardList loading={loading}>
            {sortedData.map((ticket) => (
              <TicketCard key={ticket.id} ticket={ticket} onEdit={handleEdit} />
            ))}
          </CardList>
        </div>
      </DashboardCard>

      <AddTicketModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        ticket={
          editingTicket
            ? {
                id: editingTicket.id,
                stationName: editingTicket.stationName,
                drawDate: editingTicket.drawDate,
                status: editingTicket.status,
                prizes: editingTicket.prizes || [],
              }
            : undefined
        }
        stations={stations}
        onSuccess={triggerRefresh}
      />

      <style>{`
        .desktop-view { display: block; }
        .mobile-view { display: none; }

        @media (max-width: 768px) {
          .desktop-view { display: none !important; }
          .mobile-view { display: block !important; }
        }
      `}</style>
    </div>
  );
};

export default ManageTickets;