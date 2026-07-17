import React from 'react';
import { Card, Typography } from 'antd';
import { useLotteryChecker } from './hooks/useLotteryChecker';
import CheckerForm from './components/CheckerForm';
import CheckerResults from './components/CheckerResults';

const { Title } = Typography;

const LotteryCheck: React.FC = () => {
  const { loading, stations, results, isGuest, runCheck } = useLotteryChecker();

  return (
    <div style={{ padding: '24px', maxWidth: 800, margin: '0 auto' }}>
      <Card style={{ borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
        <Title level={4} style={{ marginBottom: 24 }}>Lottery Check</Title>
        <CheckerForm 
          loading={loading}
          stations={stations}
          isGuest={isGuest}
          onFinish={runCheck}
        />
        {results && (
          <CheckerResults 
            results={results}
            isGuest={isGuest}
          />
        )}
      </Card>
    </div>
  );
};

export default LotteryCheck;