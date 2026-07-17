import React, { useEffect } from 'react';
import { Form, Select, DatePicker, Input, Button } from 'antd';
import dayjs from 'dayjs';
import type { Station } from '../hooks/useLotteryChecker';

interface CheckerFormProps {
  loading: boolean;
  stations: Station[];
  isGuest: boolean;
  onFinish: (values: { stationId: number; date: dayjs.Dayjs; numbers: string }) => void;
}

const CheckerForm: React.FC<CheckerFormProps> = ({ 
  loading, 
  stations, 
  isGuest, 
  onFinish 
}) => {
  const [form] = Form.useForm();

  // Automatically select HCM station as default when stations load
  useEffect(() => {
    if (stations.length > 0) {
      const defaultStation = stations.find(s => s.stationCode === 'SOU-HCM') || stations[0];
      form.setFieldsValue({ stationId: defaultStation.id });
    }
  }, [stations, form]);

  const onFormSubmit = (values: any) => {
    onFinish(values);
  };

  return (
    <Form 
      form={form} 
      layout="vertical" 
      onFinish={onFormSubmit}
      initialValues={{ date: dayjs(), numbers: '' }}
    >
      <Form.Item 
        name="stationId" 
        label="Select Station" 
        rules={[{ required: true, message: 'Please select a lottery station' }]}
      >
        <Select placeholder="Select Station" style={{ borderRadius: 12 }}>
          {stations.map(s => (
            <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>
          ))}
        </Select>
      </Form.Item>

      <Form.Item 
        name="date" 
        label="Select Date" 
        rules={[{ required: true, message: 'Please select a date' }]}
      >
        <DatePicker style={{ width: '100%', borderRadius: 12 }} />
      </Form.Item>

      <Form.Item 
        name="numbers" 
        label="Ticket Number(s)" 
        rules={[
          { required: true, message: 'Please enter at least one ticket number' },
          {
            validator: (_, value) => {
              if (!value) return Promise.resolve();
              const rawNums = value.split(/[\n,;]+/).filter((n: string) => n.trim());
              if (isGuest && rawNums.length > 1) {
                return Promise.reject(new Error('Guests can only check 1 ticket at a time. Please login to check multiple!'));
              }
              for (const num of rawNums) {
                if (!/^\d{6}$/.test(num.trim())) {
                  return Promise.reject(new Error(`"${num.trim()}" must be exactly 6 digits.`));
                }
              }
              return Promise.resolve();
            }
          }
        ]}
      >
        {isGuest ? (
          <Input 
            placeholder="Enter your 6-digit ticket number" 
            maxLength={6} 
            style={{ borderRadius: 12 }}
          />
        ) : (
          <Input.TextArea 
            placeholder="Enter your ticket numbers (separate by comma, space, or newline)" 
            rows={4} 
            style={{ borderRadius: 12 }}
          />
        )}
      </Form.Item>

      <Form.Item>
        <Button 
          type="primary" 
          htmlType="submit" 
          block 
          size="large" 
          loading={loading} 
          style={{ borderRadius: 12, height: 44 }}
        >
          Check Ticket
        </Button>
      </Form.Item>
    </Form>
  );
};

export default CheckerForm;