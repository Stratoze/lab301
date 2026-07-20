import React, { useEffect, useMemo, useState } from 'react';
import { Table, Tag, Radio, Button, Input, Space, Dropdown, message, Modal, Form, Select, Typography, Skeleton } from 'antd';
import { EditOutlined, DownOutlined, ExclamationCircleOutlined, ClockCircleOutlined, UserOutlined, StopOutlined, SearchOutlined, CheckCircleFilled, CloseCircleFilled, MailFilled, InfoCircleFilled, SendOutlined } from '@ant-design/icons';
import apiClient from '../../api/apiClient';
import DashboardCard from '../../components/DashboardCard';
import CardList from '../../components/CardList';
import UserCard from '../../components/UserCard';

const { Text } = Typography;

const ManageUsers: React.FC = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEmailModalOpen, setIsEmailModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<any>(null);
  const [form] = Form.useForm();
  const [emailForm] = Form.useForm();
  const [addUserForm] = Form.useForm();

  // Mobile bulk mode state
  const [isBulkMode, setIsBulkMode] = useState(false);
  const [mobileSelectedIds, setMobileSelectedIds] = useState<number[]>([]);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);
  const [loginFilter, setLoginFilter] = useState<string | undefined>(undefined);

  const fetchUsers = async (keyword = '') => {
    setLoading(true);
    try {
      const params: any = {};
      if (keyword) params.keyword = keyword;
      if (loginFilter) params.loginFilter = loginFilter;
      const response = await apiClient.get('/admin/users', { params });
      setData(response.data.data.content);
    } catch (error) {
      message.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const sortedUsers = useMemo(() => {
    if (!sortBy) return data;
    const arr = [...data];
    switch (sortBy) {
      case 'name_asc':
        arr.sort((a: any, b: any) => (a.fullName || '').localeCompare(b.fullName || ''));
        break;
      case 'name_desc':
        arr.sort((a: any, b: any) => (b.fullName || '').localeCompare(a.fullName || ''));
        break;
      case 'role':
        arr.sort((a: any, b: any) => (a.role || '').localeCompare(b.role || ''));
        break;
      case 'last_login':
        arr.sort((a: any, b: any) => {
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
          fetchUsers();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Action failed');
        }
      }
    });
  };

  const handleEdit = (user: any) => {
    setEditingUser(user);
    form.setFieldsValue({
      fullName: user.fullName,
      role: user.role,
      isActive: user.isActive
    });
    setIsModalOpen(true);
  };

  const onUpdateUser = async (values: any) => {
    try {
      const payload = {
        fullName: values.fullName,
        phone: values.phone,
        role: values.role,
        isActive: values.isActive
      };
      await apiClient.put(`/admin/users/${editingUser.id}`, payload);
      message.success('User updated successfully. Note: role changes require re-login to take effect.');
      setIsModalOpen(false);
      fetchUsers();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Update failed');
    }
  };

  const columns = [
    { title: 'User Code', dataIndex: 'userCode', key: 'userCode', sorter: (a: any, b: any) => a.userCode.localeCompare(b.userCode) },
    { title: 'Full Name', dataIndex: 'fullName', key: 'fullName', sorter: (a: any, b: any) => a.fullName.localeCompare(b.fullName) },
    { title: 'Email', dataIndex: 'email', key: 'email', sorter: (a: any, b: any) => a.email.localeCompare(b.email) },
    {
      title: 'Tags',
      key: 'tags',
      render: (_: any, record: any) => (
        <Space wrap size={[4, 4]}>
          <Tag icon={<UserOutlined />} color="blue">{record.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</Tag>
          <Tag icon={<ClockCircleOutlined />} color="default">
            {record.lastLogin
              ? new Date(record.lastLogin).toLocaleString('vi-VN', { hour12: false })
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
      render: (_: any, record: any) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>edit</Button>
      ),
    },
  ];

  const handleExport = async (format: string = 'csv') => {
    try {
      const idsToExport = isBulkMode && mobileSelectedIds.length > 0 
        ? mobileSelectedIds 
        : selectedRowKeys.map(Number);
      const params: any = { format };
      if (idsToExport.length > 0) params.ids = idsToExport;
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
    } catch (error) {
      message.error('Export failed');
    }
  };

  const handleSendEmail = async (values: any) => {
    const ids = (mobileSelectedIds.length > 0 ? mobileSelectedIds : selectedRowKeys).map(Number);
    try {
      await apiClient.post('/admin/users/send-email', { ids, subject: values.subject, content: values.content });
      message.success(`Emails sent to ${ids.length} user(s).`);
      setIsEmailModalOpen(false);
      emailForm.resetFields();
      setSelectedRowKeys([]);
      setMobileSelectedIds([]);
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Failed to send emails');
    }
  };

  const handleAddUser = async (values: any) => {
    try {
      await apiClient.post('/auth/register', {
        email: values.email,
        password: values.password,
        fullName: values.fullName,
        phone: values.phone || '',
      });
      message.success('User created successfully');
      setIsAddModalOpen(false);
      addUserForm.resetFields();
      fetchUsers();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to create user');
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

  const desktopControls = (
    <Space>
      <Select
        placeholder="Last login"
        value={loginFilter}
        onChange={(value) => {
          setLoginFilter(value);
          fetchUsers();
        }}
        allowClear
        style={{ width: 160 }}
        options={[
          { value: '24h', label: 'Within 24 hours' },
          { value: '1m', label: 'Within 1 month' },
          { value: '3m', label: 'Within 3 months' },
          { value: '6m', label: 'Within 6 months' },
          { value: '1y', label: 'Within 1 year' },
        ]}
      />
      <Input.Search 
        placeholder="email/phone/usercode" 
        onSearch={fetchUsers} 
        style={{ width: 250, borderRadius: 2 }} 
      />
      <Dropdown menu={bulkMenu} disabled={selectedRowKeys.length === 0}>
        <Button style={{ borderRadius: 12 }}>
          Actions <DownOutlined />
        </Button>
      </Dropdown>
    </Space>
  );

  const mobileControls = (
    <>
      <Input
        placeholder="input search text"
        prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
        onChange={(e) => fetchUsers(e.target.value)}
        allowClear
        style={{ borderRadius: 8 }}
      />
      <div style={{ display: 'flex', gap: 8 }}>
        <Button
          type={isBulkMode ? 'primary' : 'default'}
          onClick={() => {
            setIsBulkMode(!isBulkMode);
            setMobileSelectedIds([]);
          }}
          style={{ flex: 1, borderRadius: 20 }}
        >
          Bulk Action
        </Button>
        <Button
          type="default"
          onClick={() => {
            addUserForm.resetFields();
            setIsAddModalOpen(true);
          }}
          style={{ flex: 1, borderRadius: 20 }}
        >
          Add New
        </Button>
      </div>
      <Select
        placeholder="Sort By"
        value={sortBy}
        onChange={(value) => setSortBy(value)}
        suffixIcon={<DownOutlined />}
        style={{ width: '100%', borderRadius: 8 }}
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
            <Table 
              rowSelection={{ selectedRowKeys, onChange: setSelectedRowKeys }}
              columns={columns} 
              dataSource={data} 
              rowKey="id" 
              loading={loading}
              pagination={{ pageSize: 20 }}
              locale={{ emptyText: 'No users found' }}
            />
          )}
        </div>

        {/* Mobile Cards */}
        <div className="mobile-view">
          <CardList loading={loading}>
            {sortedUsers.map((user: any) => {
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
                          <SendOutlined key="send" onClick={(e: any) => { e.stopPropagation(); message.info('Email feature coming soon'); }} />,
                          <EditOutlined key="edit" onClick={(e: any) => { e.stopPropagation(); handleEdit(user); }} />,
                        ]
                  }
                />
              );
            })}
          </CardList>
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
          <Button shape="round" icon={<CloseCircleFilled style={{ color: '#ff4d4f' }} />} onClick={() => handleBulkStatus(false)} disabled={mobileSelectedIds.length === 0}>Block</Button>
          <Button shape="round" icon={<CheckCircleFilled style={{ color: '#52c41a' }} />} onClick={() => handleBulkStatus(true)} disabled={mobileSelectedIds.length === 0}>Unblock</Button>
          <Button shape="round" icon={<MailFilled style={{ color: '#1677ff' }} />} onClick={() => setIsEmailModalOpen(true)} disabled={mobileSelectedIds.length === 0}>Email</Button>
          <Dropdown menu={{
            items: [
              { key: 'csv', label: 'CSV', onClick: () => handleExport('csv') },
              { key: 'excel', label: 'Excel', onClick: () => handleExport('excel') },
              { key: 'json', label: 'JSON', onClick: () => handleExport('json') },
            ]
          }} disabled={mobileSelectedIds.length === 0}>
            <Button shape="round" icon={<InfoCircleFilled style={{ color: '#1677ff' }} />} disabled={mobileSelectedIds.length === 0}>Export</Button>
          </Dropdown>
        </div>
      )}

      <Modal title="Edit User" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} style={{ borderRadius: 12 }} zIndex={1100}>
        <Form form={form} layout="vertical" onFinish={onUpdateUser}>
          <Text type="secondary">User Code: {editingUser?.userCode}</Text><br/>
          <Text type="secondary">Email: {editingUser?.email}</Text>
          <Form.Item name="fullName" label="Full Name" style={{ marginTop: 16 }}>
            <Input style={{ borderRadius: 2 }} />
          </Form.Item>
          <Form.Item name="role" label="Role">
            <Select style={{ borderRadius: 12 }}>
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
            <Button onClick={() => setIsModalOpen(false)} style={{ borderRadius: 12 }}>Cancel</Button>
            <Button type="primary" htmlType="submit" style={{ borderRadius: 12 }}>Save</Button>
          </div>
        </Form>
      </Modal>

      <Modal title="Send Email" open={isEmailModalOpen} onCancel={() => setIsEmailModalOpen(false)} onOk={() => emailForm.submit()} destroyOnHidden zIndex={1100}>
        <Form form={emailForm} layout="vertical" onFinish={handleSendEmail}>
          <Text type="secondary">To: {mobileSelectedIds.length > 0 ? mobileSelectedIds.length : selectedRowKeys.length} selected users</Text>
          <Form.Item name="subject" label="Subject" rules={[{ required: true }]} style={{ marginTop: 16 }}>
            <Input placeholder="input subject here" style={{ borderRadius: 2 }} />
          </Form.Item>
          <Form.Item name="content" label="Content" rules={[{ required: true }]}>
            <Input.TextArea placeholder="input content here" rows={4} style={{ borderRadius: 2 }} maxLength={500} showCount />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Add New User" open={isAddModalOpen} onCancel={() => { setIsAddModalOpen(false); addUserForm.resetFields(); }} footer={null} destroyOnHidden zIndex={1100}>
        <Form form={addUserForm} layout="vertical" onFinish={handleAddUser}>
          <Form.Item name="fullName" label="Full Name" rules={[{ required: true, message: 'Please enter full name' }]}>
            <Input placeholder="Nguyen Van A" style={{ borderRadius: 8 }} />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Please enter email' }, { type: 'email', message: 'Invalid email format' }]}>
            <Input placeholder="example@gmail.com" style={{ borderRadius: 8 }} />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input placeholder="0123456789" style={{ borderRadius: 8 }} />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter password' }, { min: 10, message: 'Password must be at least 10 characters' }]}>
            <Input.Password placeholder="At least 10 characters" style={{ borderRadius: 8 }} />
          </Form.Item>
          <div style={{ textAlign: 'right', gap: 8, display: 'flex', justifyContent: 'flex-end' }}>
            <Button onClick={() => { setIsAddModalOpen(false); addUserForm.resetFields(); }} style={{ borderRadius: 20 }}>Cancel</Button>
            <Button type="primary" htmlType="submit" style={{ borderRadius: 20 }}>Create</Button>
          </div>
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