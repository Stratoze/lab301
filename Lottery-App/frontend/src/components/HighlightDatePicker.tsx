import React, { useState, useEffect, useMemo } from 'react';
import { DatePicker, theme } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import apiClient from '../api/apiClient';

interface HighlightDatePickerProps {
  stationId: number | null;
  value?: Dayjs | null;
  onChange?: (date: Dayjs | null) => void;
  style?: React.CSSProperties;
  /** If true (default), dates not in availableDates are disabled */
  disableUnavailable?: boolean;
}

const HighlightDatePicker: React.FC<HighlightDatePickerProps> = ({
  stationId,
  value,
  onChange,
  style,
  disableUnavailable = true,
}) => {
  const { token } = theme.useToken();

  const [availableDates, setAvailableDates] = useState<string[]>([]);
  const [panelMode, setPanelMode] = useState<'year' | 'month' | 'date'>('date');

  useEffect(() => {
    if (!stationId) return;
    apiClient
      .get('/checker/available-dates', { params: { stationId } })
      .then((res) => {
        setAvailableDates(res.data.data || []);
      })
      .catch(() => {
        setAvailableDates([]);
      });
  }, [stationId]);

  const dateInfoMap = useMemo(() => {
    const map = new Map<string, { yearSet: Set<string>; monthSet: Set<string> }>();
    for (const d of availableDates) {
      const parsed = dayjs(d);
      if (!parsed.isValid()) continue;
      const year = parsed.format('YYYY');
      const month = parsed.format('YYYY-MM');
      if (!map.has(year)) map.set(year, { yearSet: new Set(), monthSet: new Set() });
      const info = map.get(year)!;
      info.yearSet.add(d);
      info.monthSet.add(month);
    }
    return map;
  }, [availableDates]);

  const hasDate = (d: Dayjs) => availableDates.includes(d.format('YYYY-MM-DD'));

  // Panel-aware disabledDate
  const disabledDate = (current: Dayjs) => {
    if (!disableUnavailable || !availableDates.length) return false;
    if (panelMode === 'year') {
      return !dateInfoMap.has(current.format('YYYY'));
    }
    if (panelMode === 'month') {
      const monthKey = current.format('YYYY-MM');
      for (const [, info] of dateInfoMap) {
        if (info.monthSet.has(monthKey)) return false;
      }
      return true;
    }
    return !availableDates.includes(current.format('YYYY-MM-DD'));
  };

  const cellRender = (
    rawCurrent: Dayjs | string | number,
    info: { type: string; originNode: React.ReactElement }
  ) => {
    const current = dayjs(rawCurrent);
    if (!availableDates.length || !current.isValid()) return info.originNode;

    const highlightStyle: React.CSSProperties = {
      background: token.colorPrimaryBg,
      color: token.colorPrimary,
      fontWeight: 600,
      padding: '2px 0',
    };

    if (info.type === 'year') {
      const yearKey = current.format('YYYY');
      const has = dateInfoMap.has(yearKey);
      return (
        <div style={has ? highlightStyle : undefined}>
          {info.originNode}
        </div>
      );
    }

    if (info.type === 'month') {
      const monthKey = current.format('YYYY-MM');
      let has = false;
      for (const [, infoItem] of dateInfoMap) {
        if (infoItem.monthSet.has(monthKey)) {
          has = true;
          break;
        }
      }
      return (
        <div style={has ? highlightStyle : undefined}>
          {info.originNode}
        </div>
      );
    }

    if (info.type === 'date') {
      const isAvailable = hasDate(current);
      return (
        <div
          style={{
            background: isAvailable ? token.colorPrimaryBg : undefined,
            color: isAvailable ? token.colorPrimary : undefined,
            borderRadius: isAvailable ? '50%' : undefined,
            fontWeight: isAvailable ? 700 : undefined,
          }}
        >
          {current.date()}
        </div>
      );
    }

    return info.originNode;
  };

  return (
    <DatePicker
      style={{ width: '100%', ...style }}
      value={value}
      onChange={onChange}
      disabledDate={disabledDate}
      cellRender={cellRender}
      onPanelChange={(_, mode) => setPanelMode(mode as 'year' | 'month' | 'date')}
    />
  );
};

export default HighlightDatePicker;