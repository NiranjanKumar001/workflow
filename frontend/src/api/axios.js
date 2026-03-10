import axios from 'axios';
import { authApi } from './authApi';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Flag to prevent multiple refresh attempts
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// Request Interceptor - Add token to every request
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    console.log('🔹 Request:', config.method.toUpperCase(), config.url);

    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response Interceptor - Handle token refresh
axiosInstance.interceptors.response.use(
  (response) => {
    console.log('✅ Response:', response.config.url, response.status);

    // Return standardized response
    return {
      success: true,
      data: response.data.data || response.data,
      message: response.data.message || 'Success',
      status: response.status
    };
  },
  async (error) => {
    const originalRequest = error.config;

    console.error('❌ Response Error:', error.config?.url, error.response?.status);

    // if 401 and we haven't tried to refresh yet
    if (error.response?.status === 401 && !originalRequest._retry) {

      // If already refreshing, queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token;
            return axiosInstance(originalRequest);
          })
          .catch(err => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        console.log('❌ No refresh token - redirecting to login');
        isRefreshing = false;
        authApi.logout();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        console.log('🔄 Attempting to refresh token...');

        // Call refresh endpoint
        const response = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );

        if (response.data.success && response.data.data) {
          const { accessToken, refreshToken: newRefreshToken } = response.data.data;

          console.log('✅ Token refreshed successfully');

          // Update tokens
          localStorage.setItem('token', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);

          // Update axios header
          axiosInstance.defaults.headers.common['Authorization'] = 'Bearer ' + accessToken;
          originalRequest.headers['Authorization'] = 'Bearer ' + accessToken;

          // Process queued requests
          processQueue(null, accessToken);
          isRefreshing = false;

          // Retry original request
          return axiosInstance(originalRequest);
        } else {
          throw new Error('Refresh failed');
        }
      } catch (refreshError) {
        console.error('❌ Token refresh failed:', refreshError);

        processQueue(refreshError, null);
        isRefreshing = false;

        // Clear tokens and redirect to login
        authApi.logout();
        window.location.href = '/login';

        return Promise.reject(refreshError);
      }
    }

    // For other errors, return standardized error
    return Promise.reject({
      success: false,
      message: error.response?.data?.message || error.message || 'An error occurred',
      status: error.response?.status,
      data: error.response?.data
    });
  }
);

export default axiosInstance;