import { useState, useEffect } from 'react';
import apiClient from '../../../api/apiClient';

export interface ChartDataPoint {
  name: string;
  spent: number;
  won: number;
}

export interface TicketRecord {
  key: string;
  date: string;
  number: string;
  station: string;
  isWon: boolean;
  amount: number;
}

export const useHistoryAnalytics = () => {
  const [loading, setLoading] = useState(true);
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [history, setHistory] = useState<TicketRecord[]>([]);

  useEffect(() => {
    const fetchHistory = async () => {
      setLoading(true);
      try {
        const res = await apiClient.get('/checker/history');
        const sessions = res.data.data || [];
        
        // Build chart data
        const chart = sessions.map((s: any) => ({
          name: new Date(s.date || s.createdAt).toLocaleDateString(),
          spent: s.totalSpent,
          won: s.totalWon
        }));
        setChartData(chart);

        // Flatten tickets
        const allTickets: TicketRecord[] = [];
        let keyCounter = 0;
        sessions.forEach((s: any) => {
          (s.tickets || []).forEach((t: any) => {
            allTickets.push({
              key: String(keyCounter++),
              date: new Date(s.date || s.createdAt).toLocaleDateString(),
              number: t.number || t.ticketNumber,
              station: t.station || t.stationName || 'N/A',
              isWon: t.isWon,
              amount: t.amount || t.wonAmount || 0
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

    fetchHistory();
  }, []);

  const hasHistory = history.length > 0 || chartData.length > 0;

  return {
    loading,
    chartData,
    history,
    hasHistory
  };
};