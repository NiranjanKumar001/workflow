import axiosInstance from './axios';

const commentApi = {
  addComment: async (taskId, content) => {
    return await axiosInstance.post(`/comments/task/${taskId}`, { content });
  },

  getTaskComments: async (taskId) => {
    return await axiosInstance.get(`/comments/task/${taskId}`);
  },

  updateComment: async (commentId, content) => {
    return await axiosInstance.put(`/comments/${commentId}`, { content });
  },

  deleteComment: async (commentId) => {
    return await axiosInstance.delete(`/comments/${commentId}`);
  }
};

export { commentApi };