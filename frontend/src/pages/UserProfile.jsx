import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axios';
import toast from 'react-hot-toast';

function UserProfile() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);

  const [editForm, setEditForm] = useState({
    username: '',
    email: ''
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/users/me');
      if (response.success) {
        setProfile(response.data);
        setEditForm({
          username: response.data.username,
          email: response.data.email
        });
      }
    } catch (error) {
      console.error('Error fetching profile:', error);
      toast.error('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.put('/users/me', editForm);
      if (response.success) {
        toast.success('Profile updated successfully!');
        setProfile(response.data);
        setEditing(false);

        // FIX: Null check before accessing localStorage user
        const stored = localStorage.getItem('user');
        if (stored) {
          const user = JSON.parse(stored);
          user.username = response.data.username;
          user.email = response.data.email;
          localStorage.setItem('user', JSON.stringify(user));
        }
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      if (error.response?.data?.fieldErrors) {
        const errors = error.response.data.fieldErrors;
        Object.values(errors).forEach(err => toast.error(err));
      } else {
        toast.error(error.response?.data?.message || 'Failed to update profile');
      }
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }

    try {
      const response = await axios.put('/users/me/password', passwordForm);
      if (response.success) {
        toast.success('Password changed! Please login again.');
        setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
        setChangingPassword(false);

        // FIX: Clear ALL auth storage including refreshToken
        setTimeout(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          navigate('/login');
        }, 2000);
      }
    } catch (error) {
      console.error('Error changing password:', error);
      toast.error(error.response?.data?.message || 'Failed to change password');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-600">Failed to load profile</p>
      </div>
    );
  }

  const isOAuthUser = profile.provider !== 'LOCAL';

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-4xl mx-auto px-4">

        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => navigate('/dashboard')}
            className="text-blue-600 hover:text-blue-700 mb-4"
          >
            ← Back to Dashboard
          </button>
          <h1 className="text-3xl font-bold text-gray-900">My Profile</h1>
        </div>

        {/* Profile Information Card */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex justify-between items-start mb-6">
            <h2 className="text-xl font-semibold text-gray-800">Profile Information</h2>
            {!editing && (
              <button
                onClick={() => setEditing(true)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Edit Profile
              </button>
            )}
          </div>

          {!editing ? (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-600">Username</label>
                <p className="text-lg text-gray-900">{profile.username}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">Email</label>
                <p className="text-lg text-gray-900">{profile.email}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">Account Type</label>
                <div className="flex items-center gap-2">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    profile.provider === 'GOOGLE'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-blue-100 text-blue-800'
                  }`}>
                    {profile.provider}
                  </span>
                  {profile.provider === 'GOOGLE' && profile.providerId && (
                    <span className="text-sm text-gray-500">(Connected with Google)</span>
                  )}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">Account Status</label>
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                  profile.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                }`}>
                  {profile.enabled ? 'Active' : 'Inactive'}
                </span>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">Member Since</label>
                <p className="text-lg text-gray-900">
                  {new Date(profile.createdAt).toLocaleDateString()}
                </p>
              </div>
            </div>
          ) : (
            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Username
                </label>
                <input
                  type="text"
                  value={editForm.username}
                  onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                  {/* FIX: OAuth users cannot change email — managed by Google */}
                  {isOAuthUser && (
                    <span className="ml-2 text-xs text-gray-400">(managed by {profile.provider})</span>
                  )}
                </label>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  // FIX: Disable email edit for OAuth users
                  disabled={isOAuthUser}
                  className={`w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 ${
                    isOAuthUser ? 'bg-gray-100 cursor-not-allowed text-gray-500' : ''
                  }`}
                  required
                />
              </div>

              <div className="flex gap-3">
                <button
                  type="submit"
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Save Changes
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditing(false);
                    setEditForm({ username: profile.username, email: profile.email });
                  }}
                  className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Statistics Card */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Statistics</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600">Total Tasks</p>
              <p className="text-3xl font-bold text-blue-600">{profile.totalTasks ?? 0}</p>
            </div>
            <div className="bg-green-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600">Completed Tasks</p>
              <p className="text-3xl font-bold text-green-600">{profile.completedTasks ?? 0}</p>
            </div>
            <div className="bg-purple-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600">Categories</p>
              <p className="text-3xl font-bold text-purple-600">{profile.totalCategories ?? 0}</p>
            </div>
          </div>
        </div>

        {/* Change Password — LOCAL users only */}
        {!isOAuthUser && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-start mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-800">Change Password</h2>
                <p className="text-sm text-gray-600 mt-1">
                  Update your password to keep your account secure
                </p>
              </div>
              {!changingPassword && (
                <button
                  onClick={() => setChangingPassword(true)}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                >
                  Change Password
                </button>
              )}
            </div>

            {changingPassword && (
              <form onSubmit={handlePasswordSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Current Password
                  </label>
                  <input
                    type="password"
                    value={passwordForm.currentPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    New Password
                  </label>
                  <input
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                    minLength={6}
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Must be at least 6 characters with uppercase, lowercase, digit, and special character
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    value={passwordForm.confirmPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>
                <div className="flex gap-3">
                  <button
                    type="submit"
                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    Update Password
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setChangingPassword(false);
                      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
                    }}
                    className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* OAuth User Notice */}
        {isOAuthUser && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-blue-800">
              <strong>Note:</strong> You're logged in with {profile.provider}.
              Password and email management is handled by your {profile.provider} account.
            </p>
          </div>
        )}

      </div>
    </div>
  );
}

export default UserProfile;
