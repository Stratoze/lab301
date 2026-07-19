import { useState, useEffect } from 'react';
import { message } from 'antd';
import apiClient from '../../../api/apiClient';
import dayjs from 'dayjs';

export interface CheckResult {
  details: Array<{
    number: string;
    isWon: boolean;
    prize: string;
  }>;
  summary: {
    totalSpent: number;
    totalWon: number;
  };
}

interface FormState {
  stationId: number | null;
  date: dayjs.Dayjs;
  numbers: string;
}

const useLotteryChecker = () => {
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState<any[]>([]);
  const [results, setResults] = useState<CheckResult | null>(null);
  const isGuest = !localStorage.getItem('token');

  const [form, setForm] = useState<FormState>({
    stationId: null,
    date: dayjs(),
    numbers: '',
  });

  useEffect(() => {
    apiClient.get('/admin/tickets/stations').then(res => {
      const list = res.data.data;
      setStations(list);
      const hcm = list.find((s: any) => s.stationCode === 'SOU-HCM') || list[0];
      if (hcm && !form.stationId) {
        setForm((prev) => ({ ...prev, stationId: hcm.id }));
      }
    });
  }, []);

  const handleCheck = async () => {
    const nums = form.numbers.split(/[\n,;]+/).filter((n: string) => n.trim());

    if (isGuest && nums.length > 1) {
      return message.error('Guests can only check 1 ticket at a time. Please login to check multiple!');
    }

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

  return {
    loading,
    stations,
    results,
    isGuest,
    form,
    setForm,
    handleCheck,
    setResults,
  };
};

export default useLotteryChecker;