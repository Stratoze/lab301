import React from 'react';
import { Typography, Space, Tag, Row, Col, Button, message } from 'antd';
import { GoogleOutlined, FacebookFilled, PhoneOutlined, DisconnectOutlined, LinkOutlined } from '@ant-design/icons';
import { GoogleLogin } from '@react-oauth/google';
import { loginWithFacebookPopup } from '../../../utils/facebookOAuth';
import type { LinkedAccounts } from '../hooks/useProfile';

const { Text } = Typography;

interface LinkedAccountsSectionProps {
  linkedAccounts: LinkedAccounts;
  onLinkSocial: (provider: string, token: string) => void;
  onUnlinkPhone: () => void;
  onAddPhoneClick: () => void;
}

const LinkedAccountsSection: React.FC<LinkedAccountsSectionProps> = ({
  linkedAccounts,
  onLinkSocial,
  onUnlinkPhone,
  onAddPhoneClick
}) => {
  const handleFacebookLink = () => {
    loginWithFacebookPopup()
      .then(token => onLinkSocial('FACEBOOK', token))
      .catch(err => {
        if (err.message !== 'Facebook login was cancelled') {
          message.error(err.message || 'Facebook login failed');
        }
      });
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <div>
        <Text strong style={{ fontSize: 16 }}>Linked Accounts</Text>
      </div>

      <Row align="middle" justify="space-between">
        <Col>
          <Space>
            <GoogleOutlined style={{ fontSize: 18, color: linkedAccounts.googleLinked ? '#52c41a' : '#d9d9d9' }} />
            <Text>Google</Text>
            {linkedAccounts.googleLinked ? (
              <Tag color="success">Linked</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {!linkedAccounts.googleLinked && (
            <GoogleLogin
              onSuccess={(credentialResponse) => {
                onLinkSocial('GOOGLE', credentialResponse.credential!);
              }}
              onError={() => message.error('Google login failed')}
              size="medium"
              shape="rectangular"
              text="continue_with"
            />
          )}
        </Col>
      </Row>

      <Row align="middle" justify="space-between">
        <Col>
          <Space>
            <FacebookFilled style={{ fontSize: 18, color: linkedAccounts.facebookLinked ? '#52c41a' : '#d9d9d9' }} />
            <Text>Facebook</Text>
            {linkedAccounts.facebookLinked ? (
              <Tag color="success">Linked</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {!linkedAccounts.facebookLinked && (
            <Button
              icon={<FacebookFilled />}
              onClick={handleFacebookLink}
              style={{ borderRadius: 12 }}
            >
              Link
            </Button>
          )}
        </Col>
      </Row>

      <Row align="middle" justify="space-between">
        <Col>
          <Space>
            <PhoneOutlined style={{ fontSize: 18, color: linkedAccounts.phone ? '#52c41a' : '#d9d9d9' }} />
            <Text>Phone</Text>
            {linkedAccounts.phone ? (
              <Tag color="success">{linkedAccounts.phone}</Tag>
            ) : (
              <Tag>Not linked</Tag>
            )}
          </Space>
        </Col>
        <Col>
          {linkedAccounts.phone ? (
            <Button
              icon={<DisconnectOutlined />}
              danger
              onClick={onUnlinkPhone}
              style={{ borderRadius: 12 }}
            >
              Unlink
            </Button>
          ) : (
            <Button
              icon={<LinkOutlined />}
              onClick={onAddPhoneClick}
              style={{ borderRadius: 12 }}
            >
              Add Phone
            </Button>
          )}
        </Col>
      </Row>
    </Space>
  );
};

export default LinkedAccountsSection;