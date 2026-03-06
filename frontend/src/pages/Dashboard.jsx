import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { taskApi } from '../api/taskApi';
import toast from 'react-hot-toast';
import Logo from '../assets/logo.png';
import EmailVerificationBanner from '../components/EmailVerificationBanner';

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [stats, setStats] = useState(null);
  const [recentTasks, setRecentTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    const currentUser = authApi.getCurrentUser();
    if (!currentUser) {
      navigate('/login');
      return;
    }
    setUser(currentUser);
    setLoading(false);

    await Promise.allSettled([loadStats(), loadRecentTasks()]);
  };

  const loadStats = async () => {
    try {
      const res = await taskApi.getTaskStats();
      if (res.success) {
        setStats(res.data);
      }
    } catch (err) {
      console.error('Stats error:', err);
    }
  };

  const loadRecentTasks = async () => {
    try {
      const res = await taskApi.filterTasks({
        page: 0,
        size: 5,
        sortBy: 'createdAt',
        sortDirection: 'DESC'
      });
      if (res.success) {
        setRecentTasks(res.data.content ?? []);
      }
    } catch (err) {
      console.error('Recent tasks error:', err);
      if (err.response?.status === 401) {
        toast.error('Session expired. Please login again.');
        authApi.logout();
        navigate('/login');
      } else {
        toast.error('Failed to load recent tasks');
      }
    }
  };

  const handleLogout = () => {
    authApi.logout();
    toast.success('Logged out successfully');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <EmailVerificationBanner />

      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div>
            <img
              src={Logo}
              alt="WorkFlow Logo"
              className="h-20 w-auto object-contain -my-4 -ml-1"
              style={{ position: 'relative', top: '2px', left: '-22px' }}
            />
            <p className="text-gray-600">Welcome back, {user?.username}!</p>
          </div>

          <div className="flex gap-4">
            {/* Admin Dashboard Button */}
            {authApi.isAdmin() && (
              <button
                onClick={() => navigate('/admin')}
                className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition"
              >
                Admin Dashboard
              </button>
            )}

            {/* Statistics Button */}
            <button
              onClick={() => navigate('/statistics')}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              📊 Statistics
            </button>

            {/* My Profile Button */}
            <button
              onClick={() => navigate('/profile')}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
            >
              My Profile
            </button>

            {/* View All Tasks Button */}
            <button
              onClick={() => navigate('/tasks')}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              View All Tasks
            </button>

            {/* Logout Button */}
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
          {/* Total Tasks */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Total Tasks</p>
                <p className="text-3xl font-bold text-gray-900">{stats?.totalTasks || 0}</p>
              </div>
              <div className="bg-blue-100 rounded-full p-3">
                <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
            </div>
          </div>

          {/* To Do */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">To Do</p>
                <p className="text-3xl font-bold text-blue-600">{stats?.todoTasks || 0}</p>
              </div>
              <div className="bg-blue-100 rounded-full p-3">
                <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          {/* In Progress */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">In Progress</p>
                <p className="text-3xl font-bold text-yellow-600">{stats?.inProgressTasks || 0}</p>
              </div>
              <div className="bg-yellow-100 rounded-full p-3">
                <svg className="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
            </div>
          </div>

          {/* Completed */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Completed</p>
                <p className="text-3xl font-bold text-green-600">{stats?.completedTasks || 0}</p>
              </div>
              <div className="bg-green-100 rounded-full p-3">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          {/* Overdue */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Overdue</p>
                <p className="text-3xl font-bold text-red-600">{stats?.overdueTasks || 0}</p>
              </div>
              <div className="bg-red-100 rounded-full p-3">
                <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Tasks */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">Recent Tasks</h2>
          </div>
          <div className="p-6">
            {recentTasks.length === 0 ? (
              <div className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                <p className="mt-4 text-gray-500">No tasks yet. Create your first task!</p>
                <button
                  onClick={() => navigate('/tasks')}
                  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  Create Task
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {recentTasks.map((task) => (
                  <div
                    key={task.id}
                    className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer"
                    onClick={() => navigate('/tasks')}
                  >
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">{task.title}</h3>
                        <p className="text-sm text-gray-600 mt-1">{task.description}</p>
                      </div>
                      <div className="flex gap-2 ml-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          task.status === 'TODO' ? 'bg-blue-100 text-blue-800' :
                          task.status === 'IN_PROGRESS' ? 'bg-yellow-100 text-yellow-800' :
                          'bg-green-100 text-green-800'
                        }`}>
                          {task.status.replace('_', ' ')}
                        </span>
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          task.priority === 'HIGH' || task.priority === 'URGENT'
                            ? 'bg-red-100 text-red-800' :
                          task.priority === 'MEDIUM' ? 'bg-orange-100 text-orange-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {task.priority}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;