import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface ChartPoint {
  name: string;
  spent: number;
  won: number;
}

interface AnalyticsChartProps {
  data: ChartPoint[];
}

const AnalyticsChart: React.FC<AnalyticsChartProps> = ({ data }) => {
  return (
    <div style={{ height: 300, width: '100%' }}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Legend verticalAlign="bottom" height={36} />
          <Bar dataKey="spent" name="Total Spent" fill="#ff4d4f" radius={[4, 4, 0, 0]} />
          <Bar dataKey="won" name="Total Won" fill="#52c41a" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default AnalyticsChart;