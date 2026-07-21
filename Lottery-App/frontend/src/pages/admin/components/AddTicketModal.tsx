import React, { useEffect } from 'react';
import { Modal, Form, Select, Button, Radio, message } from 'antd';
import apiClient from '../../../api/apiClient';
import LotteryNumberInput from '../../../components/LotteryNumberInput';
import HighlightDatePicker from '../../../components/HighlightDatePicker';
import dayjs from 'dayjs';

interface Props {
  open: boolean;
  onClose: () => void;
  ticket?: any;
  stations: any[];
  onSuccess: () => void;
}

const prizeConfig: Record<string, { label: string; chunkSize: number; maxChunks: number; rewardAmount: number }> = {
  g_db:  { label: 'Special (6 digits)',         chunkSize: 6, maxChunks: 1, rewardAmount: 2000000000 },
  g1:    { label: '1st (5 digits)',             chunkSize: 5, maxChunks: 1, rewardAmount: 30000000 },
  g2:    { label: '2nd (5 digits)',             chunkSize: 5, maxChunks: 1, rewardAmount: 15000000 },
  g3:    { label: '3rd (5 digits, 2 numbers)',  chunkSize: 5, maxChunks: 2, rewardAmount: 10000000 },
  g4:    { label: '4th (5 digits, 7 numbers)',  chunkSize: 5, maxChunks: 7, rewardAmount: 3000000 },
  g5:    { label: '5th (4 digits)',             chunkSize: 4, maxChunks: 1, rewardAmount: 1000000 },
  g6:    { label: '6th (4 digits, 3 numbers)',  chunkSize: 4, maxChunks: 3, rewardAmount: 400000 },
  g7:    { label: '7th (3 digits)',             chunkSize: 3, maxChunks: 1, rewardAmount: 200000 },
  g8:    { label: '8th (2 digits)',             chunkSize: 2, maxChunks: 1, rewardAmount: 100000 },
};

