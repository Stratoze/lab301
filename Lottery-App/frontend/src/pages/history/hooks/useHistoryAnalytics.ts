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
    let cancelled = false;
    const fetchHistory = async () => {
      setLoading(true);
      try {
        const res = await apiClient.get('/checker/history');
        if (cancelled) return;
        const sessions = res.data.data || [];

        const chart = sessions.map((s: { date: string; totalSpent: number; totalWon: number }) => ({
          name: new Date(s.date).toLocaleDateString(),
          spent: s.totalSpent,
          won: s.totalWon,
        }));
        setChartData(chart);

        const allTickets: Ticket[] = [];
        sessions.forEach((s: { date: string; tickets?: Array<{ checkTime?: string; station: string }> }) => {
          (s.tickets || []).forEach((t: { checkTime?: string; station: string }) => {
            allTickets.push({
              key: allTickets.length,
              date: new Date(s.date).toLocaleDateString(),
              checkTime: t.checkTime || s.date,
              station: t.station,
              number: (t as { number?: string }).number || '',
              isWon: (t as { isWon?: boolean }).isWon || false,
              amount: (t as { amount?: number }).amount || 0,
            });
          });
        });
        setHistory(allTickets);
      } catch (e) {
        console.error('Failed to load history', e);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchHistory();
    return () => { cancelled = true; };
  }, []);

  return { loading, history, chartData, hasHistory: history.length > 0 || chartData.length > 0 };
};

export default useHistoryAnalytics;