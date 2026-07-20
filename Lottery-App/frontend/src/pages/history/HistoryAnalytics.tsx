import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Spin, Empty } from 'antd';

import { useNavigate } from 'react-router-dom';
import useHistoryAnalytics from './hooks/useHistoryAnalytics';
import AnalyticsChart from './components/AnalyticsChart';
import HistoryTable from './components/HistoryTable';
import DashboardCard from '../../components/DashboardCard';
import CardList from '../../components/CardList';
import HistoryCard from '../../components/HistoryCard';

const HistoryAnalytics: React.FC = () => {
  const { loading, history, chartData, hasHistory } = useHistoryAnalytics();
  const navigate = useNavigate();
  const [sortBy, setSortBy] = useState<string>('latest');
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const sortedHistory = useMemo(() => {
    const arr = [...history];
    switch (sortBy) {
      case 'oldest':
        arr.sort((a, b) => new Date(a.checkTime).getTime() - new Date(b.checkTime).getTime());
        break;
      case 'station':
        arr.sort((a, b) => (a.station || '').localeCompare(b.station || ''));
        break;
      case 'ticket':
        arr.sort((a, b) => (a.number || '').localeCompare(b.number || ''));
        break;
      case 'won':
        arr.sort((a, b) => (b.isWon === a.isWon ? 0 : b.isWon ? 1 : -1));
        break;
      case 'amount':
        arr.sort((a, b) => b.amount - a.amount);
        break;
      case 'latest':
      default:
        arr.sort((a, b) => new Date(b.checkTime).getTime() - new Date(a.checkTime).getTime());
        break;
    }
    return arr;
  }, [history, sortBy]);

  const formatCheckTime = (iso: string) => {
    const d = new Date(iso);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    const ss = String(d.getSeconds()).padStart(2, '0');
    return `${yyyy}/${mm}/${dd} : ${hh}/${min}/${ss}`;
  };

  return (
    <div>
      <DashboardCard title="History & Analytics">
        <Spin spinning={loading}>
          {!hasHistory && !loading ? (
            <div style={{ textAlign: 'center', padding: '60px 0' }}>
              <Empty description="No history yet. Start checking your lottery tickets!">
                <Button type="primary" size="large" onClick={() => navigate('/lottery')}>
                  Go to Lottery
                </Button>
              </Empty>
            </div>
          ) : (
            <>
              {!isMobile ? (
                /* Desktop */
                <>
                  <Card style={{ borderRadius: 12, marginBottom: 24 }}>
                    <AnalyticsChart data={chartData} />
                  </Card>
                  <Card style={{ borderRadius: 12 }}>
                    <HistoryTable data={history} />
                  </Card>
                </>
              ) : (
                /* Mobile */
                <>
                  <Card style={{ borderRadius: 12, marginBottom: 16 }}>
                    <AnalyticsChart data={chartData} />
                  </Card>
                  <CardList
                    loading={loading}
                    sortBy={sortBy}
                    onSortChange={setSortBy}
                    sortOptions={[
                      { value: 'latest', label: 'Latest First' },
                      { value: 'oldest', label: 'Oldest First' },
                      { value: 'station', label: 'Station' },
                      { value: 'ticket', label: 'Ticket Number' },
                      { value: 'won', label: 'Result' },
                      { value: 'amount', label: 'Amount Won' },
                    ]}
                  >
                    {sortedHistory.map((ticket) => (
                      <HistoryCard key={ticket.key} ticket={ticket} formatCheckTime={formatCheckTime} />
                    ))}
                  </CardList>
                </>
              )}
            </>
          )}
        </Spin>
      </DashboardCard>
    </div>
  );
};

export default HistoryAnalytics;