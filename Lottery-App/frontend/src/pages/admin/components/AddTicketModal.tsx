import React, { useEffect } from 'react';
import { Modal, Form, Select, DatePicker, Input, Button, Radio, message } from 'antd';
import apiClient from '../../../api/apiClient';
import dayjs from 'dayjs';

interface Props {
  open: boolean;
  onClose: () => void;
  ticket?: any;
  stations: any[];
  onSuccess: () => void;
}

const AddTicketModal: React.FC<Props> = ({ open, onClose, ticket, stations, onSuccess }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open && ticket) {
      // ticket is available, stations should be loaded by now
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
      form.setFieldsValue({ status: 'UNPUBLISH' });
    }
  }, [ticket, open, stations, form]);

  const getNumbers = (prizes: any[], type: string) => {
    if (!prizes) return '';
    return prizes
      .filter((p: any) => p.type === type)
      .map((p: any) => p.winningNumber)
      .join('\n');
  };

  const onFinish = async (values: any) => {
    const mapPrize = (val: string, type: string, amount: number) => ({
      type, 
      winningNumbers: val || '', 
      rewardAmount: amount 
    });

    const prizes = [
      mapPrize(values.g_db, 'G_DB', 2000000000),
      mapPrize(values.g1, 'G1', 30000000),
      mapPrize(values.g2, 'G2', 15000000),
      mapPrize(values.g3, 'G3', 10000000),
      mapPrize(values.g4, 'G4', 3000000),
      mapPrize(values.g5, 'G5', 1000000),
      mapPrize(values.g6, 'G6', 400000),
      mapPrize(values.g7, 'G7', 200000),
      mapPrize(values.g8, 'G8', 100000),
    ].filter(p => p.winningNumbers.trim() !== '');

    // Duplicate check across all prizes
    const allNumbers = prizes.flatMap(p => p.winningNumbers.split(/[\s\n,]+/)).filter(n => n);
    const uniqueNumbers = new Set(allNumbers);
    if (allNumbers.length !== uniqueNumbers.size) {
      return message.error('Duplicate winning numbers detected! Each number must be unique.');
    }

    // Validate length per prize type
    const expectedLengths: Record<string, number> = {
      G_DB: 6, G1: 5, G2: 5, G3: 5, G4: 5, G5: 4, G6: 4, G7: 3, G8: 2
    };
    for (const p of prizes) {
      const nums = p.winningNumbers.split(/[\s\n,]+/).filter(n => n);
      const expectedLen = expectedLengths[p.type] || 0;
      for (const n of nums) {
        if (n.length !== expectedLen) {
          return message.error(`Prize ${p.type} expects ${expectedLen}-digit numbers. "${n}" is ${n.length} digits.`);
        }
      }
    }

    try {
      await apiClient.post('/admin/tickets', {
        stationId: values.stationId,
        drawDate: values.drawDate.format('YYYY-MM-DD'),
        status: values.status,
        prizes
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
      title={ticket ? "Edit Ticket" : "Add Ticket"}
      open={open}
      onCancel={onClose}
      footer={null}
      centered
      width={700}
      zIndex={1100}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <div style={{ display: 'flex', gap: 16 }}>
          <Form.Item name="stationId" label="Station" style={{ flex: 1 }} rules={[{ required: true, message: 'Please select a station' }]}>
            <Select placeholder="Select Station">
              {stations.map((s: any) => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="drawDate" label="Select date" style={{ flex: 1 }} rules={[{ required: true, message: 'Please select a date' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </div>

        <Form.Item name="g_db" label="Special (6 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="123456" maxLength={6} />
        </Form.Item>
        <Form.Item name="g1" label="1st (5 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="12345" maxLength={5} />
        </Form.Item>
        <Form.Item name="g2" label="2nd (5 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="12346" maxLength={5} />
        </Form.Item>
        <Form.Item name="g3" label="3rd (5 digits, 2 lines)" rules={[{ required: true, message: 'Required' }]}>
          <Input.TextArea placeholder="12344, 12354" rows={2} />
        </Form.Item>
        <Form.Item name="g4" label="4th (5 digits, 7 lines)" rules={[{ required: true, message: 'Required' }]}>
          <Input.TextArea placeholder="7 numbers separated by comma or newline" rows={3} />
        </Form.Item>
        <Form.Item name="g5" label="5th (4 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="1234" maxLength={4} />
        </Form.Item>
        <Form.Item name="g6" label="6th (4 digits, 3 lines)" rules={[{ required: true, message: 'Required' }]}>
          <Input.TextArea placeholder="3 numbers separated by comma or newline" rows={2} />
        </Form.Item>
        <Form.Item name="g7" label="7th (3 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="123" maxLength={3} />
        </Form.Item>
        <Form.Item name="g8" label="8th (2 digits, 1 line)" rules={[{ required: true, message: 'Required' }]}>
          <Input placeholder="12" maxLength={2} />
        </Form.Item>

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