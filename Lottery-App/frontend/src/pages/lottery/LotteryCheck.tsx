import React, { useState, useEffect } from 'react';
import { Card, Select, DatePicker, Input, Button, Typography, Space, Alert, message, Divider } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined, FacebookFilled } from '@ant-design/icons';
import apiClient from '../../api/apiClient';
import dayjs from 'dayjs';

const { Title } = Typography;

const LotteryCheck: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState([]);
  const [results, setResults] = useState<any>(null);
  const isGuest = !localStorage.getItem('token');
 
  const [form, setForm] = useState<any>({
    stationId: null,
    date: dayjs(),
    numbers: ''
  });

  useEffect(() => {
    apiClient.get('/admin/tickets/stations').then(res => {
      const list = res.data.data;
      setStations(list);
      // Default to HCM station (usually first or find by name)
      const hcm = list.find((s: any) => s.stationCode === 'SOU-HCM') || list[0];
      if (hcm && !form.stationId) {
        setForm((prev: any) => ({ ...prev, stationId: hcm.id }));
      }
    });
  }, []);

  const handleCheck = async () => {
    const nums = form.numbers.split(/[\n,;]+/).filter((n: string) => n.trim());

    if (isGuest && nums.length > 1) {
      return message.error('Guests can only check 1 ticket at a time. Please login to check multiple!');
    }

    // Validate each number is exactly 6 digits
    for (const num of nums) {
      if (!/^\d{6}$/.test(num.trim())) {
        return message.error(`Invalid ticket number: "${num.trim()}". Each ticket must be exactly 6 digits.`);
      }
    }

    if (!form.stationId || !form.date || nums.length === 0) {
      return message.warning('Please fill all fields');
    }
    setLoading(true);
    try {
      const email = localStorage.getItem('email');
      const res = await apiClient.post('/checker/check', nums, {
        params: { stationId: form.stationId, date: form.date.format('YYYY-MM-DD'), email }
      });
      setResults(res.data.data);
    } catch (e: any) {
      message.error(e.response?.data?.message || 'Check failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: 800, margin: '0 auto' }}>
      <Card style={{ borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
        <Title level={4}>Lottery Check</Title>
        <Space orientation="vertical" style={{ width: '100%' }} size="middle">
          <Select
            placeholder="Select Station"
            style={{ width: '100%', borderRadius: 12 }}
            value={form.stationId}
            onChange={(v) => setForm({...form, stationId: v})}
          >
            {stations.map((s: any) => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
          </Select>
         
          <DatePicker
            style={{ width: '100%', borderRadius: 2 }}
            value={form.date}
            onChange={(d) => setForm({...form, date: d})}
          />

          {isGuest ? (
            <Input
              placeholder="Enter your 6-digit ticket number"
              maxLength={6}
              style={{ borderRadius: 2 }}
              value={form.numbers}
              onChange={(e) => {
                // Only allow digits
                const val = e.target.value.replace(/\D/g, '').slice(0, 6);
                setForm({...form, numbers: val});
              }}
            />
          ) : (
            <Input.TextArea
              placeholder="Enter your ticket numbers (separate by comma or newline)"
              rows={4}
              style={{ borderRadius: 2 }}
              value={form.numbers}
              onChange={(e) => setForm({...form, numbers: e.target.value})}
            />
          )}

          <Button type="primary" block size="large" loading={loading} onClick={handleCheck} style={{ borderRadius: 12 }}>
            Check Ticket
          </Button>

          {results && (
            <div style={{ marginTop: 24 }}>
              <Divider>Results</Divider>
              {results.details.map((res: any, i: number) => (
                <Alert
                  key={i}
                  style={{ marginBottom: 12, borderRadius: 12 }}
                  message={`Ticket: ${res.number}`}
                  description={res.isWon ? `Congratulations!! you won the ${res.prize} prize` : "Better luck next time"}
                  type={res.isWon ? "success" : "error"}
                  showIcon
                  icon={res.isWon ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                  action={res.isWon && !isGuest && (
                    <Button
                      size="small"
                      type="primary"
                      icon={<FacebookFilled />}
                      style={{ borderRadius: 12 }}
                      onClick={() => {
                        const shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(window.location.href)}&quote=I just won ${res.prize} with ticket ${res.number}!`;
                        window.open(shareUrl, '_blank', 'width=600,height=400');
                      }}
                    >
                      Share
                    </Button>
                  )}
                />
              ))}
              <Alert
                message="Summary"
                description={`Total Spent: ${results.summary.totalSpent.toLocaleString()} VND | Total Won: ${results.summary.totalWon.toLocaleString()} VND`}
                type="info"
                showIcon
                icon={<InfoCircleOutlined />}
                style={{ borderRadius: 12 }}
              />
            </div>
          )}
        </Space>
      </Card>
    </div>
  );
};

export default LotteryCheck; 
