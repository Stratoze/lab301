import React, { useState } from 'react';
import { Button } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface ChartPoint {
  name: string;
  spent: number;
  won: number;
}

interface AnalyticsChartProps {
  data: ChartPoint[];
}

const formatVnd = (value: number) => {
  if (value >= 1_000_000_000) return `${(value / 1_000_000_000).toFixed(1)}B`;
  if (value >= 1_000_000) return `${(value / 1_000_000).toFixed(1)}M`;
  if (value >= 1_000) return `${(value / 1_000).toFixed(0)}K`;
  return String(value);
};

const AnalyticsChart: React.FC<AnalyticsChartProps> = ({ data }) => {
  const [logScale, setLogScale] = useState(false);

  // Ensure no zero/negative values break the log scale
  const hasValidLogData = data.every(d => d.spent > 0 && d.won > 0);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 8 }}>
        <Button
          size="small"
          onClick={() => setLogScale(v => !v)}
          disabled={logScale ? false : !hasValidLogData}
          type={logScale ? 'primary' : 'default'}
        >
          {logScale ? 'Log Scale' : 'Linear Scale'}
        </Button>
      </div>
      <div style={{ height: 300, width: '100%', minHeight: 300 }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="name" />
            <YAxis
              scale={logScale && hasValidLogData ? 'log' : 'linear'}
              domain={['auto', 'auto']}
              tickFormatter={formatVnd}
              label={{ value: 'Amount (VND)', angle: -90, position: 'insideLeft', style: { fontSize: 12 } }}
            />
            <Tooltip formatter={(value) => [`${Number(value).toLocaleString()} VND`, '']} />
            <Legend verticalAlign="bottom" height={36} />
            <Bar dataKey="spent" name="Total Spent" fill="#ff4d4f" radius={[4, 4, 0, 0]} />
            <Bar dataKey="won" name="Total Won" fill="#52c41a" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default AnalyticsChart;