import React, { useEffect, useState } from 'react';
import { Button, Card, Table, Tag, Space, Spin, Empty } from 'antd';
import { useNavigate } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import apiClient from '../../api/apiClient';

const HistoryAnalytics: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [history, setHistory] = useState<any[]>([]);
  const [chartData, setChartData] = useState<any[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const email = localStorage.getItem('email');
      const res = await apiClient.get('/checker/history', { params: { email } });
      const sessions = res.data.data || [];
     
      // Build chart data from sessions
      const chart = sessions.map((s: any) => ({
        name: new Date(s.date).toLocaleDateString(),
        spent: s.totalSpent,
        won: s.totalWon
      }));
      setChartData(chart);

      // Flatten all tickets from all sessions for the table
      const allTickets: any[] = [];
      sessions.forEach((s: any) => {
        (s.tickets || []).forEach((t: any) => {
          allTickets.push({
            ...t,
            date: new Date(s.date).toLocaleDateString(),
            station: t.station
          });
        });
      });
      setHistory(allTickets);
    } catch (e) {
      console.error('Failed to load history', e);
    } finally {
      setLoading(false);
    }
  };

  const hasHistory = history.length > 0 || chartData.length > 0;

  return (
    <Space orientation="vertical" style={{ width: '100%' }} size="large">
      <Spin spinning={loading}>
        {!hasHistory && !loading ? (
          <Card style={{ borderRadius: 12, textAlign: 'center', padding: '60px 0' }}>
            <Empty description="No history yet. Start checking your lottery tickets!">
              <Button type="primary" size="large" onClick={() => navigate('/lottery')}>
                Go to Lottery
              </Button>
            </Empty>
          </Card>
        ) : (
          <>
            <Card title="History & Analytics" style={{ borderRadius: 12 }}>
              <div style={{ height: 300, width: '100%' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend verticalAlign="bottom" height={36}/>
                    <Bar dataKey="spent" name="Total Spent" fill="#ff4d4f" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="won" name="Total Won" fill="#52c41a" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>

            <Card title="Detailed History" style={{ borderRadius: 12 }}>
              <Table
                pagination={{ pageSize: 5 }}
                dataSource={history.map((h, i) => ({ ...h, key: i }))}
                columns={[
                  { title: 'Date', dataIndex: 'date' },
                  { title: 'Ticket', dataIndex: 'number' },
                  { title: 'Station', dataIndex: 'station' },
                  { title: 'Result', dataIndex: 'isWon', render: (w: any) => <Tag color={!w ? 'red' : 'green'}>{w ? 'Winner' : 'No Prize'}</Tag> },
                  { title: 'Amount', dataIndex: 'amount', render: (a: any) => a?.toLocaleString() + ' VND' },
                ]}
              />
            </Card>
          </>
        )}
      </Spin>
    </Space>
  );
};

export default HistoryAnalytics; 