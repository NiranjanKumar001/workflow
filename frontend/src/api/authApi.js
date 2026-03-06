import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const authApi = {
  // Login
  login: async (credentials) => {
    try {
      const response = await axios.post(`${API_URL}/auth/login`, credentials);

      if (response.data.success && response.data.data) {
        const { accessToken, refreshToken, user } = response.data.data;

        // Store tokens and user
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));

        console.log('✅ Login successful');

        return {
          success: true,
          data: response.data.data
        };
      }

      return {
        success: false,
        message: response.data.message || 'Login failed'
      };
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Login failed'
      };
    }
  },

  // Register
  register: async (userData) => {
    try {
      const response = await axios.post(`${API_URL}/auth/register`, userData);
      return {
        success: true,
        data: response.data.data,
        message: response.data.message
      };
    } catch (error) {
      console.error('Register error:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Registration failed'
      };
    }
  },

  // Refresh Token
  refreshToken: async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });

      if (response.data.success && response.data.data) {
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;

        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        console.log('✅ Token refreshed via authApi');

        return {
          success: true,
          data: response.data.data
        };
      }

      throw new Error('Token refresh failed');
    } catch (error) {
      console.error('❌ Refresh token error:', error);
      authApi.logout();
      return {
        success: false,
        message: 'Session expired. Please login again.'
      };
    }
  },

  // Logout
  logout: async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        // Try to revoke token on server
        await axios.post(`${API_URL}/auth/logout`, { refreshToken });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Always clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      console.log('Logged out');
    }
  },

  // Get current user
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  // Check if user is admin
  isAdmin: () => {
    const user = authApi.getCurrentUser();
    return user?.roles?.includes('ROLE_ADMIN') || false;
  },

  // Check if authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

export { authApi };