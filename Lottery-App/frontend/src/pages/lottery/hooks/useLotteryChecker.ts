import { useState, useEffect } from 'react';
import { message } from 'antd';
import apiClient from '../../../api/apiClient';
import dayjs from 'dayjs';

export interface Station {
  id: number;
  stationCode: string;
  name: string;
  region: string;
}

export interface CheckSummary {
  totalSpent: number;
  totalWon: number;
}

export interface CheckDetail {
  number: string;
  isWon: boolean;
  prize?: string;
}

export interface CheckResults {
  summary: CheckSummary;
  details: CheckDetail[];
}

export const useLotteryChecker = () => {
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState<Station[]>([]);
  const [results, setResults] = useState<CheckResults | null>(null);
  const isGuest = !localStorage.getItem('token');

  useEffect(() => {
    apiClient.get('/admin/tickets/stations')
      .then(res => {
        setStations(res.data.data);
      })
      .catch(err => {
        message.error('Failed to load lottery stations');
        console.error(err);
      });
  }, []);

  const runCheck = async (values: { stationId: number; date: dayjs.Dayjs; numbers: string }) => {
    const rawNums = values.numbers.split(/[\n,;]+/).filter((n: string) => n.trim());

    if (isGuest && rawNums.length > 1) {
      message.error('Guests can only check 1 ticket at a time. Please login to check multiple!');
      return;
    }

    // Validate 6 digits
    for (const num of rawNums) {
      if (!/^\d{6}$/.test(num.trim())) {
        message.error(`Invalid ticket number: "${num.trim()}". Each ticket must be exactly 6 digits.`);
        return;
      }
    }

    setLoading(true);
    try {
      const res = await apiClient.post('/checker/check', rawNums, {
        params: { 
          stationId: values.stationId, 
          date: values.date.format('YYYY-MM-DD')
        }
      });
      setResults(res.data.data);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Check failed');
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    stations,
    results,
    isGuest,
    runCheck
  };
};