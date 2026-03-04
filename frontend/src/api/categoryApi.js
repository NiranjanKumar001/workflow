import axiosInstance from './axios';

export const categoryApi = {
  // Get all categories for a user
  getAllCategories: async (userId) => {
    const response = await axiosInstance.get(`/categories?userId=${userId}`);
    return response;
  },

  // Get category by ID
  getCategoryById: async (userId, categoryId) => {
    const response = await axiosInstance.get(`/categories/${userId}/${categoryId}`);
    return response;
  },

  // Create new category
  createCategory: async (userId, categoryData) => {
    const response = await axiosInstance.post(`/categories?userId=${userId}`, categoryData);
    return response;
  },

  // Update category
  updateCategory: async (userId, categoryId, categoryData) => {
    const response = await axiosInstance.put(`/categories/${userId}/${categoryId}`, categoryData);
    return response;
  },

  // Delete category
  deleteCategory: async (userId, categoryId) => {
    const response = await axiosInstance.delete(`/categories/${userId}/${categoryId}`);
    return response;
  },
};