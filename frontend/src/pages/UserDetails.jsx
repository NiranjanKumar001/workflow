import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from '../api/axios';
import toast from 'react-hot-toast';

function UserDetails() {
  const navigate = useNavigate();
  const { userId } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUser();
  }, [userId]);

  const loadUser = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/admin/users/${userId}`);

      if (response.success) {
        setUser(response.data);
      }
    } catch (error) {
      console.error('Error loading user:', error);
      toast.error('Failed to load user details');
      navigate('/admin');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>User not found</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <button
          onClick={() => navigate('/admin')}
          className="text-blue-600 hover:text-blue-700 mb-4"
        >
          ← Back to Admin Dashboard
        </button>

        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">User Details</h1>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600">User ID</label>
              <p className="text-lg text-gray-900">{user.id}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-600">Username</label>
              <p className="text-lg text-gray-900">{user.username}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-600">Email</label>
              <p className="text-lg text-gray-900">{user.email}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-600">Status</label>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              }`}>
                {user.enabled ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserDetails;