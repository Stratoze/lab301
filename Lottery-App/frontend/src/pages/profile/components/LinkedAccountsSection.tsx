import React from 'react';
import { Typography, Space, Tag, Button, Row, Col, Divider } from 'antd';
import {
  GoogleOutlined,
  FacebookFilled,
  PhoneOutlined,
  LinkOutlined,
  DisconnectOutlined,
} from '@ant-design/icons';
import { GoogleLogin } from '@react-oauth/google';
import { loginWithFacebookPopup } from '../../../utils/facebookOAuth';
import { message, theme } from 'antd';
import type { LinkedAccounts } from '../hooks/useProfile';

interface LinkedAccountsSectionProps {
  accounts: LinkedAccounts;
  onLinkGoogle: (token: string) => Promise<void>;
  onLinkFacebook: (token: string) => Promise<void>;
  onAddPhone: () => void;
  onUnlinkPhone: () => Promise<void>;
  loading: boolean;
}

const LinkedAccountsSection: React.FC<LinkedAccountsSectionProps> = ({
  accounts,
  onLinkGoogle,
  onLinkFacebook,
  onAddPhone,
  onUnlinkPhone,
  loading,
}) => {
  const { token: themeToken } = theme.useToken();

  const iconStyle = (linked: boolean) => ({
    fontSize: 18,
    color: linked ? themeToken.colorSuccess : themeToken.colorTextDisabled,
  });

  const handleFacebookLink = () => {
    loginWithFacebookPopup()
      .then(token => onLinkFacebook(token))
      .catch(err => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  const rowStyle: React.CSSProperties = {
    marginBottom: themeToken.marginSM,
  };

  return (
    <>
      <Divider style={{ marginTop: themeToken.marginXS }} />
      <div style={{ marginBottom: themeToken.marginSM }}>
        <Typography.Text strong style={{ fontSize: 16 }}>
          Linked Accounts
        </Typography.Text>
      </div>

      <Row align="middle" justify="space-between" style={rowStyle}>
        <Col>
          <Space>
            <GoogleOutlined style={iconStyle(accounts.googleLinked)} />
            <Typography.Text>Google</Typography.Text>
            {accounts.googleLinked ? (
              <Tag color="success">Linked</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {!accounts.googleLinked && (
            <GoogleLogin
              onSuccess={credentialResponse => {
                onLinkGoogle(credentialResponse.credential!);
              }}
              onError={() => message.error('Google login failed')}
              size="medium"
              shape="rectangular"
              text="continue_with"
            />
          )}
        </Col>
      </Row>

      <Row align="middle" justify="space-between" style={rowStyle}>
        <Col>
          <Space>
            <FacebookFilled style={iconStyle(accounts.facebookLinked)} />
            <Typography.Text>Facebook</Typography.Text>
            {accounts.facebookLinked ? (
              <Tag color="success">Linked</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {!accounts.facebookLinked && (
            <Button
              icon={<FacebookFilled />}
              onClick={handleFacebookLink}
              loading={loading}
              shape="round"
            >
              Link
            </Button>
          )}
        </Col>
      </Row>

      <Row align="middle" justify="space-between" style={rowStyle}>
        <Col>
          <Space>
            <PhoneOutlined style={iconStyle(!!accounts.phone)} />
            <Typography.Text>Phone</Typography.Text>
            {accounts.phone ? (
              <Tag color="success">{accounts.phone}</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {accounts.phone ? (
            <Button
              icon={<DisconnectOutlined />}
              danger
              onClick={onUnlinkPhone}
              loading={loading}
              shape="round"
            >
              Unlink
            </Button>
          ) : (
            <Button
              icon={<LinkOutlined />}
              onClick={onAddPhone}
              shape="round"
            >
              Add Phone
            </Button>
          )}
        </Col>
      </Row>

      <Divider style={{ marginBottom: 0 }} />
    </>
  );
};

export default LinkedAccountsSection;