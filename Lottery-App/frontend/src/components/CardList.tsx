import React from 'react';
import { Select, Skeleton, Space, Empty } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import DashboardItemCard from './DashboardItemCard';
import type { DashboardItemCardProps } from './DashboardItemCard';

interface SortOption {
  value: string;
  label: string;
}

interface CardListProps {
  items?: DashboardItemCardProps[];
  children?: React.ReactNode;
  loading?: boolean;
  sortBy?: string;
  onSortChange?: (value: string) => void;
  sortOptions?: SortOption[];
  emptyDescription?: string;
}

const CardList: React.FC<CardListProps> = ({
  items,
  children,
  loading,
  sortBy,
  onSortChange,
  sortOptions,
  emptyDescription = 'No data',
}) => {
  if (loading) {
    return <Skeleton active paragraph={{ rows: 4 }} />;
  }

  const hasContent = (items && items.length > 0) || children;

  return (
    <>
      {sortOptions && onSortChange && (
        <Select
          placeholder="Sort By"
          value={sortBy}
          onChange={onSortChange}
          suffixIcon={<DownOutlined />}
          style={{ width: '100%', marginBottom: 16}}
          options={sortOptions}
        />
      )}

      {!hasContent ? (
        <Empty description={emptyDescription} />
      ) : (
        <Space orientation="vertical" size={12} style={{ width: '100%' }}>
          {items
            ? items.map((item, index) => (
                <DashboardItemCard key={index} {...item} />
              ))
            : children}
        </Space>
      )}
    </>
  );
};

export default CardList;