import React from 'react';
import { Button, Card, Space, Spin, Empty } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useHistoryAnalytics } from './hooks/useHistoryAnalytics';
import AnalyticsChart from './components/AnalyticsChart';
import HistoryTable from './components/HistoryTable';

const HistoryAnalytics: React.FC = () => {
  const { loading, chartData, history, hasHistory } = useHistoryAnalytics();
  const navigate = useNavigate();

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
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
            <AnalyticsChart data={chartData} />
            <HistoryTable data={history} />
          </>
        )}
      </Spin>
    </Space>
  );
};

export default HistoryAnalytics;