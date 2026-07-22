interface FacebookAuthResponse {
  status: string;
  authResponse?: {
    accessToken: string;
    expiresIn: string;
    signedRequest: string;
    userID: string;
  };
}

interface FacebookInitOptions {
  appId: string;
  cookie?: boolean;
  xfbml?: boolean;
  version: string;
  [key: string]: unknown;
}

interface FacebookLoginOptions {
  scope?: string;
  return_scopes?: boolean;
  enable_profile_selector?: boolean;
  auth_type?: string;
  [key: string]: unknown;
}

interface Window {
  FB: {
    init: (options: FacebookInitOptions) => void;
    login: (callback: (response: FacebookAuthResponse) => void, options?: FacebookLoginOptions) => void;
  };
  fbAsyncInit: () => void;
}