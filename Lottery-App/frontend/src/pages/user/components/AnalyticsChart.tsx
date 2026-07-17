import React from 'react';
import { Card } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { ChartDataPoint } from '../hooks/useHistoryAnalytics';

interface AnalyticsChartProps {
  data: ChartDataPoint[];
}

const AnalyticsChart: React.FC<AnalyticsChartProps> = ({ data }) => {
  return (
    <Card title="History & Analytics" style={{ borderRadius: 12 }}>
      <div style={{ height: 300, width: '100%' }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend verticalAlign="bottom" height={36}/>
            <Bar dataKey="spent" name="Total Spent" fill="#ff4d4f" radius={[4, 4, 0, 0]} />
            <Bar dataKey="won" name="Total Won" fill="#52c41a" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </Card>
  );
};

export default AnalyticsChart;