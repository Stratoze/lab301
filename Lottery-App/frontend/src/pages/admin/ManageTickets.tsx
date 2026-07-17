import React, { useEffect, useState, useCallback } from 'react';
import { Table, Card, Tag, Button, Input, Space, DatePicker, Select, Typography, message, Modal, Skeleton } from 'antd';
import { EditOutlined, PlusOutlined, EyeOutlined, CalendarOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import apiClient from '../../api/apiClient';
import AddTicketModal from './components/AddTicketModal';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const ManageTickets: React.FC = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTicket, setEditingTicket] = useState<any>(null);
  
  const [filters, setFilters] = useState<any>({
    stationId: null,
    startDate: null,
    endDate: null,
    keyword: ''
  });

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
      }
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
      )
    },
    {
      title: 'Action',
      key: 'action',
      render: (_: any, record: any) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>Edit</Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <Title level={4} style={{ margin: 0 }}>Manage Ticket</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingTicket(null); setIsModalOpen(true); }} style={{ borderRadius: 12 }}>
          Add New
        </Button>
      </div>

      <Card style={{ marginBottom: 16, borderRadius: 12 }}>
        <Space wrap>
          <Select 
            placeholder="Select Station" 
            style={{ width: 200, borderRadius: 2 }} 
            allowClear
            value={filters.stationId}
            onChange={(val) => setFilters({...filters, stationId: val || null})}
          >
            {stations.map((s: any) => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
          </Select>
          <RangePicker 
            style={{ borderRadius: 2 }} 
            onChange={(dates) => setFilters({
              ...filters, 
              startDate: dates ? dates[0] : null, 
              endDate: dates ? dates[1] : null
            })}
          />
          <Input.Search 
            placeholder="Search by result code or station" 
            onSearch={(val) => setFilters({...filters, keyword: val})} 
            style={{ width: 250, borderRadius: 2 }} 
          />
        </Space>
      </Card>

      {/* Desktop View */}
      <div className="desktop-view">
        <Card style={{ borderRadius: 12 }}>
          {loading && data.length === 0 ? (
            <Skeleton active paragraph={{ rows: 8 }} />
          ) : (
            <Table 
              columns={columns} 
              dataSource={data} 
              rowKey="id" 
              loading={loading}
              locale={{ emptyText: 'No tickets found' }}
            />
          )}
        </Card>
      </div>

      {/* Mobile View */}
      <div className="mobile-view" style={{ display: 'none' }}>
        {loading ? <Skeleton active paragraph={{ rows: 4 }} /> : (
          data.map((ticket: any) => (
            <Card key={ticket.id} style={{ marginBottom: 12, borderRadius: 12 }} bodyStyle={{ padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Text strong style={{ fontSize: 16 }}>{ticket.resultCode}</Text>
                <Space size={[4, 4]} wrap>
                  <Tag color={ticket.status === 'PUBLISH' ? 'green' : 'default'}>{ticket.status}</Tag>
                  <Tag icon={<EyeOutlined />} color="blue">{(ticket.totalQueries ?? 0).toLocaleString()} Views</Tag>
                </Space>
              </div>
              <div style={{ marginTop: 8 }}>
                 <div><CalendarOutlined /> Date Added: {ticket.drawDate}</div>
                 <Text type="secondary">Station: {ticket.stationName}</Text>
              </div>
              <div style={{ marginTop: 12, borderTop: '1px solid #f0f0f0', paddingTop: 12, textAlign: 'right' }}>
                 <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(ticket)}>Edit</Button>
              </div>
            </Card>
          ))
        )}
      </div>

      <AddTicketModal 
        open={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        ticket={editingTicket} 
        stations={stations}
        onSuccess={fetchTickets}
      />

      <style>{`
        @media (max-width: 768px) {
          .desktop-view { display: none !important; }
          .mobile-view { display: block !important; }
        }
      `}</style>
    </div>
  );
};

export default ManageTickets;