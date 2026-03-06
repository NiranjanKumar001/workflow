import axiosInstance from './axios';

const taskApi = {
  // Create new task
  createTask: async (taskData) => {
    const response = await axiosInstance.post('/tasks', taskData);
    return response;
  },

  // Get all tasks for current user
  getAllTasks: async () => {
    const response = await axiosInstance.get('/tasks');
    return response;
  },

  // Get task by ID
  getTaskById: async (taskId) => {
    const response = await axiosInstance.get(`/tasks/${taskId}`);
    return response;
  },

  // Get tasks by status
  getTasksByStatus: async (status) => {
    const response = await axiosInstance.get(`/tasks/status/${status}`);
    return response;
  },

  // Update task
  updateTask: async (taskId, taskData) => {
    const response = await axiosInstance.put(`/tasks/${taskId}`, taskData);
    return response;
  },

  // Update task status
  updateTaskStatus: async (taskId, status) => {
    const response = await axiosInstance.patch(`/tasks/${taskId}/status?status=${status}`);
    return response;
  },

  // Delete task
  deleteTask: async (taskId) => {
    const response = await axiosInstance.delete(`/tasks/${taskId}`);
    return response;
  },

  // Filter tasks with pagination
  filterTasks: async (filterData) => {
    const response = await axiosInstance.post('/tasks/filter', filterData);
    return response;
  },

  // Get overdue tasks
  getOverdueTasks: async () => {
    const response = await axiosInstance.get('/tasks/overdue');
    return response;
  },

  // Get tasks due soon
  getTasksDueSoon: async (days = 7) => {
    const response = await axiosInstance.get(`/tasks/due-soon?days=${days}`);
    return response;
  },

  // Get task statistics
  getTaskStats: async () => {
    const response = await axiosInstance.get('/tasks/stats');
    return response;
  }
};

export { taskApi };