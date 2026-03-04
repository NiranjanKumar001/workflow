import axiosInstance from './axios';

export const taskApi = {
  // Get all tasks (userId extracted from JWT token)
  getAllTasks: async () => {
    const response = await axiosInstance.get('/tasks');
    return response;
  },

  // Get task by ID (userId extracted from JWT token)
  getTaskById: async (taskId) => {
    const response = await axiosInstance.get(`/tasks/${taskId}`);
    return response;
  },

  // Create new task (userId extracted from JWT token)
  createTask: async (taskData) => {
    const response = await axiosInstance.post('/tasks', taskData);
    return response;
  },

  // Update task (userId extracted from JWT token)
  updateTask: async (taskId, taskData) => {
    const response = await axiosInstance.put(`/tasks/${taskId}`, taskData);
    return response;
  },

  // Delete task (userId extracted from JWT token)
  deleteTask: async (taskId) => {
    const response = await axiosInstance.delete(`/tasks/${taskId}`);
    return response;
  },

  // Filter tasks with pagination (userId extracted from JWT token)
  filterTasks: async (filters = {}) => {
    const params = new URLSearchParams(filters);
    const response = await axiosInstance.get(`/tasks/filter?${params}`);
    return response;
  },

  // Get overdue tasks (userId extracted from JWT token)
  getOverdueTasks: async () => {
    const response = await axiosInstance.get('/tasks/overdue');
    return response;
  },

  // Get tasks due soon (userId extracted from JWT token)
  getTasksDueSoon: async (days = 7) => {
    const response = await axiosInstance.get(`/tasks/due-soon?days=${days}`);
    return response;
  },

  // Get task statistics (userId extracted from JWT token)
  getTaskStats: async () => {
    const response = await axiosInstance.get('/tasks/stats');
    return response;
  },
};