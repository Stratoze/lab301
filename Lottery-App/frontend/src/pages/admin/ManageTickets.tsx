import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Table, Tag, Button, Input, Space, DatePicker, Select, message, Modal, Skeleton } from 'antd';
import { EditOutlined, PlusOutlined, EyeOutlined, ExclamationCircleOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons';
import apiClient from '../../api/apiClient';
import AddTicketModal from './components/AddTicketModal';
import DashboardCard from '../../components/DashboardCard';
import CardList from '../../components/CardList';
import TicketCard from '../../components/TicketCard';

const { RangePicker } = DatePicker;

interface TicketData {
  id: number;
  resultCode: string;
  stationName: string;
  drawDate: string;
  status: string;
  totalQueries: number;
}

const ManageTickets: React.FC = () => {
  const [data, setData] = useState<TicketData[]>([]);
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTicket, setEditingTicket] = useState<any>(null);

  const [filters, setFilters] = useState<any>({
    stationId: null,
    startDate: null,
    endDate: null,
    keyword: '',
  });
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);

  const fetchStations = async () => {
    try {
      const res = await apiClient.get('/admin/tickets/stations');
      setStations(res.data.data);
    } catch (e) { message.error('Failed to load stations'); }
  };

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const params: any = {};
      if (filters.stationId) params.stationId = filters.stationId;
      if (filters.startDate) params.startDate = filters.startDate.format('YYYY-MM-DD');
      if (filters.endDate) params.endDate = filters.endDate.format('YYYY-MM-DD');
      if (filters.keyword) params.keyword = filters.keyword;

      const response = await apiClient.get('/admin/tickets', { params });
      setData(response.data.data.content);
    } catch (error) {
      message.error('Failed to load tickets');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  const sortedData = useMemo(() => {
    if (!sortBy) return data;
    const arr = [...data];
    switch (sortBy) {
      case 'resultCode_asc':
        arr.sort((a, b) => (a.resultCode || '').localeCompare(b.resultCode || ''));
        break;
      case 'resultCode_desc':
        arr.sort((a, b) => (b.resultCode || '').localeCompare(a.resultCode || ''));
        break;
      case 'station_asc':
        arr.sort((a, b) => (a.stationName || '').localeCompare(b.stationName || ''));
        break;
      case 'station_desc':
        arr.sort((a, b) => (b.stationName || '').localeCompare(a.stationName || ''));
        break;
      case 'date_asc':
        arr.sort((a, b) => (a.drawDate || '').localeCompare(b.drawDate || ''));
        break;
      case 'date_desc':
        arr.sort((a, b) => (b.drawDate || '').localeCompare(a.drawDate || ''));
        break;
    }
    return arr;
  }, [data, sortBy]);

  useEffect(() => { fetchStations(); }, []);
  useEffect(() => { fetchTickets(); }, [fetchTickets]);

  const handleEdit = (ticket: any) => {
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
          fetchTickets();
        } catch (e: any) {
          message.error(e.response?.data?.message || 'Failed to update status');
        }
      },
    });
  };

  const columns = [
    { title: 'Result Code', dataIndex: 'resultCode', key: 'resultCode' },
    { title: 'Station', dataIndex: 'stationName', key: 'stationName' },
    { title: 'Date', dataIndex: 'drawDate', key: 'drawDate', sorter: (a: any, b: any) => a.drawDate.localeCompare(b.drawDate) },
    {
      title: 'Tags',
      key: 'tags',
      render: (_: any, record: any) => (
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
      render: (_: any, record: any) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>Edit</Button>
      ),
    },
  ];

  const desktopControls = (
    <Space wrap>
      <Select
        placeholder="Select Station"
        style={{ width: 200, borderRadius: 2 }}
        allowClear
        value={filters.stationId}
        onChange={(val) => setFilters({ ...filters, stationId: val || null })}
      >
        {stations.map((s: any) => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
      </Select>
      <RangePicker
        style={{ borderRadius: 2 }}
        onChange={(dates) => setFilters({
          ...filters,
          startDate: dates ? dates[0] : null,
          endDate: dates ? dates[1] : null,
        })}
      />
      <Input.Search
        placeholder="Search by result code or station"
        onSearch={(val) => setFilters({ ...filters, keyword: val })}
        style={{ width: 250, borderRadius: 2 }}
      />
      <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingTicket(null); setIsModalOpen(true); }} style={{ borderRadius: 12 }}>
        Add New
      </Button>
    </Space>
  );

  const mobileControls = (
    <>
      <Input
        placeholder="input search text"
        prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
        onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
        onPressEnter={() => fetchTickets()}
        allowClear
        style={{ borderRadius: 8 }}
      />
      <Select
        placeholder="Sort By"
        suffixIcon={<DownOutlined />}
        style={{ width: '100%', borderRadius: 8 }}
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
      <RangePicker
        style={{ width: '100%', borderRadius: 8 }}
        onChange={(dates) => setFilters({
          ...filters,
          startDate: dates ? dates[0] : null,
          endDate: dates ? dates[1] : null,
        })}
      />
      <Button
        type="primary"
        icon={<PlusOutlined />}
        onClick={() => { setEditingTicket(null); setIsModalOpen(true); }}
        style={{ borderRadius: 20 }}
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
            <Table
              columns={columns}
              dataSource={sortedData} /* Updated to use sortedData */
              rowKey="id"
              loading={loading}
              locale={{ emptyText: 'No tickets found' }}
            />
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
        ticket={editingTicket}
        stations={stations}
        onSuccess={fetchTickets}
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