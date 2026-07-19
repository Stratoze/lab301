import React from 'react';
import { Card, Typography, Space, Button } from 'antd';
import CheckerForm from './components/CheckerForm';
import CheckerResults from './components/CheckerResults';
import useLotteryChecker from './hooks/useLotteryChecker';

const { Title } = Typography;

const LotteryCheck: React.FC = () => {
  const {
    loading,
    stations,
    results,
    isGuest,
    form,
    setForm,
    handleCheck,
  } = useLotteryChecker();

  return (
    <div style={{ padding: '24px', maxWidth: 800, margin: '0 auto' }}>
      <Card style={{ borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
        <Title level={4}>Lottery Check</Title>
        <Space orientation="vertical" style={{ width: '100%' }} size="middle">
          <CheckerForm
            form={form}
            stations={stations}
            isGuest={isGuest}
            onChange={setForm}
          />

          <Button type="primary" block size="large" loading={loading} onClick={handleCheck} style={{ borderRadius: 12 }}>
            Check Ticket
          </Button>

          {results && (
            <CheckerResults results={results} isGuest={isGuest} />
          )}
        </Space>
      </Card>
    </div>
  );
};

export default LotteryCheck;