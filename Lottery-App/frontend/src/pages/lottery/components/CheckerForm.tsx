import React from 'react';
import { Select, DatePicker, Input } from 'antd';
import dayjs from 'dayjs';

interface FormState {
  stationId: number | null;
  date: dayjs.Dayjs;
  numbers: string;
}

interface CheckerFormProps {
  form: FormState;
  stations: any[];
  isGuest: boolean;
  onChange: (form: FormState) => void;
}

const CheckerForm: React.FC<CheckerFormProps> = ({ form, stations, isGuest, onChange }) => {
  return (
    <>
      <Select
        placeholder="Select Station"
        style={{ width: '100%', borderRadius: 12 }}
        value={form.stationId}
        onChange={(v) => onChange({ ...form, stationId: v })}
      >
        {stations.map((s: any) => (
          <Select.Option key={s.id} value={s.id}>
            {s.name}
          </Select.Option>
        ))}
      </Select>

      <DatePicker
        style={{ width: '100%', borderRadius: 2 }}
        value={form.date}
        onChange={(d) => onChange({ ...form, date: d || dayjs() })}
      />

      {isGuest ? (
        <Input
          placeholder="Enter your 6-digit ticket number"
          maxLength={6}
          style={{ borderRadius: 2 }}
          value={form.numbers}
          onChange={(e) => {
            const val = e.target.value.replace(/\D/g, '').slice(0, 6);
            onChange({ ...form, numbers: val });
          }}
        />
      ) : (
        <Input.TextArea
          placeholder="Enter your ticket numbers (separate by comma or newline)"
          rows={4}
          style={{ borderRadius: 2 }}
          value={form.numbers}
          onChange={(e) => onChange({ ...form, numbers: e.target.value })}
        />
      )}
    </>
  );
};

export default CheckerForm;