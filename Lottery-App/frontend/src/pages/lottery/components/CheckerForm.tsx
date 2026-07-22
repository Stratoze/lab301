import React from 'react';
import { Select } from 'antd';
import dayjs from 'dayjs';
import LotteryNumberInput from '../../../components/LotteryNumberInput';
import HighlightDatePicker from '../../../components/HighlightDatePicker';

interface FormState {
  stationId: number | null;
  date: dayjs.Dayjs;
  numbers: string;
}

interface Station {
  id: number;
  name: string;
}

interface CheckerFormProps {
  form: FormState;
  stations: Station[];
  isGuest: boolean;
  onChange: (form: FormState) => void;
}

const CheckerForm: React.FC<CheckerFormProps> = ({ form, stations, isGuest, onChange }) => {
  return (
    <>
      <Select
        placeholder="Select Station"
        style={{ width: '100%' }}
        onChange={(v) => onChange({ ...form, stationId: v, date: dayjs() })}
      >
        {stations.map((s) => (
          <Select.Option key={s.id} value={s.id}>
            {s.name}
          </Select.Option>
        ))}
      </Select>

      <HighlightDatePicker
        stationId={form.stationId}
        value={form.date}
        onChange={(d) => onChange({ ...form, date: d || dayjs() })}
      />

      <LotteryNumberInput
        chunkSize={6}
        maxChunks={isGuest ? 1 : 50}
        value={form.numbers}
        onChange={(val) => onChange({ ...form, numbers: val })}
        placeholder={isGuest ? 'Enter your 6-digit ticket number' : 'Enter ticket numbers (comma or Enter to add)'}
      />
    </>
  );
};

export default CheckerForm;