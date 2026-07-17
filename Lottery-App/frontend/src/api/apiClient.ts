import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach JWT token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: normalize errors & handle 401
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Auto-redirect on 401, but NOT for login attempts
      const isLoginRequest = error.config?.url?.includes('/auth/login');
      if (error.response.status === 401 && !isLoginRequest) {
        localStorage.clear();
        window.location.href = '/auth';
        return Promise.reject(error);
      }
      const data = error.response.data;
      if (typeof data === 'string') {
        error.response.data = { message: data };
      } else if (data && !data.message) {
        data.message = data.error || data.detail || 'An unexpected error occurred.';
      } else if (!data) {
        error.response.data = { message: 'An unexpected error occurred.' };
      }
    } else if (error.request) {
      error.response = { data: { message: 'Network error. Please check your connection.' } };
    } else {
      error.response = { data: { message: error.message } };
    }
    return Promise.reject(error);
  }
);

export default apiClient;