const AddTicketModal: React.FC<Props> = ({ open, onClose, ticket, stations, onSuccess }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open && ticket) {
      const station = stations.find((s: any) => s.name === ticket.stationName);
      form.setFieldsValue({
        stationId: station?.id || null,
        drawDate: ticket.drawDate ? dayjs(ticket.drawDate) : null,
        status: ticket.status,
        g_db: getNumbers(ticket.prizes, 'G_DB'),
        g1: getNumbers(ticket.prizes, 'G1'),
        g2: getNumbers(ticket.prizes, 'G2'),
        g3: getNumbers(ticket.prizes, 'G3'),
        g4: getNumbers(ticket.prizes, 'G4'),
        g5: getNumbers(ticket.prizes, 'G5'),
        g6: getNumbers(ticket.prizes, 'G6'),
        g7: getNumbers(ticket.prizes, 'G7'),
        g8: getNumbers(ticket.prizes, 'G8'),
      });
    } else if (open && !ticket) {
      form.resetFields();
      const hcmStation = stations.find((s: any) => s.stationCode === 'SOU-HCM') || stations[0];
      form.setFieldsValue({
        status: 'UNPUBLISH',
        stationId: hcmStation?.id || null,
        drawDate: dayjs(),
      });
    }
  }, [ticket, open, stations, form]);

  const getNumbers = (prizes: any[], type: string) => {
    if (!prizes) return '';
    return prizes
      .filter((p: any) => p.type === type)
      .map((p: any) => p.winningNumber)
      .join(',');
  };

  const onFinish = async (values: any) => {
    const prizes = Object.entries(prizeConfig).map(([field, config]) => ({
      type: field.toUpperCase(),
      winningNumbers: values[field] || '',
      rewardAmount: config.rewardAmount,
    })).filter(p => p.winningNumbers.trim() !== '');

    // Count validation for multi-entry prizes
    for (const p of prizes) {
      const nums = p.winningNumbers.split(',').filter((n: string) => n.trim());
      const config = prizeConfig[p.type.toLowerCase().replace('g', 'g_') as keyof typeof prizeConfig];
      if (config && nums.length !== config.maxChunks) {
        return message.error(`${config.label} must have exactly ${config.maxChunks} number(s). Found ${nums.length}.`);
      }
    }

    // Length validation per prize type
    const expectedLengths: Record<string, number> = {
      G_DB: 6, G1: 5, G2: 5, G3: 5, G4: 5, G5: 4, G6: 4, G7: 3, G8: 2,
    };
    for (const p of prizes) {
      const nums = p.winningNumbers.split(',').filter((n: string) => n.trim());
      const expectedLen = expectedLengths[p.type] || 0;
      for (const n of nums) {
        if (n.length !== expectedLen) {
          return message.error(`Prize ${p.type} expects ${expectedLen}-digit numbers. "${n}" is ${n.length} digits.`);
        }
      }
    }

    // Overlapping-number validation
    for (let i = 0; i < prizes.length; i++) {
      const numsI = prizes[i].winningNumbers.split(',').filter((n: string) => n.trim());
      for (let j = i + 1; j < prizes.length; j++) {
        const numsJ = prizes[j].winningNumbers.split(',').filter((n: string) => n.trim());
        for (const a of numsI) {
          for (const b of numsJ) {
            if (a !== b && (a.endsWith(b) || b.endsWith(a))) {
              return message.error(`Overlapping numbers detected: "${a}" and "${b}". Please correct.`);
            }
          }
        }
      }
    }

    // Duplicate check across all prizes
    const allNumbers = prizes.flatMap(p => p.winningNumbers.split(',').filter((n: string) => n.trim()));
    const uniqueNumbers = new Set(allNumbers);
    if (allNumbers.length !== uniqueNumbers.size) {
      return message.error('Duplicate winning numbers detected! Each number must be unique.');
    }

    try {
      await apiClient.post('/admin/tickets', {
        stationId: values.stationId,
        drawDate: values.drawDate.format('YYYY-MM-DD'),
        status: values.status,
        prizes,
      });
      message.success('Ticket saved successfully');
      onSuccess();
      onClose();
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || 'Failed to save ticket (Potential duplicate date/station)';
      message.error(errorMsg);
    }
  };

  return (
    <Modal
      title={ticket ? 'Edit Ticket' : 'Add Ticket'}
      open={open}
      onCancel={onClose}
      footer={null}
      centered
      width={700}
      zIndex={1100}
      destroyOnHidden
      style={{ top: 20 }}
      styles={{ body: { maxHeight: '70vh', overflowY: 'auto' } }}
    >
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <Form.Item name="stationId" label="Station" style={{ flex: 1, minWidth: 200 }} rules={[{ required: true, message: 'Please select a station' }]}>
            <Select placeholder="Select Station">
              {stations.map((s: any) => (
                <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            noStyle
            shouldUpdate={(prev, cur) => prev.stationId !== cur.stationId}
          >
            {({ getFieldValue }) => {
              const currentStationId = getFieldValue('stationId');
              return (
                <Form.Item name="drawDate" label="Select date" style={{ flex: 1, minWidth: 200 }} rules={[{ required: true, message: 'Please select a date' }]}>
                  <HighlightDatePicker stationId={currentStationId} disableUnavailable={false} style={{ width: '100%' }} />
                </Form.Item>
              );
            }}
          </Form.Item>
        </div>

        {Object.entries(prizeConfig).map(([field, config]) => (
          <Form.Item
            key={field}
            name={field}
            label={config.label}
            rules={[{ required: true, message: 'Required' }]}
          >
            <LotteryNumberInput
              chunkSize={config.chunkSize}
              maxChunks={config.maxChunks}
              placeholder={`${config.chunkSize} digits`}
            />
          </Form.Item>
        ))}

        <Form.Item name="status" label="Status">
          <Radio.Group buttonStyle="solid">
            <Radio.Button value="UNPUBLISH">Unpublished</Radio.Button>
            <Radio.Button value="PUBLISH">Published</Radio.Button>
          </Radio.Group>
        </Form.Item>

        <div style={{ textAlign: 'right', marginTop: 24, display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button type="primary" htmlType="submit">Save</Button>
          <Button onClick={onClose}>Cancel</Button>
        </div>
      </Form>
    </Modal>
  );
};

export default AddTicketModal;