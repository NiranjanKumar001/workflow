import axiosInstance from './axios';

const authApi = {
  // Register new user
  register: async (userData) => {
    const response = await axiosInstance.post('/auth/register', userData);
    return response;
  },

  // Login user (stores both access token and refresh token)
  login: async (credentials) => {
    const response = await axiosInstance.post('/auth/login', credentials);

    if (response.success && response.data) {
      // Store access token
      localStorage.setItem('token', response.data.token);

      // Store refresh token
      localStorage.setItem('refreshToken', response.data.refreshToken);

      // Store user info
      localStorage.setItem('user', JSON.stringify(response.data.user));

      console.log('Login successful - tokens saved');
    }

    return response;
  },

  // NEW: Refresh access token
  refreshToken: async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await axiosInstance.post('/auth/refresh', {
        refreshToken: refreshToken
      });

      if (response.success && response.data) {
        // Update tokens
        localStorage.setItem('token', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);

        console.log('Tokens refreshed successfully');
        return response.data.accessToken;
      }

      throw new Error('Failed to refresh token');
    } catch (error) {
      console.error('Token refresh failed:', error);
      // Clear tokens and redirect to login
      authApi.logout();
      window.location.href = '/login';
      throw error;
    }
  },

  // UPDATED: Logout with refresh token revocation
  logout: async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        // Revoke refresh token on server
        await axiosInstance.post('/auth/logout', {
          refreshToken: refreshToken
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
      // Continue with local logout even if server request fails
    } finally {
      // Clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  // NEW: Logout from all devices
  logoutAll: async () => {
    try {
      await axiosInstance.post('/auth/logout-all');

      // Clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      console.log('Logged out from all devices');
    } catch (error) {
      console.error('Logout all error:', error);
      throw error;
    }
  },

  // Check if email exists
  checkEmail: async (email) => {
    const response = await axiosInstance.get(`/auth/check-email?email=${email}`);
    return response;
  },

  // Check if username exists
  checkUsername: async (username) => {
    const response = await axiosInstance.get(`/auth/check-username?username=${username}`);
    return response;
  },

  // Get current user from localStorage
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');
    return !!(token && refreshToken);
  },

  // Check if user is admin
  isAdmin: () => {
    const user = authApi.getCurrentUser();
    return user && user.roles && user.roles.includes('ROLE_ADMIN');
  }
};

export { authApi };