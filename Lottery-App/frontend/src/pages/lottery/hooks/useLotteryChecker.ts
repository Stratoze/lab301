import { useState, useEffect, useRef } from 'react';
import { message } from 'antd';
import apiClient from '../../../api/apiClient';
import { useAuthContext } from '../../../contexts/useAuthContext';
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

interface Station {
  id: number;
  stationCode: string;
  name: string;
}

interface FormState {
  stationId: number | null;
  date: dayjs.Dayjs;
  numbers: string;
}

const useLotteryChecker = () => {
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState<Station[]>([]);
  const [results, setResults] = useState<CheckResult | null>(null);
  const [availableDates, setAvailableDates] = useState<string[]>([]);
  const { isAuthenticated } = useAuthContext();
  const isGuest = !isAuthenticated;
  const initializedRef = useRef(false);

  const [form, setForm] = useState<FormState>({
    stationId: null,
    date: dayjs(),
    numbers: '',
  });

  useEffect(() => {
    apiClient.get('/admin/tickets/stations').then(res => {
      const list: Station[] = res.data.data;
      setStations(list);
      if (!initializedRef.current) {
        const hcm = list.find((s) => s.stationCode === 'SOU-HCM') || list[0];
        if (hcm) {
          initializedRef.current = true;
          setForm((prev) => ({ ...prev, stationId: hcm.id }));
        }
      }
    });
  }, []);

  // Fetch available dates when station changes
  useEffect(() => {
    if (!form.stationId) return;
    apiClient.get('/checker/available-dates', { params: { stationId: form.stationId } })
      .then(res => {
        setAvailableDates(res.data.data || []);
      })
      .catch(() => setAvailableDates([]));
  }, [form.stationId]);

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
      const res = await apiClient.post('/checker/check', nums, {
        params: { stationId: form.stationId, date: form.date.format('YYYY-MM-DD') }
      });
      setResults(res.data.data);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Check failed');
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
    availableDates,
  };
};

export default useLotteryChecker;