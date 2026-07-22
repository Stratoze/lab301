import React from 'react';
import { Card, Space, Tag, Typography } from 'antd';

const { Text } = Typography;

export interface DetailRow {
  icon: React.ReactNode;
  label: string;
  value: string;
}

export interface DashboardItemCardProps {
  title: string;
  tags?: Array<{ label: string; color: string; icon?: React.ReactNode }>;
  details: DetailRow[];
  actions?: React.ReactNode[];
  onClick?: () => void;
  selected?: boolean;
}

const DashboardItemCard: React.FC<DashboardItemCardProps> = ({
  title,
  tags,
  details,
  actions,
  onClick,
  selected,
}) => (
  <Card
    onClick={onClick}
    style={{
      marginBottom: 12,
      borderRadius: 12,
      border: selected ? '1px solid #1677ff' : '1px solid #f0f0f0',
      background: selected ? '#e6f4ff' : '#ffffff',
      boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
      cursor: onClick ? 'pointer' : 'default',
      transition: 'all 0.2s',
    }}
    styles={{ body: { padding: 16 } }}
    actions={actions}
  >
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
      <Text strong style={{ fontSize: 15 }}>{title}</Text>
      {tags && (
        <Space size={4} wrap>
          {tags.map((t, i) => (
            <Tag key={i} color={t.color} style={{ borderRadius: 12 }} icon={t.icon}>{t.label}</Tag>
          ))}
        </Space>
      )}
    </div>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {details.map((d, i) => (
        <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ color: '#1677ff', display: 'flex', alignItems: 'center' }}>{d.icon}</span>
          <Text style={{ color: '#595959' }}>{d.label}: {d.value}</Text>
        </div>
      ))}
    </div>
  </Card>
);

export default DashboardItemCard;