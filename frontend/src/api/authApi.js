import axios from 'axios';

const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const authApi = {
  /**
   * Login user
   */
  login: async (credentials) => {
    try {
      console.log('🔐 Starting login process...');
      console.log('📧 Email:', credentials.email);

      const response = await axios.post(`${API_URL}/auth/login`, credentials);

      console.log('📦 Full Response:', response);
      console.log('📦 Response Data:', response.data);

      if (response.data.success && response.data.data) {
        const { token: accessToken, refreshToken, user } = response.data.data;

        // Validate response data
        if (!accessToken || !refreshToken || !user) {
          console.error('❌ Missing data in response:', {
            hasAccessToken: !!accessToken,
            hasRefreshToken: !!refreshToken,
            hasUser: !!user
          });
          throw new Error('Invalid response from server');
        }

        // Store tokens and user
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));

        // Verify storage
        const storedToken = localStorage.getItem('token');
        const storedRefreshToken = localStorage.getItem('refreshToken');
        const storedUser = localStorage.getItem('user');

        console.log('✅ Login successful!');
        console.log('✅ Access Token stored:', !!storedToken);
        console.log('✅ Refresh Token stored:', !!storedRefreshToken);
        console.log('✅ User data stored:', !!storedUser);
        console.log('👤 User:', user.username);
        console.log('🎭 Roles:', user.roles);
        console.log('🔑 Token Preview:', accessToken.substring(0, 30) + '...');

        return {
          success: true,
          data: response.data.data,
          message: 'Login successful'
        };
      }

      console.error('❌ Login failed - invalid response structure');
      return {
        success: false,
        message: response.data.message || 'Login failed - invalid response'
      };
    } catch (error) {
      console.error('❌ Login error:', error);
      console.error('❌ Error response:', error.response?.data);
      console.error('❌ Error status:', error.response?.status);

      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Login failed'
      };
    }
  },

  /**
   * Register new user
   */
  register: async (userData) => {
    try {
      console.log('📝 Starting registration...');
      console.log('📧 Email:', userData.email);

      const response = await axios.post(`${API_URL}/auth/register`, userData);

      console.log('✅ Registration response:', response.data);

      return {
        success: true,
        data: response.data.data,
        message: response.data.message || 'Registration successful'
      };
    } catch (error) {
      console.error('❌ Registration error:', error);
      console.error('❌ Error response:', error.response?.data);

      return {
        success: false,
        message: error.response?.data?.message || 'Registration failed'
      };
    }
  },

  /**
   * Refresh access token
   */
  refreshToken: async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        console.error('❌ No refresh token found in localStorage');
        throw new Error('No refresh token available');
      }

      console.log('🔄 Attempting to refresh token...');

      const response = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });

      console.log('📦 Refresh response:', response.data);

      if (response.data.success && response.data.data) {
        const {accessToken: accessToken, refreshToken: newRefreshToken } = response.data.data;

        if (!accessToken || !newRefreshToken) {
          console.error('❌ Invalid refresh response');
          throw new Error('Invalid refresh response');
        }

        // Update tokens
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        console.log('✅ Token refreshed successfully');
        console.log('🔑 New Token Preview:', accessToken.substring(0, 30) + '...');

        return {
          success: true,
          data: response.data.data
        };
      }

      throw new Error('Token refresh failed');
    } catch (error) {
      console.error('❌ Refresh token error:', error);
      console.error('❌ Error response:', error.response?.data);

      // Clear invalid tokens
      authApi.logout();

      return {
        success: false,
        message: 'Session expired. Please login again.'
      };
    }
  },

  /**
   * Logout user
   */
  logout: async () => {
    try {
      console.log('🚪 Starting logout...');

      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        console.log('🔄 Revoking refresh token on server...');
        await axios.post(`${API_URL}/auth/logout`, { refreshToken });
        console.log('✅ Token revoked on server');
      }
    } catch (error) {
      console.error('❌ Logout error:', error);
      // Continue with local cleanup even if server logout fails
    } finally {
      // Always clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      console.log('✅ Logged out - local storage cleared');
      console.log('🔑 Token removed:', !localStorage.getItem('token'));
      console.log('👤 User removed:', !localStorage.getItem('user'));
    }
  },

  /**
   * Get current user from localStorage
   */
  getCurrentUser: () => {
    try {
      const userStr = localStorage.getItem('user');

      if (!userStr) {
        console.log('ℹ️ No user in localStorage');
        return null;
      }

      const user = JSON.parse(userStr);
      console.log('👤 Current user:', user.username);
      console.log('🎭 Roles:', user.roles);

      return user;
    } catch (error) {
      console.error('❌ Error parsing user from localStorage:', error);
      return null;
    }
  },

  /**
   * Check if user is admin
   */
  isAdmin: () => {
    const user = authApi.getCurrentUser();
    const isAdmin = user?.roles?.includes('ROLE_ADMIN') || false;

    console.log('🔐 Admin check:', isAdmin);

    return isAdmin;
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    const isAuth = !!(token && user);

    console.log('🔐 Auth check:', {
      hasToken: !!token,
      hasUser: !!user,
      isAuthenticated: isAuth
    });

    return isAuth;
  },

  /**
   * Debug function - Check current auth state
   */
  debugAuthState: () => {
    const token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');
    const user = localStorage.getItem('user');

    console.log('=== 🔍 AUTH STATE DEBUG ===');
    console.log('Has Token:', !!token);
    console.log('Token Preview:', token ? token.substring(0, 50) + '...' : 'NONE');
    console.log('Has Refresh Token:', !!refreshToken);
    console.log('Has User:', !!user);

    if (user) {
      try {
        const userData = JSON.parse(user);
        console.log('User Data:', userData);
      } catch (e) {
        console.error('Invalid user JSON:', e);
      }
    }

    console.log('isAuthenticated():', authApi.isAuthenticated());
    console.log('isAdmin():', authApi.isAdmin());
    console.log('========================');
  }
};

export { authApi };