import React, { useEffect, useMemo, useState } from 'react';
import { Table, Tag, Radio, Button, Input, Space, Dropdown, message, Modal, Form, Select, Typography, Skeleton, Card, Pagination } from 'antd';
import { EditOutlined, DownOutlined, ExclamationCircleOutlined, ClockCircleOutlined, UserOutlined, StopOutlined, SearchOutlined, CheckCircleFilled, CloseCircleFilled, MailFilled, InfoCircleFilled, SendOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import apiClient from '../../api/apiClient';
import DashboardCard from '../../components/DashboardCard';
import CardList from '../../components/CardList';
import UserCard from '../../components/UserCard';

const { Text } = Typography;

interface UserData {
  id: number;
  userCode: string;
  fullName: string;
  email: string;
  role: string;
  isActive: boolean;
  lastLogin?: string;
}

const ManageUsers: React.FC = () => {
  const [data, setData] = useState<UserData[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEmailModalOpen, setIsEmailModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserData | null>(null);
  const [form] = Form.useForm();
  const [emailForm] = Form.useForm();

  // Mobile bulk mode state
  const [isBulkMode, setIsBulkMode] = useState(false);
  const [mobileSelectedIds, setMobileSelectedIds] = useState<number[]>([]);
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);
  const [loginFilter, setLoginFilter] = useState<string | undefined>(undefined);
  const [refreshTick, setRefreshTick] = useState(0);
  const triggerRefresh = () => setRefreshTick(t => t + 1);

  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const pageSize = 20;

  useEffect(() => {
    const loadUsers = async () => {
      setLoading(true);

      try {
        const params: Record<string, unknown> = {
          page,
          size: pageSize,
        };

        if (keyword) params.keyword = keyword;
        if (loginFilter) params.loginFilter = loginFilter;

        const response = await apiClient.get('/admin/users', { params });

        setData(response.data.data.content);
        setTotal(response.data.data.totalElements || 0);
      } catch {
        message.error('Failed to load users');
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, [keyword, page, loginFilter, refreshTick]);

  const sortedUsers = useMemo(() => {
    if (!sortBy) return data;
    const arr = [...data];
    switch (sortBy) {
      case 'name_asc':
        arr.sort((a, b) => (a.fullName || '').localeCompare(b.fullName || ''));
        break;
      case 'name_desc':
        arr.sort((a, b) => (b.fullName || '').localeCompare(a.fullName || ''));
        break;
      case 'role':
        arr.sort((a, b) => (a.role || '').localeCompare(b.role || ''));
        break;
      case 'last_login':
        arr.sort((a, b) => {
          const aTime = a.lastLogin ? new Date(a.lastLogin).getTime() : 0;
          const bTime = b.lastLogin ? new Date(b.lastLogin).getTime() : 0;
          return bTime - aTime;
        });
        break;
      default:
        break;
    }
    return arr;
  }, [data, sortBy]);

  const handleBulkStatus = async (isActive: boolean) => {
    const ids = mobileSelectedIds.length > 0 ? mobileSelectedIds : selectedRowKeys;
    if (ids.length === 0) return;
    const action = isActive ? 'unlock' : 'block';
    Modal.confirm({
      title: `Are you sure you want to ${action} ${ids.length} user(s)?`,
      icon: <ExclamationCircleOutlined />,
      content: `This will ${isActive ? 'enable' : 'disable'} their access to the system.`,
      okText: `Yes, ${action}`,
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await apiClient.patch('/admin/users/status', { ids, isActive });
          message.success(`Users ${isActive ? 'unlocked' : 'locked'} successfully`);
          setSelectedRowKeys([]);
          setMobileSelectedIds([]);
          triggerRefresh();
        } catch (e: unknown) {
          const err = e as { response?: { data?: { message?: string } } };
          message.error(err.response?.data?.message || 'Action failed');
        }
      }
    });
  };

  const handleEdit = (user: UserData) => {
    setEditingUser(user);
    form.setFieldsValue({
      fullName: user.fullName,
      role: user.role,
      isActive: user.isActive
    });
    setIsModalOpen(true);
  };

  const onUpdateUser = async (values: { fullName: string; phone: string; role: string; isActive: boolean }) => {
    if (!editingUser) return;
    try {
      const payload = {
        fullName: values.fullName,
        phone: values.phone,
        role: values.role,
        isActive: values.isActive
      };
      await apiClient.put(`/admin/users/${editingUser.id}`, payload);
      message.success('User updated successfully. Note: role changes require refresh to take ui effect.');
      setIsModalOpen(false);
      triggerRefresh();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Update failed');
    }
  };

  const columns = [
    { title: 'User Code', dataIndex: 'userCode', key: 'userCode', sorter: (a: UserData, b: UserData) => a.userCode.localeCompare(b.userCode) },
    { title: 'Full Name', dataIndex: 'fullName', key: 'fullName', sorter: (a: UserData, b: UserData) => a.fullName.localeCompare(b.fullName) },
    { title: 'Email', dataIndex: 'email', key: 'email', sorter: (a: UserData, b: UserData) => a.email.localeCompare(b.email) },
    {
      title: 'Tags',
      key: 'tags',
      render: (_: unknown, record: UserData) => (
        <Space wrap size={[4, 4]}>
          <Tag icon={<UserOutlined />} color="blue">{record.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</Tag>
          <Tag icon={<ClockCircleOutlined />} color="default">
            {record.lastLogin
                ? dayjs(record.lastLogin).format('DD/MM/YYYY')
                : 'Never'}
          </Tag>
          {!record.isActive && (
            <Tag icon={<StopOutlined />} color="red">Blocked</Tag>
          )}
        </Space>
      )
    },
    {
      title: 'Action',
      key: 'action',
      render: (_: unknown, record: UserData) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>edit</Button>
      ),
    },
  ];

  const handleExport = async (format: string = 'csv') => {
    try {
      const idsToExport = isBulkMode && mobileSelectedIds.length > 0 
        ? mobileSelectedIds 
        : selectedRowKeys.map(Number);
      const params: Record<string, unknown> = { format };

      if (idsToExport.length > 0) {
        params.ids = idsToExport;
      } else if (keyword) {
        params.keyword = keyword;
      }
      const responseType = format === 'json' ? 'json' : 'blob';
      const response = await apiClient.get('/admin/users/export', { params, responseType });
      if (format === 'json') {
        const jsonStr = JSON.stringify(response.data, null, 2);
        const blob = new Blob([jsonStr], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'users_export.json');
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      } else {
        const mime = format === 'excel' 
          ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          : 'text/csv';
        const extension = format === 'excel' ? 'xlsx' : 'csv';
        const url = window.URL.createObjectURL(new Blob([response.data], { type: mime }));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `users_export.${extension}`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      }
      message.success(`${format.toUpperCase()} export completed`);
    } catch {
      message.error('Export failed');
    }
  };

  const selectedEmails = useMemo(() => {
    const ids = mobileSelectedIds.length > 0 ? mobileSelectedIds : selectedRowKeys.map(Number);
    return data.filter(u => ids.includes(u.id)).map(u => u.email);
  }, [data, mobileSelectedIds, selectedRowKeys]);

  const removeEmail = (email: string) => {
    if (mobileSelectedIds.length > 0) {
      const user = data.find(u => u.email === email);
      if (user) setMobileSelectedIds(prev => prev.filter(id => id !== user.id));
    } else {
      const user = data.find(u => u.email === email);
      if (user) setSelectedRowKeys(prev => prev.filter(id => id !== user.id));
    }
  };

  const handleSendEmail = async (values: { subject: string; content: string }) => {
    const ids = (mobileSelectedIds.length > 0 ? mobileSelectedIds : selectedRowKeys).map(Number);
    try {
      await apiClient.post('/admin/users/send-email', { ids, subject: values.subject, content: values.content });
      message.success(`Emails sent to ${ids.length} user(s).`);
      setIsEmailModalOpen(false);
      emailForm.resetFields();
      setSelectedRowKeys([]);
      setMobileSelectedIds([]);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to send emails');
    }
  };


  const bulkMenu = {
    items: [
      { key: 'lock', label: 'Bulk Block', onClick: () => handleBulkStatus(false) },
      { key: 'unlock', label: 'Bulk Unblock', onClick: () => handleBulkStatus(true) },
      { key: 'email', label: 'Send Email', onClick: () => setIsEmailModalOpen(true) },
      { key: 'export', label: 'Export', children: [
        { key: 'export-csv', label: 'CSV', onClick: () => handleExport('csv') },
        { key: 'export-excel', label: 'Excel', onClick: () => handleExport('excel') },
        { key: 'export-json', label: 'JSON', onClick: () => handleExport('json') },
      ]},
    ],
  };

  const exportMenu = {
    items: [
        { key: 'export-csv', label: 'CSV', onClick: () => handleExport('csv') },
        { key: 'export-excel', label: 'Excel', onClick: () => handleExport('excel') },
        { key: 'export-json', label: 'JSON', onClick: () => handleExport('json') },
    ],
  };

  const desktopControls = (
    <Space>
      <Select
        placeholder="Last login"
        value={loginFilter}
        onChange={(value) => {
          setLoginFilter(value);
          triggerRefresh();
        }}
        allowClear
        style={{ width: 160 }}
        options={[
            { value: 'inactive-1w', label: 'Inactive for 1 week' },
            { value: 'inactive-1m', label: 'Inactive for 1 month' },
            { value: 'inactive-3m', label: 'Inactive for 3 months' },
            { value: 'inactive-6m', label: 'Inactive for 6 months' },
            { value: 'inactive-1y', label: 'Inactive for 1 year' },
        ]}
      />

      <Input.Search
        placeholder="email/phone/usercode"
        allowClear
        onSearch={(value) => {
          setKeyword(value);
          setPage(0);
        }}
        style={{ width: 250 }}
      />

      <Dropdown menu={bulkMenu} disabled={selectedRowKeys.length === 0}>
        <Button>
          Actions <DownOutlined />
        </Button>
      </Dropdown>

      <Dropdown menu={exportMenu}>
        <Button>
            Export <DownOutlined />
        </Button>
    </Dropdown>
    </Space>
  );

  const mobileControls = (
    <>
      <Input
        placeholder="email/phone/usercode"
        prefix={<SearchOutlined/>}
        onChange={(e) => {
          setKeyword(e.target.value);
          setPage(0);
        }}
        allowClear
      />
      <Select
        placeholder="Filter: last login"
        value={loginFilter}
        onChange={(value) => {
          setLoginFilter(value);
          setPage(0);
          triggerRefresh();
        }}
        allowClear
        style={{ width: '100%' }}
        options={[
          { value: 'inactive-1w', label: 'Inactive 1 week' },
          { value: 'inactive-1m', label: 'Inactive 1 month' },
          { value: 'inactive-3m', label: 'Inactive 3 months' },
          { value: 'inactive-6m', label: 'Inactive 6 months' },
          { value: 'inactive-1y', label: 'Inactive 1 year' },
        ]}
      />
      <div style={{ display: 'flex', gap: 8 }}>
        <Button
          type={isBulkMode ? 'primary' : 'default'}
          onClick={() => {
            setIsBulkMode(!isBulkMode);
            setMobileSelectedIds([]);
          }}
          style={{ flex: 1 }}
        >
          Bulk Action
        </Button>
      </div>
      <Select
        placeholder="Sort By"
        value={sortBy}
        onChange={(value) => setSortBy(value)}
        suffixIcon={<DownOutlined />}
        style={{ width: '100%' }}
        allowClear
        options={[
          { value: 'name_asc', label: 'Name A-Z' },
          { value: 'name_desc', label: 'Name Z-A' },
          { value: 'role', label: 'Role' },
          { value: 'last_login', label: 'Last Login' },
        ]}
      />
    </>
  );

  return (
    <div>
      <DashboardCard
        title="Manage User"
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
                  rowSelection={{ selectedRowKeys, onChange: setSelectedRowKeys }}
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
                  locale={{ emptyText: 'No users found' }}
                />
            </Card>
          )}
        </div>

        {/* Mobile Cards */}
        <div className="mobile-view">
          <CardList loading={loading}>
            {sortedUsers.map((user: UserData) => {
              const isSelected = mobileSelectedIds.includes(user.id);
              return (
                <UserCard
                  key={user.id}
                  user={user}
                  selected={isBulkMode && isSelected}
                  onClick={
                    isBulkMode
                      ? () => {
                          if (isSelected) setMobileSelectedIds(mobileSelectedIds.filter(id => id !== user.id));
                          else setMobileSelectedIds([...mobileSelectedIds, user.id]);
                        }
                      : undefined
                  }
                  actions={
                    isBulkMode
                      ? undefined
                      : [
                          <SendOutlined key="send" onClick={(e: React.MouseEvent) => { e.stopPropagation(); setMobileSelectedIds([user.id]); setIsEmailModalOpen(true); }} />,
                          <EditOutlined key="edit" onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleEdit(user); }} />,
                        ]
                  }
                />
              );
            })}
          </CardList>
          {total > pageSize && (
            <Pagination
              current={page + 1}
              pageSize={pageSize}
              total={total}
              onChange={(p) => setPage(p - 1)}
              simple
              style={{ marginTop: 16, display: 'flex', justifyContent: 'center' }}
            />
          )}
        </div>
      </DashboardCard>

      {isBulkMode && (
        <div
          className="mobile-bulk-footer"
          style={{
            position: 'fixed',
            bottom: 0,
            left: 0,
            right: 0,
            background: '#ffffff',
            padding: '12px 16px',
            borderTop: '1px solid #f0f0f0',
            boxShadow: '0 -4px 12px rgba(0,0,0,0.08)',
            zIndex: 1000,
            display: 'flex',
            justifyContent: 'space-around',
            alignItems: 'center',
          }}
        >
          <Button icon={<CloseCircleFilled color='red' />} onClick={() => handleBulkStatus(false)} disabled={mobileSelectedIds.length === 0}>Block</Button>
          <Button icon={<CheckCircleFilled color='green' />} onClick={() => handleBulkStatus(true)} disabled={mobileSelectedIds.length === 0}>Unblock</Button>
          <Button icon={<MailFilled color='blue'/>} onClick={() => setIsEmailModalOpen(true)} disabled={mobileSelectedIds.length === 0}>Email</Button>
          <Dropdown menu={{
            items: [
              { key: 'csv', label: 'CSV', onClick: () => handleExport('csv') },
              { key: 'excel', label: 'Excel', onClick: () => handleExport('excel') },
              { key: 'json', label: 'JSON', onClick: () => handleExport('json') },
            ]
          }} disabled={mobileSelectedIds.length === 0}>
            <Button icon={<InfoCircleFilled color='blue' />} disabled={mobileSelectedIds.length === 0}>Export</Button>
          </Dropdown>
        </div>
      )}

      <Modal title="Edit User" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} zIndex={1100}>
        <Form form={form} layout="vertical" onFinish={onUpdateUser}>
          <Text type="secondary">User Code: {editingUser?.userCode}</Text><br/>
          <Text type="secondary">Email: {editingUser?.email}</Text>
          <Form.Item name="fullName" label="Full Name" style={{ marginTop: 16 }}>
            <Input/>
          </Form.Item>
          <Form.Item name="role" label="Role">
            <Select>
              <Select.Option value="ROLE_USER">USER</Select.Option>
              <Select.Option value="ROLE_ADMIN">ADMIN</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="isActive" label="Status">
            <Radio.Group buttonStyle="solid">
              <Radio.Button value={true}>Active</Radio.Button>
              <Radio.Button value={false}>Blocked</Radio.Button>
            </Radio.Group>
          </Form.Item>
          <div style={{ textAlign: 'right', gap: 8, display: 'flex', justifyContent: 'flex-end' }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </div>
        </Form>
      </Modal>

      <Modal title="Send Email" open={isEmailModalOpen} onCancel={() => setIsEmailModalOpen(false)} onOk={() => emailForm.submit()} destroyOnHidden zIndex={1100}>
        <Form form={emailForm} layout="vertical" onFinish={handleSendEmail}>
          <Text type="secondary">To:</Text>
          <div style={{ marginTop: 8, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
            {selectedEmails.map(email => (
              <Tag key={email} closable onClose={() => removeEmail(email)} color="blue">{email}</Tag>
            ))}
          </div>
          <Form.Item name="subject" label="Subject" rules={[{ required: true }]} style={{ marginTop: 16 }}>
            <Input placeholder="input subject here"/>
          </Form.Item>
          <Form.Item name="content" label="Content" rules={[{ required: true }]}>
            <Input.TextArea placeholder="input content here" rows={4}maxLength={500} showCount />
          </Form.Item>
        </Form>
      </Modal>

      <style>{`
        .desktop-controls { display: flex; }
        .desktop-view { display: block; }
        .mobile-view { display: none; }

        @media (max-width: 768px) {
          .desktop-controls { display: none !important; }
          .desktop-view { display: none !important; }
          .mobile-view { display: block !important; }
        }
      `}</style>
    </div>
  );
};

export default ManageUsers;