const FACEBOOK_APP_ID = import.meta.env.VITE_FACEBOOK_APP_ID;

/**
 * Opens a Facebook OAuth popup (bypasses the blocked SDK).
 * Returns a Promise that resolves with the access token or rejects on failure/timeout.
 */
export function loginWithFacebookPopup(): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!FACEBOOK_APP_ID) {
      reject(new Error('Facebook login not available'));
      return;
    }

    // Listen for the callback message from facebook-callback.html
    const listener = (event: MessageEvent) => {
      if (event.origin !== window.location.origin) return;
      if (event.data?.provider === 'FACEBOOK' && event.data?.token) {
        window.removeEventListener('message', listener);
        clearTimeout(timeout);
        if (popup && !popup.closed) popup.close();
        resolve(event.data.token);
      }
    };
    window.addEventListener('message', listener);

    // Timeout after 2 minutes
    const timeout = setTimeout(() => {
      window.removeEventListener('message', listener);
      if (popup && !popup.closed) popup.close();
      reject(new Error('Facebook login timed out'));
    }, 120_000);

    // Build the OAuth URL
    const redirectUri = `${window.location.origin}/facebook-callback.html`;
    const authUrl = `https://www.facebook.com/v21.0/dialog/oauth?` +
      `client_id=${encodeURIComponent(FACEBOOK_APP_ID)}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=token` +
      `&scope=${encodeURIComponent('email,public_profile')}`;

    // Open popup centered
    const width = 500;
    const height = 500;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;
    const popup = window.open(
      authUrl,
      'facebook-oauth',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    if (!popup) {
      window.removeEventListener('message', listener);
      clearTimeout(timeout);
      reject(new Error('Popup blocked. Please allow popups for this site.'));
      return;
    }

    // Also check periodically if the user closed the popup
    const checkClosed = setInterval(() => {
      if (popup.closed) {
        clearInterval(checkClosed);
        window.removeEventListener('message', listener);
        clearTimeout(timeout);
        reject(new Error('Facebook login was cancelled'));
      }
    }, 500);
  });
}