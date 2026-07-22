import React from 'react';
import { Alert, Button, Divider } from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  InfoCircleOutlined,
  FacebookFilled,
} from '@ant-design/icons';
import type { CheckResult } from '../hooks/useLotteryChecker';

interface CheckerResultsProps {
  results: CheckResult;
  isGuest: boolean;
}

const CheckerResults: React.FC<CheckerResultsProps> = ({ results, isGuest }) => {
  return (
    <div style={{ marginTop: 24 }}>
      <Divider>Results</Divider>

      {results.details.map((res, i) => (
        <Alert
          key={i}
          style={{ marginBottom: 12, borderRadius: 12 }}
          title={`Ticket: ${res.number}`}
          description={
            res.isWon
              ? `Congratulations!! you won the ${res.prize} prize`
              : 'Better luck next time'
          }
          type={res.isWon ? 'success' : 'error'}
          showIcon
          icon={res.isWon ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
          action={
            res.isWon && !isGuest ? (
              <Button
                size="small"
                type="primary"
                icon={<FacebookFilled />}
                style={{ borderRadius: 12 }}
                onClick={() => {
                  const shareUrl =
                    `https://www.facebook.com/sharer/sharer.php?u=` +
                    `${encodeURIComponent(window.location.href)}` +
                    `&quote=I just won ${res.prize} with ticket ${res.number}!`;

                  window.open(shareUrl, '_blank', 'width=600,height=400');
                }}
              >
                Share
              </Button>
            ) : undefined
          }
        />
      ))}

      <Alert
        title="Summary"
        description={`Total Spent: ${results.summary.totalSpent.toLocaleString()} VND | Total Won: ${results.summary.totalWon.toLocaleString()} VND`}
        type="info"
        showIcon
        icon={<InfoCircleOutlined />}
        style={{ borderRadius: 12 }}
      />
    </div>
  );
};

export default CheckerResults;