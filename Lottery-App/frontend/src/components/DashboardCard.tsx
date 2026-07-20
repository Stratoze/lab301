import React from 'react';
import { Card, Typography } from 'antd';
import { createStyles } from 'antd-style';

const { Title } = Typography;

const useStyles = createStyles(({ token, css }) => ({
  card: css`
    border-radius: ${token.borderRadiusLG}px;
  `,
  cardMobileBody: css`
    .ant-card-body {
      padding: ${token.paddingSM}px;
    }
  `,
  header: css`
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;
  `,
  mobileControls: css`
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 16px;
  `,
}));

interface DashboardCardProps {
  title: string;
  desktopControls?: React.ReactNode;
  mobileControls?: React.ReactNode;
  children: React.ReactNode;
}

const DashboardCard: React.FC<DashboardCardProps> = ({
  title,
  desktopControls,
  mobileControls,
  children,
}) => {
  const { styles } = useStyles();
  const [isMobile, setIsMobile] = React.useState(window.innerWidth <= 768);

  React.useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <Card className={`${styles.card} ${isMobile ? styles.cardMobileBody : ''}`}>
      {/* Title Row */}
      <div className={styles.header}>
        <Title level={4} style={{ margin: 0 }}>{title}</Title>
        {!isMobile && desktopControls && (
          <div>{desktopControls}</div>
        )}
      </div>

      {/* Mobile Controls (stacked) */}
      {isMobile && mobileControls && (
        <div className={styles.mobileControls}>
          {mobileControls}
        </div>
      )}

      {/* Content */}
      {children}
    </Card>
  );
};

export default DashboardCard;