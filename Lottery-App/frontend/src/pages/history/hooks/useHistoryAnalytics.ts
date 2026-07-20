import { useState, useEffect } from 'react';
import apiClient from '../../../api/apiClient';

interface ChartPoint {
  name: string;
  spent: number;
  won: number;
}

interface Ticket {
  key: number;
  date: string;
  checkTime: string;
  number: string;
  station: string;
  isWon: boolean;
  amount: number;
}

const useHistoryAnalytics = () => {
  const [loading, setLoading] = useState(true);
  const [history, setHistory] = useState<Ticket[]>([]);
  const [chartData, setChartData] = useState<ChartPoint[]>([]);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const email = localStorage.getItem('email');
      const res = await apiClient.get('/checker/history', { params: { email } });
      const sessions = res.data.data || [];

      const chart = sessions.map((s: any) => ({
        name: new Date(s.date).toLocaleDateString(),
        spent: s.totalSpent,
        won: s.totalWon,
      }));
      setChartData(chart);

      const allTickets: Ticket[] = [];
      sessions.forEach((s: any) => {
        (s.tickets || []).forEach((t: any) => {
          allTickets.push({
            ...t,
            key: allTickets.length,
            date: new Date(s.date).toLocaleDateString(),
            checkTime: t.checkTime || s.date,
            station: t.station,
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

  return { loading, history, chartData, hasHistory: history.length > 0 || chartData.length > 0 };
};

export default useHistoryAnalytics;