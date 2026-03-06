import { useState, useEffect } from 'react';
import { taskApi } from '../api/taskApi';
import toast from 'react-hot-toast';

function TaskStats() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      const response = await taskApi.getTaskStats();

      if (response.success) {
        setStats(response.data);
      }
    } catch (error) {
      console.error('Error loading stats:', error);
      toast.error('Failed to load statistics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Tasks */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Total Tasks</p>
              <p className="text-3xl font-bold text-gray-900">{stats.totalTasks}</p>
            </div>
            <div className="bg-blue-100 rounded-full p-3">
              <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
          </div>
        </div>

        {/* Active Tasks */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Active Tasks</p>
              <p className="text-3xl font-bold text-purple-600">{stats.activeTasks}</p>
            </div>
            <div className="bg-purple-100 rounded-full p-3">
              <svg className="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
              <p className="text-3xl font-bold text-green-600">{stats.completedTasks}</p>
              <p className="text-sm text-gray-500 mt-1">
                {stats.completionRate?.toFixed(1)}% completion rate
              </p>
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
              <p className="text-3xl font-bold text-red-600">{stats.overdueTasks}</p>
              {stats.dueSoonTasks > 0 && (
                <p className="text-sm text-orange-500 mt-1">
                  {stats.dueSoonTasks} due soon
                </p>
              )}
            </div>
            <div className="bg-red-100 rounded-full p-3">
              <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      {/* Status Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* By Status */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Tasks by Status</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                <span className="text-gray-700">To Do</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.todoTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.statusDistribution?.TODO?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-purple-500 rounded-full"></div>
                <span className="text-gray-700">In Progress</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.inProgressTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.statusDistribution?.IN_PROGRESS?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                <span className="text-gray-700">Completed</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.completedTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.statusDistribution?.COMPLETED?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-gray-500 rounded-full"></div>
                <span className="text-gray-700">Archived</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.archivedTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.statusDistribution?.ARCHIVED?.toFixed(1)}%)
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* By Priority */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Tasks by Priority</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                <span className="text-gray-700">Urgent</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.urgentPriorityTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.priorityDistribution?.URGENT?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-orange-500 rounded-full"></div>
                <span className="text-gray-700">High</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.highPriorityTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.priorityDistribution?.HIGH?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                <span className="text-gray-700">Medium</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.mediumPriorityTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.priorityDistribution?.MEDIUM?.toFixed(1)}%)
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 bg-gray-500 rounded-full"></div>
                <span className="text-gray-700">Low</span>
              </div>
              <div className="text-right">
                <span className="font-semibold text-gray-900">{stats.lowPriorityTasks}</span>
                <span className="text-sm text-gray-500 ml-2">
                  ({stats.priorityDistribution?.LOW?.toFixed(1)}%)
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Completion Progress Bar */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-lg font-semibold text-gray-900">Overall Progress</h3>
          <span className="text-2xl font-bold text-green-600">
            {stats.completionRate?.toFixed(1)}%
          </span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-4">
          <div
            className="bg-green-600 h-4 rounded-full transition-all duration-500"
            style={{ width: `${stats.completionRate}%` }}
          ></div>
        </div>
        <p className="text-sm text-gray-600 mt-2">
          {stats.completedTasks} of {stats.totalTasks} tasks completed
        </p>
      </div>
    </div>
  );
}

export default TaskStats;